package net.xinhong.meteoserve.common.constant;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/7/25.
 */
public class GFSVTIConfig {


    public static boolean isNeedVTI(int vti){
        /*if((vti>=0 && vti<6) || (vti>=48 && vti<=96)){
            if(vti % 3 == 0){
                return true;
            }else{
                return false;
            }
        }else if(vti>=6 && vti<48){
            return true;
        }
        return false ;*/

        if(vti>=72 && vti<=96){
            if(vti % 3 == 0){
                return true;
            }else{
                return false;
            }
        }else if(vti<72){
            return true;
        }
        return false;

    }
}
