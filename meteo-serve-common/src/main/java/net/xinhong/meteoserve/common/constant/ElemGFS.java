package net.xinhong.meteoserve.common.constant;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/8/25.
 */
public enum ElemGFS {

    ICE("ICE","JB", "积冰"),
    TURB("TURB","DB","云中颠簸"),
    CAT("CAT","CAT", "晴空颠簸"),

    WS("WS","WS","风速"),
    WD("WD","WD", "风向"),
    TS("TS","TS", "雷暴落区"),
    VIS("VIS","VIS", "能见度"),
    CTH("CTH","CTH", "云顶高"),
    CBH("CBH","CBH", "云底高"),
    WSI("WSI","WSI", "下击暴流"),
    VWS("VWS","VWS", "垂直风切变"),
    ;
    private String ename, cname,fileCode;

    ElemGFS(String ename,String fileCode, String cname) {
        this.ename = ename;
        this.cname = cname;
        this.fileCode = fileCode;
    }

    public String getEname() {
        return ename;
    }


    public String getCname() {
        return cname;
    }

    public String getFileCode() {
        return fileCode;
    }

    public static ElemGFS fromFileCode(String fileCode){
        for (ElemGFS elemGFS : ElemGFS.values()) {
            if(elemGFS.getFileCode().equals(fileCode)){
                return elemGFS;
            }
        }
        return null;
    }

}
