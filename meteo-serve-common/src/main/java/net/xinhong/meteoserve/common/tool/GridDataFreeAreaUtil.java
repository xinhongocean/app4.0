package net.xinhong.meteoserve.common.tool;

import com.xinhong.mids3d.datareader.util.FreeArea;
import net.xinhong.meteoserve.common.grib.GridData;

/**
 * Created by shijunna on 2016/7/20.
 */
public class GridDataFreeAreaUtil {

    public static GridData getFreeArea(GridData gridData, FreeArea freeArea){
        try{
            //1.交换经纬度，使原数据和自由区域纬度相同
            if(gridData != null && gridData.getLatAry1D() != null){
                if((freeArea.geteLat() - freeArea.getsLat()) != (gridData.getXEnd() - gridData.getXStart())){
                    //进行交换，使得纬度方向相同
                    gridData = getExchangeLat(gridData);
                }
            }

            //调整大小
            //2.创建新的GridData，并将自有区域范围设置给GridData
            GridData gridData_new = new GridData();
            gridData_new.setXStart(freeArea.getsLon());
            gridData_new.setXEnd(freeArea.geteLon());
            gridData_new.setYStart(freeArea.getsLat());
            gridData_new.setYEnd(freeArea.geteLat());

            //3.设置新的GridData的行列间距
            gridData_new.setXDel(gridData.getXDel());
            gridData_new.setYDel(gridData.getYDel());
            //所选择的自由区域和查出的区域起始结束经纬度相同，则直接返回gridData
            //4.判断自有区域和原始数据的区域大小，如果区域相等则直接返回原始数据
            //或者原始数据和自有区域都为全球区域，也直接返回原始数据
            if(gridData_new.getXStart() == gridData.getXStart() && gridData_new.getXEnd() == gridData.getXEnd() &&
                    gridData_new.getYStart() == gridData.getYStart() && gridData_new.getYEnd() == gridData.getYEnd())
                return gridData;
            else if(((gridData.getXEnd() - gridData.getXStart()) + gridData.getXDel()) >= 360 &&
                    ((gridData.getYEnd() - gridData.getYStart()) + gridData.getYDel()) >= 180 &&
                    ((freeArea.geteLat() - freeArea.getsLat()) + gridData.getYDel()) >= 180 &&
                    ((freeArea.geteLon() - freeArea.getsLon()) + gridData.getXDel()) >= 360 ){
                return  gridData;
            }
            //5.设置新的GridData的行列数

            //将原始数据和自有区域的经度全部调整为0~360模式的数据
            FreeArea ff = freeArea.clone();
            ff = getFreeAreaFromLon0(ff);
            gridData = getGridDataFromLon0(gridData);
            int row = -1;int col = -1;
            row = (int) (Math.abs(Math.ceil(ff.geteLat()) - Math.floor(ff.getsLat()))/gridData.getXDel()) + 1;
            if(ff.geteLat() < ff.getsLon()){
                col = (int) (((360 - Math.floor(ff.getsLon())) + Math.ceil(ff.geteLon()))/gridData.getYDel()) + 1;
            }else
                col = (int) (Math.abs(Math.ceil(ff.geteLon()) - Math.floor(ff.getsLon()))/gridData.getYDel()) + 1;

            //6.如果新的GridData的行列数大于原始数据的行列数，则直接返回原始数据
            if(row > gridData.getRowNum() || col >gridData.getColNum()){
                System.out.println("所截取的区域的行列数大于原数据的行列数，无法截取");
                return gridData;
            }
            //7.校验经度
//			float[] lon = exchangeLon(dataType,freeArea.getsLon(), freeArea.geteLon());
//			//根据传入的自由区域的经纬度信息。自动填充经纬度的数组信息
//			//8.计算截取的开始行列数
//			double sr1 = (Math.abs((gridData_new.getYStart() - gridData.getYStart())) / gridData.getYDel());
//			//合并时，发现GRID切割时是：
//			//double sc1 = (Math.abs((lon[0] - gridData.getXStart())) / gridData.getXDel());
//			//NC 切割 和GRADS切割时是：
//			//double sc1 = (Math.abs((freeArea.getsLon() - gridData.getXStart())) / gridData.getXDel());
//			//默认先用GRID的，稍后测试后再做修改
//			double sc1 = (Math.abs((lon[0] - gridData.getXStart())) / gridData.getXDel());
//			int rStartIndex = (int) sr1;//开始行数
//			int cStartIndex = (int) sc1;//开始列数
            int rStartIndex = -1;//开始行数
            int cStartIndex = -1;//开始列数

            //9.校验行列数
            if(gridData.getLatAry2D() != null){
                float[][] latAry2 = gridData.getLatAry2D();
                //校验开始行数
                for(int i=0;i<latAry2.length; i++){
                    if(latAry2[i][0] == ff.getsLat()){
                        rStartIndex = i;
                        break;
                    }else if(latAry2[i][0] > ff.getsLat()){
                        gridData_new.setLonsDataChange(true);
                        rStartIndex = i - 1;
                        break;
                    }
                }
                //校验行数
                for(int i=0;i<latAry2.length; i++){
                    if(latAry2[i][0] >= ff.geteLat()){
                        row = i - rStartIndex + 1;
                        break;
                    }
                }
            }
            if(gridData.getLonAry2D() != null){
                float[][] lonAry2 = gridData.getLonAry2D();
                //校验开始列数
                for(int i=0;i<lonAry2[0].length; i++){
                    if(lonAry2[0][i] == ff.getsLon()){
                        cStartIndex = i;
                        break;
                    }else if(lonAry2[0][i] > ff.getsLon()){
                        gridData_new.setLonsDataChange(true);
                        cStartIndex = i - 1;
                        break;
                    }
                }
                //校验列数
                if(ff.geteLat() < ff.getsLon()){

                }else{
                    for(int i=0;i<lonAry2[0].length; i++){
                        if(lonAry2[0][i] >= ff.geteLon()){
                            col = i - cStartIndex + 1;
                            break;
                        }
                    }
                }
            }
            gridData_new.setRowNum(row);
            gridData_new.setColNum(col);

            //赋值数组
            int gridColCnt = gridData.getColNum();

            //如果sCol1 + col > gridData.getColNum() 或 sRow1 + row > gridData.getRowNum()
            //并且不循环获取数据？？？？？？？？？？？？
            boolean isCircle = true;
//			boolean isCircle = false;
//			if(Math.abs((gridData.getYEnd() - gridData.getYStart())) >= 180 ||
//					Math.abs((gridData.getXEnd() - gridData.getXStart())) >= 360)
//				isCircle = true;
            if((cStartIndex + col) > gridData.getColNum() ){
                if(isCircle == false){
                    col = col - ((cStartIndex + col) - gridData.getColNum());
                }
            }
            if((rStartIndex + row) > gridData.getRowNum()){
                if(isCircle == false){
                    row = row - ((rStartIndex + row) - gridData.getRowNum());
                }
            }

            float[][] dataAry = new float[row][col];
            int rowIndex = 0;
            for(int i=rStartIndex; i<row + rStartIndex; i++){
                int colIndex = 0;
                for(int j=cStartIndex; j<col + cStartIndex; j++){
                    if(j>=gridColCnt && cStartIndex >0){//如果列的索引值大于网格的列数，且列的开始值大于0，则循环网格从0开始取数据
                        dataAry[rowIndex][colIndex] = gridData.getGridData()[i][j - gridColCnt];
                    }else{
                        dataAry[rowIndex][colIndex] = gridData.getGridData()[i][j];
                    }
                    colIndex ++;
                }
                rowIndex++;
            }
            gridData_new.setGridData(dataAry);
            //赋值经纬度区域
            float[] lats = new float[row];
            float[] lons = new float[col];
            for (int i = 0; i < row; i++) {
                lats[i] = gridData_new.getXStart() + gridData_new.getXDel() * i;
            }
            for (int i = 0; i < col; i++) {
                lons[i] = gridData_new.getYStart() + gridData_new.getYDel() * i;
            }
            gridData_new.setLatAry1D(lats);
            gridData_new.setLonAry1D(lons);
            return gridData_new;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 校验起始结束经纬度
     * @return
     */
    private static FreeArea getFreeAreaFromLon0(FreeArea freeArea){
        if(freeArea == null)
            return null;
        if(freeArea.getsLon() < 0)
            freeArea.setsLon(360 + freeArea.getsLon());
        if(freeArea.geteLon() < 0)
            freeArea.seteLon(360 + freeArea.geteLon());
        return freeArea;
    }

    /**
     * 校验起始结束经纬度
     * @return
     */
    private static GridData getGridDataFromLon0(GridData gridData){
        if(gridData == null)
            return null;
        float[] lons = gridData.getLonAry1D();
        if(lons == null){
            if(gridData.getLonAry2D() == null)
                return null;
            lons = gridData.getLonAry2D()[0];
        }

        if(lons[0] >= 0 && lons[lons.length - 1] >= 0)
            return gridData;
        int sRowCnt = gridData.getRowNum();
        int sColCnt = gridData.getColNum();
        float[][] newData = new float[sRowCnt][sColCnt];
        int index0 = -1;
        for(int i=0;i<lons.length;i++){
            if(lons[i] >= 0){
                index0 = i;
                break;
            }
        }
        for(int i=0;i<sRowCnt;i++){
            for(int j=index0; j<sColCnt + index0;j++){
                if(j>=sColCnt){//如果列的索引值大于网格的列数
                    newData[i][j] = gridData.getGridData()[i][j - sColCnt];
                }else{
                    newData[i][j] = gridData.getGridData()[i][j];
                }
            }
        }
        gridData.setGridData(newData);
        return gridData;
    }

    private static GridData getExchangeLat(GridData gridData){
        float[] lats = gridData.getLatAry1D();
        float[] newLats = new float[lats.length];
        for(int i=0;i<lats.length;i++){
            newLats[i] = lats[lats.length - i - 1];
        }

        gridData.setLatAry1D(newLats);
        float[][] dataAry = gridData.getGridData();
        float[][] new_dataAry = new float[gridData.getRowNum()][gridData.getColNum()];
        for(int i=0;i<gridData.getRowNum();i++){
            new_dataAry[i] = dataAry[gridData.getRowNum() - i - 1];
        }

        float ystart = gridData.getYStart();
        float yend = gridData.getYEnd();
        gridData.setYStart(yend);
        gridData.setYEnd(ystart);
        gridData.setGridData(new_dataAry);

        return gridData;
    }

}
