package net.xinhong.meteoserve.common.constant;

import com.xinhong.mids3d.datareader.util.DataType;
import com.xinhong.mids3d.datareader.util.ElemCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/18.
 */
public class DataTypeElemConfig {
    static {
        //...
    }

    //GFS 全球危险天气图片层次
    public static List<String> getGFSSigmetLevelList(DataType dataType,ElemGFS elem){
        List<String> levelList = new ArrayList<>();
        if(dataType == DataType.GFS){
            if(elem == ElemGFS.TURB || elem == ElemGFS.ICE){
                levelList.add("0925");
                levelList.add("0850");
                levelList.add("0700");
                levelList.add("0600");
                levelList.add("0500");
                levelList.add("0400");
                levelList.add("0300");
                levelList.add("0200");

            }
        }

        return levelList;
    }

    public static List<String> getLevelListFromDataTypeElem(DataType datatype, ElemCode elem) {
        List<String> levelList = new ArrayList<>();
        if (datatype == DataType.MHWNI) {
            if (elem == ElemCode.HH) {
                levelList.add("0850");
                levelList.add("0700");
                levelList.add("0600");
                levelList.add("0500");
                levelList.add("0400");
                levelList.add("0300");
                levelList.add("0250");
                levelList.add("0200");
                levelList.add("0150");
                levelList.add("0100");
            } else if (elem == ElemCode.TT) {
                levelList.add("9999");
                levelList.add("0850");
                levelList.add("0700");
                levelList.add("0600");
                levelList.add("0500");
                levelList.add("0400");
                levelList.add("0300");
                levelList.add("0250");
                levelList.add("0200");
            } else if (elem == ElemCode.RH) {
                levelList.add("0850");
                levelList.add("0700");
                levelList.add("0600");
                levelList.add("0500");
            } else if (elem == ElemCode.UU || elem == ElemCode.VV) {
                // levelList.add("0850");
                // levelList.add("0700");
                //levelList.add("0600");
                levelList.add("0500");
                levelList.add("0400");
                //  levelList.add("0350");
                levelList.add("0300");
                // levelList.add("2700");
                // levelList.add("0250");
                // levelList.add("0230");
                levelList.add("0200");
                //levelList.add("0175");
                // levelList.add("0150");
                //levelList.add("0100");
            }
        } else if (datatype == DataType.GFS) {
            if (elem == ElemCode.HH || elem == ElemCode.TT
                    || elem == ElemCode.RH || elem == ElemCode.WS
                    || elem == ElemCode.UU || elem == ElemCode.VV) { //WS 是根据UU、VV 分量计算来的
                //  2016/10/27  新增加1000层次
                levelList.add("1000");
                levelList.add("0925");
                levelList.add("0850");
                levelList.add("0700");
                levelList.add("0600");
                levelList.add("0500");
                levelList.add("0400");
                levelList.add("0300");
                levelList.add("0200");
            }
            if (elem == ElemCode.PR || elem == ElemCode.RN ||  elem == ElemCode.TT) {
                levelList.add("9999");
            }
        }
        return levelList;
    }
}
