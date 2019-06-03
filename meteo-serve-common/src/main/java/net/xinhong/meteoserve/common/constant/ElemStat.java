package net.xinhong.meteoserve.common.constant;

/**
 * Description:统计产品 <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/16.
 */
public enum ElemStat {

    AVGT("AVGT",    "平均气温"),
    AVGMXT("AVGMXT",    "平均最高气温"),
    AVGMIT("AVGMIT",    "平均最低气温"),
    AVGVP("AVGVP",  "平均水汽压"),
   // RN20("RN20",    "20-20时降水量"),
    AVGRND("AVGRND",    "08-08时降水量"),
    AVGWS("AVGWS",  "平均风速"),

    AVGRN("AVGRN",    "降水量"),

    AVGPR("AVGPR",    "平均海平面气压"),
    MMXT("MMXT",      "极端最高气温"),
    MMXTYD("MMXTYD",    "极端最高气温出现年份日期"),
    MMIT("MMIT",        "极端最低气温"),
    MMITYD("MMITYD",    "极端最低气温出现年份日期"),
    AVGRH("AVGRH",      "平均相对湿度"),
    MXRN("MXRN",        "最多降水量"),
    MXDRN("MXDRN",      "最大日降水量"),
    MXDRNYD("MXDRNYD",  "最大日降水量出现年份日期"),
    MXWS("MXWS",        "极大风速 "),
    MXWD("MXWD",        "极大风速的风向"),
    MXWSYD("MXWSYD",    "极大风速出现年份日期"),
    ;

    private String ename, cname;

    ElemStat(String ename, String cname) {
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
