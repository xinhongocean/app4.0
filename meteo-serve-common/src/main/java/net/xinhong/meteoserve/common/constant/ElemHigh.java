package net.xinhong.meteoserve.common.constant;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/16.
 */
public enum ElemHigh {

    WD("WD", "风向"),
    WDF("WDF", "风向编码(16风向)"),
    WS("WS", "风速"),
    TT("TT", "气温"),
    TD("TD", "露点温度"),
    RH("RH", "相对湿度"),
    VO("VO", "绝对涡度"),
    WXT("WXT", "垂直速度"),
    HH("HH", "位势米"),
    HH10("HH10", "位势什米"),
    W_GPH("W_GPH", "填图GPH");

    private String ename, cname;

    ElemHigh(String ename, String cname) {
        this.ename = ename;
        this.cname = cname;
    }

    public String getEname() {
        return this.ename;
    }

    public String getCname() {
        return this.cname;
    }

}
