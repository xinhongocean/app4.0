package net.xinhong.meteoserve.common.constant;

/**
 * Created by shijunna on 2016/7/22.
 */
public enum StatDate {
    Day("Day", "日统计"),
    Period("Period", "旬统计"),
    Month("Month", "月统计");

    private String ename, cname;

    StatDate(String ename, String cname) {
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
