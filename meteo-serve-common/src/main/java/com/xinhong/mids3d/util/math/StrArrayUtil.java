package com.xinhong.mids3d.util.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * 数组工具类
 * */
public class StrArrayUtil {
	/**
	 * 返回指定列数据唯一的二维数组
	 */
	public static String[][] uniqueRowAsColumns(String[][]data,Integer[]index){
		//总列数 */
		int columns = data[0].length;
		//需要检查重复的列*/
		List<Integer> check_col = Arrays.asList(index);
		int row_c_counts = check_col.size();
		int row_p_counts = columns-check_col.size();
		//存储经筛选选取的数据
		LinkedHashMap<String, String[]> result = new LinkedHashMap<String, String[]>();
		//分割标记 
		String splitstr = "@xh@";
		//把检查重复的列和检查null（排列优先级）的列分离出来
		for(String[]row:data){
			String row_c = ""; // 迭代每一行数据中，需要检测重复的列（数据字符串拼接）
			String []row_p = new String[row_p_counts];
			int check_col_index = 0;// 判断重复列索引
			int row_p_index = 0;// 判断优先级列索引
			for(int i=0;i<columns;i++){ //分离数组数据
				int n = -1;
				if(check_col_index<row_c_counts){
					n = check_col.get(check_col_index);
				}
				if(n!=i){ // 若不是判断唯一的列，则是需要判断""的列（判断优先级）
					row_p[row_p_index]=row[i];
					row_p_index++;
				}else{ // 判断唯一的列
					row_c+=row[i]+splitstr;
					check_col_index++;
				}
			}
			//过滤 对比流程
			if(!result.containsKey(row_c)){
				//直接加入
				result.put(row_c, row_p);
			}else{
				//判断唯一的列数据存在相同的情况，需要判断其他列，进行优先选择
				String []compare_atrr1 = result.get(row_c);
				//row_p
				int P1=0; // 优先级标识1
				int P2=0; // 优先级标识2 
				int X=0; // 空值交叉判断标识
				// 当前的优先级判断数据为 row_p
				for (int i = 0; i < row_p_counts; i++) {
					if(compare_atrr1[i].equals("")){
						P1++;
					}
					if(row_p[i].equals("")){
						P2++;
					}
					if(compare_atrr1[i].equals("")&&!row_p[i].equals("")){
						X++;
					}
					if(!compare_atrr1[i].equals("")&&row_p[i].equals("")){
						X++;
					}
				}
				//System.out.println(P1+":"+P2+"-"+X);
				if(P1>P2){
					//System.out.println("对比优先级后交换");
					// 替换
					result.remove(row_c);
					result.put(row_c, row_p);
				}else if(P1==P2){
					//System.out.println("优先级相等！");
					if(X>0){
						//更复杂的优先级判断
						//System.out.println("对比优先级后交换");
						// 加入
					}
				}
			}
		}
		String [][]resultArray = new String[result.size()][]; // 返回结果数组
		int result_row_count = 0;
		Set<String> parts1 = result.keySet();
		for (String part1Str : parts1) { // 组装数组 循环体
			String[] part1 = part1Str.split(splitstr);
			String[] part2 = result.get(part1Str);
			resultArray[result_row_count] = new String[columns];
			int part1_index=0;
			int part2_index=0;
			for(int i=0;i<columns;i++){ // 将分离后的数据 重新组合成数组数据
				if(check_col.contains(i)){
					resultArray[result_row_count][i] = part1[part1_index++];
				}else{
					resultArray[result_row_count][i] = part2[part2_index++];
				}
			}
			result_row_count++;
		}
		return resultArray;
	}
	
	/**
	 * 通过指定列索引(多个)以及不期望出现的值(多个)，过滤掉相应的记录
	 * @param data 处理的数组
	 * @param index 索引...
	 * @param values 值...
	 * @return 过滤后的数组
	 */
	public static String[][] arrayFilter(String[][]data,Integer[]index,String [] values) {
		List<String[]> resultList = new ArrayList<String[]>();
		for(String []row:data){
			int equation_count=0;
			for(int i=0;i<index.length;i++){
				int filter_index=index[i];
				if(row[filter_index].equals(values[i])){
					equation_count++;
				}
			}
			if(equation_count!=index.length){
				resultList.add(row);
			}
		}
		String [][]resultArray = new String[resultList.size()][];
		for (int i = 0; i < resultArray.length; i++) {
			resultArray[i]=resultList.get(i);
		}
		return resultArray;
	}
	

	/**
	 * 获取二维数组的最小最大值
	 * @return 最小值 最大值
	 */
	public static float[] getMinMaxVal(float[][] data) {
		float minVal = data[0][0];
		float maxVal = data[0][0];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				if (minVal > data[i][j]) {
					minVal = data[i][j];
				}
				if (maxVal < data[i][j]) {
					maxVal = data[i][j];
				}
			}
		}
		//慢
//		for(float[] rowData: data){
//		    Arrays.sort(rowData);
//		    if(minVal>rowData[0]){
//			   minVal = rowData[0];
//		    }
//		    if(maxVal<rowData[rowData.length-1]){
//			   maxVal = rowData[rowData.length-1];
//		    }
//	    }
		return new float[] { minVal, maxVal };
	}
	

	/**
	 * 获取二维数组出去非正常值unnormalVal以外的最小最大值
	 * @param data 数组
	 * @param unnormalVal 计算时忽略的值
	 * @return 最小值 最大值
	 */
	public static float[] getMinMaxVal(float[][] data, float unnormalVal) {
		float minVal = Float.MAX_VALUE;
		float maxVal = Float.MIN_VALUE;
		boolean flag = false;
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				if (data[i][j] == unnormalVal) {
					continue;
				} else {
					if (!flag) {
						minVal = data[i][j];
						maxVal = data[i][j];
						flag = true;
					}
				}
				if (minVal > data[i][j]) {
					minVal = data[i][j];
				}
				if (maxVal < data[i][j]) {
					maxVal = data[i][j];
				}
			}
		}
		return new float[] { minVal, maxVal };
	}

	/**
	 * 获取一维数组的最小最大值
	 * @param data 一维数组
	 * @return 最小值 最大值
	 */
	public static float[] getMinMaxVal(float[] data) {
		float minVal = data[0];
		float maxVal = data[0];
		for (int i = 0; i < data.length; i++) {
			if (minVal>data[i]) {
				minVal = data[i];
			}
			if (maxVal<data[i]) {
				maxVal = data[i];
			}
		}
		return new float[] { minVal, maxVal};
	}
	
	/**
	 * 获取一维数组的最小最大值
	 * @param data 一维数组
	 * @return 最小值 最大值
	 */
	public static int[] getMinMaxVal(int[] data) {
		int minVal = data[0];
		int maxVal = data[0];
		for (int i = 0; i < data.length; i++) {
			if (minVal>data[i]) {
				minVal = data[i];
			}
			if (maxVal<data[i]) {
				maxVal = data[i];
			}
		}
		return new int[] { minVal, maxVal};
	}
	
	/**
	 * 根据起止(坐标)值和间隔获取一维数组
	 * @param start 起始值
	 * @param end 结束值
	 * @param delta 间隔值
	 * @return 一维数组
	 */
	public static float[] getArrFromSE(float start, float end, float delta) {
		int rowcol = (int) (Math.abs(end - start) / delta + 1);
		float[] resArr = new float[rowcol];
		for (int i = 0; i < rowcol; i++) {
			resArr[i] = start + delta * i;
		}
		return resArr;
	}

	/**
	 * 根据起止(坐标)值和 行或列获取一维数组
	 * @param start 起始值
	 * @param end 结束值
	 * @param rowcol 行或列
	 * @return 一维数组
	 */
	public static float[] getArrFromSE(float start, float end, int rowcol) {
		float[] resArr = new float[rowcol];
		float delta = Math.abs(end - start) / (rowcol - 1);
		for (int i = 0; i < rowcol; i++) {
			resArr[i] = start + delta * i;
		}
		return resArr;
	}

	/**
	 * 根据起止(坐标)值和行列数获取二维数组
	 * @param start 起始值
	 * @param end 结束值
	 * @param delta 间隔值
	 * @param row 行
	 * @param col 列
	 * @param isRow 是否是行(横向 每行都相同；列：每列都相同
	 * @return 二维数组
	 */
	public static float[][] getArrFromSERowCol(float start, float end, float delta, int row, int col, boolean isRow) {
		float[][] resArr = new float[row][col];
		if (isRow) {
			for (int i = 0; i < row; i++) {
				for (int j = 0; j < col; j++) {
					resArr[i][j] = start + delta * j;
				}
			}
		} else {
			for (int i = 0; i < row; i++) {
				for (int j = 0; j < col; j++) {
					resArr[i][j] = start + delta * i;
				}
			}
		}
		return resArr;
	}
	
	/**
	 * 获取数组的某一行
	 * @param data 数组
	 * @param row 行数
	 * @return 数组的某一行
	 */
	public static float[] getOneRowFromArray(float[][] data, int row){
		int dataRow = data.length;
		int dataCol = data[0].length;
		if(row>dataRow-1 || row<0){
			System.out.println("获取二维数组第"+row+"行出错，超出范围");
			return null;
		}
		float[] resArr = new float[dataCol];
		for(int j=0; j<dataCol; j++){
			resArr[j] = data[row][j];
		}
		return resArr;
	}
	
	/**
	 * 获取数组的某一列
	 * @param data 数组
	 * @param col 列数
	 * @return 数组的某一列
	 */
	public static float[] getOneColFromArray(float[][] data, int col){
		int dataRow = data.length;
		int dataCol = data[0].length;
		if(col>dataCol-1 || col<0){
			System.out.println("获取二维数组第"+col+"列出错，超出范围");
			return null;
		}
		
		float[] resArr = new float[dataRow];
		for(int i=0; i<dataRow; i++){
			resArr[i] = data[i][col];
		}
		return resArr;
	}
	
	/**
	 * val是否存在数组data中，若存在返回存在的行列数
	 * @param data 数组
	 * @param val 某一个值
	 * @return 存在的行列数，若不存在返回null
	 */
	public static int[] inArrayRowCol(float[][] data, float val){
		int[] rowcol = new int[2];
		if(isInArray(data, val)){
			int row = data.length;
			int col = data[0].length;
			for(int i=0; i<row; i++){
				for(int j=0; j<col; j++){
					if(val==data[i][j]){
						rowcol[0] = i;
						rowcol[1] = j;
						break;
					}
				}
			}
		}else{
//			System.out.println(val+"不存在二维数组中");
			return null;
		}
		return rowcol;
	}
	
	/**
	 * val是否存在一维数组data中，若存在返回存在的行数
	 * @param data 数组
	 * @param val 某一个值
	 * @return 返回存在的列数，若不存在则返回99999
	 */
	public static int inArrayCol(float[] data, float val){
		if(isInArray(data, val)){
			for(int i=0; i<data.length; i++){
				if(val == data[i]){
					return i;
				}
			}
		}
//		System.out.println(val+"不存在一维数组中");
		return 99999;
	}
	
	/**
	 * val是否存在数组data中
	 * @param data 数组
	 * @param val 某一值
	 * @return 存在true，不存在false
	 */
	public static boolean isInArray(float[][] data, float val){
		int row = data.length;
		int col = data[0].length;
		for(int i=0; i<row; i++){
			for(int j=0; j<col; j++){
				if(val == data[i][j]){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * val是否存在数组data中
	 * @param data 数组
	 * @param val 某一值
	 * @return 存在true，不存在false
	 */
	public static boolean isInArray(float[] data, float val){
		int col = data.length;
		for(int j=0; j<col; j++){
			if(val == data[j]){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 旋转或转置数组
	 * @param data 二维数组
	 * @param direction 0：不旋转，不转置；
	 * 1：旋转90度，不转置；2：旋转180度，不转置；
	 * 3：旋转270度，不转置；4：不旋转，转置；
	 * 5：旋转90度，转置；6：旋转180度，转置；
	 * 7：旋转270度，转置；其他：返回原值
	 * @return 旋转转置后的数组
	 */
	public static float[][] rotateArray(float[][] data, int direction){
		if(data==null){
			return null;
		}
		float[][] newdata = null;
		int oldRow = data.length;
		int oldCol = data[0].length;
		int newRow = 0;
		int newCol = 0;
		switch(direction){
		case 1://不转置，旋转90度[x0][y0]--->[y0][-x0] = [y0][newCol-x0-1]
			newRow = oldCol;
			newCol = oldRow;
			newdata = new float[newRow][newCol];
			for(int i=0; i<oldRow; i++){
				for(int j=0; j<oldCol; j++){
					newdata[j][newCol-i-1] = data[i][j];
				}
			}
			break;
		case 2://不转置，旋转180度[x0][y0]--->[-x0][-y0] = [newRow-x0-1][newCol-y0-1]
			newRow = oldRow;
			newCol = oldCol;
			newdata = new float[newRow][newCol];
			for(int i=0; i<oldRow; i++){
				for(int j=0; j<oldCol; j++){
					newdata[newRow-i-1][newCol-j-1] = data[i][j];
				}
			}
			break;
		case 3://不转置，旋转270度[x0][y0]--->[-y0][x0] = [newRow-y0-1][x0]
			newRow = oldCol;
			newCol = oldRow;
			newdata = new float[newRow][newCol];
			for(int i=0; i<oldRow; i++){
				for(int j=0; j<oldCol; j++){
					newdata[newRow-j-1][i] = data[i][j];
				}
			}
			break;
		case 4://转置(行变成列，列变成行)，不旋转[x0][y0]--->[y0][x0]
			newRow = oldCol;
			newCol = oldRow;
			newdata = new float[newRow][newCol];
			for(int i=0; i<oldRow; i++){
				for(int j=0; j<oldCol; j++){
					newdata[j][i] = data[i][j];
				}
			}
			break;
		case 5://转置，旋转90度[x0][y0]--->[x0][-y0]=[][]
			newRow = oldRow;
			newCol = oldCol;
			newdata = new float[newRow][newCol];
			for(int i=0; i<oldRow; i++){
				for(int j=0; j<oldCol; j++){
					newdata[i][newCol-j-1] = data[i][j];
				}
			}
			break;
		case 6://转置，旋转180度[x0][y0]--->[-y0][-x0]=[][]
			newRow = oldCol;
			newCol = oldRow;
			newdata = new float[newRow][newCol];
			for(int i=0; i<oldRow; i++){
				for(int j=0; j<oldCol; j++){
					newdata[newRow-j-1][newCol-i-1] = data[i][j];
				}
			}
			break;
		case 7://转置，旋转270度[x0][y0]--->[-x0][y0]=[][]
			newRow = oldRow;
			newCol = oldCol;
			newdata = new float[newRow][newCol];
			for(int i=0; i<oldRow; i++){
				for(int j=0; j<oldCol; j++){
					newdata[newRow-i-1][j] = data[i][j];
				}
			}
			break;
		default:
				return data;
		}
		return newdata;
	}
	
	/**
	 * 转置
	 * @param data 二维数组
	 * @return 转置后的数组  列变行， 行变列 同：rotateArray(data,4)
	 */
	public static float[][] transposeArray(float[][] data){
		if(data==null){
			return null;
		}
		float[][] newdata = null;
		int oldRow = data.length;
		int oldCol = data[0].length;
		int newRow = oldCol;
		int newCol = oldRow;
		newdata = new float[newRow][newCol];
		for(int i=0; i<oldRow; i++){
			for(int j=0; j<oldCol; j++){
				newdata[j][i] = data[i][j];
			}
		}
		return newdata;
	}
	
//	public static float[][] reformArray(float[][] data){
//	}
//	public static float[][] shiftArray(float[][] data){
//	}
	
	/**
	 * 倒转数组
	 * @param data 二维数组
	 * @param subscript 1or2。 1：交换列，第一列变最后一列，依次类推；2：交换行，第一行变最后一行，依次类推
	 * @return 倒转后的数组
	 */
	public static float[][] reverseArray(float[][] data, int subscript){
		if(data==null){
			return null;
		}
	    if(subscript==1){
	    	return rotateArray(data, 5);
	    }else if(subscript==2){
	    	return rotateArray(data, 7);
	    }else{
	    	System.out.println("不能倒转");
	    	return data;
	    }
	}
	
	  /**
     * 删除二维数组中的某行
     * @param ary 二维数组
     * @param index 需要删除的行数
     * @return 处理后的二维数组
     */
	public static float[][] removeRowByIndex(float[][] ary, int index){
    	if(ary == null)
    		return null;
    	if(ary.length <1)
    		return null;
    	float[][] newAry = new float[ary.length-1][ary[0].length];
    	for(int i=0; i<ary.length; i++){
    		for(int j=0; j<ary[0].length; j++){
    			if(i < index){
    				newAry[i][j] = ary[i][j];
    			}
    			if(i == index){
    				break;
    			}
    			if(i > index){
    				newAry[i-1][j] = ary[i][j];
    			}
    		}
    	}
    	return newAry;
    }
	
	/**
	 * 删除二维数组中的某行
     * @param ary 二维数组
     * @param indexAry 需要删除的行数
     * @return 处理后的二维数组
     */
	public static float[][] removeRowByIndexAry(float[][] ary, int[] indexAry){
    	if(ary == null)
    		return null;
    	if(ary.length <1)
    		return null;
    	float[][] newAry = new float[ary.length -indexAry.length][ary[0].length];
    	float[][] tmpAry = ary;
    	for(int i=0; i<indexAry.length; i++){
    		int index = indexAry[i]-i;
    		tmpAry = removeRowByIndex(tmpAry, index);
    	}
    	newAry = tmpAry;
    	return newAry;
    }
	
	/**
     * 删除二维数组中的某行
     * @param ary 二维数组
     * @param indexList 需要删除的行数
     * @return 处理后的二维数组
     */
	public static float[][] removeRowByIndexList(float[][] ary, ArrayList<Integer> indexList){
    	if(ary == null)
    		return null;
    	if(ary.length <1)
    		return null;
    	int[] indexAry = new int[indexList.size()];
    	for(int i=0; i<indexList.size(); i++){
    		indexAry[i] = indexList.get(i);
    	}
    	return removeRowByIndexAry(ary, indexAry);
    }
	
	 /**
     * 删除二维数组中的某列
     * @param ary 二维数组
     * @param index 需要删除的列数
     * @return 处理后的二维数组
     */
	public static float[][] removeColByIndex(float[][] ary, int index){
    	if(ary == null)
    		return null;
    	if(ary.length <1)
    		return null;
    	float[][] newAry = new float[ary.length][ary[0].length-1];
    	for(int j=0; j<ary[0].length; j++){
    		for(int i=0; i<ary.length; i++){
    			if(j < index)
    				newAry[i][j] = ary[i][j];
    			if(j == index)
    				break;
    			if(j > index)
    				newAry[i][j-1] = ary[i][j];
    		}
    	}
    	return newAry;
    }
	
	/**
     * 删除二维数组中的某列
     * @param ary 二维数组
     * @param indexAry 需要删除的列数
     * @return 处理后的二维数组
     */
	public static float[][] removeColByIndexAry(float[][] ary, int[] indexAry){
    	if(ary == null)
    		return null;
    	if(ary.length <1)
    		return null;
    	float[][] newAry = new float[ary.length][ary[0].length -indexAry.length];
    	float[][] tmpAry = ary;
    	for(int i=0; i<indexAry.length; i++){
    		int index = indexAry[i]-i;
    		tmpAry = removeColByIndex(tmpAry, index);
    	}
    	newAry = tmpAry;
    	return newAry;
    }
	
	/**
     * 删除二维数组中的某列
     * @param ary 二维数组
     * @param indexList 需要删除的列数 
     * @return 处理后的二维数组
     */
	public static float[][] removeColByIndexList(float[][] ary, ArrayList<Integer> indexList){
    	if(ary == null)
    		return null;
    	if(ary.length <1)
    		return null;
    	int[] indexAry = new int[indexList.size()];
    	for(int i=0; i<indexList.size(); i++){
    		indexAry[i] = indexList.get(i);
    	}
    	return removeColByIndexAry(ary, indexAry);
    }
	
	/**
	 * 从数组1中去掉与数组2相同的元素	
	 * @param arr1 减数
	 * @param arr2 被减数
	 * @return 
	 */
	public static int[] arrSubtractArr(int[] arr1, int[] arr2){
		if(arr1==null || arr2==null){
			return arr1;
		}
		int size = arr1.length - arr2.length;
		int[] resArr = new int[size];
		int t=0;
		boolean isSame = false;
		for(int i=0; i<arr1.length; i++){
			isSame = false;
			for(int j=0; j<arr2.length; j++){
				if(arr2[j]==arr1[i]){
					isSame = true;
					break;
				}
			}
			if(!isSame){
				resArr[t++] = arr1[i];
			}
		}
		
		return resArr;
	}
	
	/**
	 * 两个二维数组是否完全相等
	 * @param val1
	 * @param val2
	 * @return
	 */
	public static boolean isValEqual(String[][] val1, String[][] val2){
		if(val1.length!=val2.length){
			return false;
		}
		if(val1[0].length!=val2[0].length){
			return false;
		}
		for(int i=0; i<val1.length; i++){
			for(int j=0; j<val1[0].length; j++){
				if(!val1[i][j].equals(val2[i][j])){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 两个二维数组是否完全相等
	 * @param val1
	 * @param val2
	 * @return
	 */
	public static boolean isValEqual(float[][] val1, float[][] val2){
		if(val1.length!=val2.length){
			return false;
		}
		if(val1[0].length!=val2[0].length){
			return false;
		}
		for(int i=0; i<val1.length; i++){
			for(int j=0; j<val1[0].length; j++){
				if(val1[i][j]!=val2[i][j]){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 两个一维数组是否完全相等
	 * @param val1
	 * @param val2
	 * @return
	 */
	public static boolean isValEqual(String[] val1, String[] val2){
		if(val1.length!=val2.length){
			return false;
		}
		for(int i=0; i<val1.length; i++){
			if(!val1[i].equals(val2[i])){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 两个一维数组是否完全相等
	 * @param val1
	 * @param val2
	 * @return
	 */
	public static boolean isValEqual(float[] val1, float[] val2){
		if(val1.length!=val2.length){
			return false;
		}
		for(int i=0; i<val1.length; i++){
			if(val1[i]!=val2[i]){
				return false;
			}
		}
		return true;
	}
	
}
