package net.xinhong.meteoserve.service.common;

/**
 * Created by Administrator on 2016/5/6.
 */

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class SunTimes {
    static Double start = 0.0;
    static Double end = 0.0;
    static Double sRA = 0.0;
    static Double sdec = 0.0;
    static Double sr = 0.0;
    static Double lon = 0.0;
    private static int[] days_of_month_1 = {31, 28, 31, 30, 31, 30, 31, 31,
            30, 31, 30, 31};
    private static int[] days_of_month_2 = {31, 29, 31, 30, 31, 30, 31, 31,
            30, 31, 30, 31};

//    private int year = 2016;
//    private int month = 5;
//    private int day = 9;
//    //北京地区的经纬度
//    private double areaLon = 116.4009203787;
//    private double areaLat = 39.9032724643;

    //日出时间
    private String sunrise = null;
    //日落时间
    private String sunset = null;

    public SunTimes(int year, int month, int day, double longitude, double latitude) {
        long mydays = days(year, month, day);
        sunRiseSet(mydays, longitude, latitude, -35.0 / 60.0, 1, start, end);
        sunrise = ToLocalTime(start);
        sunset = ToLocalTime(end);
    }

    public SunTimes(String year, String month, String day, double longitude, double latitude) {
        long mydays = days(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        sunRiseSet(mydays, longitude, latitude, -35.0 / 60.0, 1, start, end);
        sunrise = ToLocalTime(start);
        sunset = ToLocalTime(end);
    }


    // 判断是否为闰年：若为闰年，返回1；若不是闰年,返回0
    public static boolean leap_year(int year) {
        if (((year % 400 == 0) || (year % 100 != 0) && (year % 4 == 0)))
            return true;
        else
            return false;
    }

    // 求从格林威治时间公元2000年1月1日到计算日天数days
    public static int days(int year, int month, int day) {
        int i, a = 0;
        for (i = 2000; i < year; i++) {
            if (leap_year(i))
                a = a + 366;
            else
                a = a + 365;
        }
        if (leap_year(year)) {
            for (i = 0; i < month - 1; i++) {
                a = a + days_of_month_2[i];
            }
        } else {
            for (i = 0; i < month - 1; i++) {
                a = a + days_of_month_1[i];
            }
        }
        a = a + day;
        return a;
    }

    public List<String> GetSunTime(long day, Double longitude,
                                   Double latitude) throws ParseException {
        List<String> retList = new ArrayList<String>();
        sunRiseSet(day, longitude, latitude, -35.0 / 60.0, 1, start, end);
        sunrise = ToLocalTime(start);
        sunset = ToLocalTime(end);
        retList.add("日出" + sunrise);
        retList.add("日落" + sunset);
        return retList;
    }

    private static String ToLocalTime(Double utTime) {
        int hour = (int) (Math.floor(utTime));
        Double temp = utTime - hour;
        hour += 8;

        temp = temp * 60;
        int minute = (int) (Math.floor(temp));
        String minuteStr = minute + " ";
        if (minute < 10) {
            minuteStr = "0" + minute;
        }
        String hourStr = hour+"";
        if (hour<10)
            hourStr = "0"+hour;;
        return hourStr + ":" + minuteStr;
    }

    private static void Sunpos(Double d, Double lon, Double r) {
        Double M, // 太阳的平均近点角，从太阳观察到的地球（=从地球看到太阳的）距近日点（近地点）的角度。
                w, // 近日点的平均黄道经度。
                e, // 地球椭圆公转轨道离心率。
                E, // 太阳的偏近点角。计算公式见下面。

                x, y, v; // 真近点角，太阳在任意时刻的真实近点角。

        M = Revolution(356.0470 + 0.9856002585 * d);// 自变量的组成：2000.0时刻太阳黄经为356.0470度,此后每天约推进一度（360度/365天
        w = 282.9404 + 4.70935E-5 * d;// 近日点的平均黄经。

        e = 0.016709 - 1.151E-9 * d;// 地球公转椭圆轨道离心率的时间演化。以上公式和黄赤交角公式一样，不必深究。

        E = M + e * Radge * Sind(M) * (1.0 + e * Cosd(M));
        x = Cosd(E) - e;
        y = Math.sqrt(1.0 - e * e) * Sind(E);
        setSr(Math.sqrt(x * x + y * y));
        v = Atan2d(y, x);
        lon = v + w;
        setLon(lon);
        if (lon >= 360.0) {
            lon -= 360.0;
            setLon(lon);
        }
    }

    private static void Sun_RA_dec(Double d, Double RA, Double dec, Double r) {
        Double obl_ecl, x, y, z;
        Sunpos(d, lon, r);
        // 计算太阳的黄道坐标。
        x = sr * Cosd(lon);
        y = sr * Sind(lon);
        // 计算太阳的直角坐标。
        obl_ecl = 23.4393 - 3.563E-7 * d;
        // 黄赤交角，同前。
        z = y * Sind(obl_ecl);
        y = y * Cosd(obl_ecl);
        // 把太阳的黄道坐标转换成赤道坐标（暂改用直角坐标）。

        setsRA(Atan2d(y, x));
        setSdec(Atan2d(z, Math.sqrt(x * x + y * y)));
        // 最后转成赤道坐标。显然太阳的位置是由黄道坐标方便地直接确定的，但必须转换到赤
        // 道坐标里才能结合地球的自转确定我们需要的白昼长度。

    }

    private static int sunRiseSet(long day, Double longitude, Double lat,
                                  Double altit, int upper_limb, Double trise, Double tset) {
        Double d, /* Days since 2000 Jan 0.0 (negative before) */
                // 以历元2000.0起算的日数。

                sradius, /* Sun's apparent radius */
                // 太阳视半径，约16分（受日地距离、大气折射等诸多影响）

                t, /* Diurnal arc */
                // 周日弧，太阳一天在天上走过的弧长。

                tsouth, /* Time when Sun is at south */
                sidtime; /* Local sidereal time */
        // 当地恒星时，即地球的真实自转周期。比平均太阳日（日常时间）长3分56秒。

        int rc = 0; /* Return cde from function - usually 0 */

		/* Compute d of 12h local mean solar time */
        d = day/* Days_since_2000_Jan_0(date) */ + 0.5 - longitude / 360.0;
        // 计算观测地当日中午时刻对应2000.0起算的日数。

		/* Compute local sideral time of this moment */
        sidtime = Revolution(GMST0(d) + 180.0 + longitude);
        // 计算同时刻的当地恒星时（以角度为单位）。以格林尼治为基准，用经度差校正。

		/* Compute Sun's RA + Decl at this moment */
        Sun_RA_dec(d, sRA, sdec, sr);
        // 计算同时刻太阳赤经赤纬。

		/* Compute time when Sun is at south - in hours UT */
        tsouth = 12.0 - Rev180(sidtime - sRA) / 15.0;
        // 计算太阳日的正午时刻，以世界时（格林尼治平太阳时）的小时计。

		/* Compute the Sun's apparent radius, degrees */
        sradius = 0.2666 / sr;
        // 太阳视半径。0.2666是一天文单位处的太阳视半径（角度）。

		/* Do correction to upper limb, if necessary */
        if (upper_limb != 0)
            altit -= sradius;
        // 如果要用上边缘，就要扣除一个视半径。

		/* Compute the diurnal arc that the Sun traverses to reach */
        // 计算周日弧。直接利用球面三角公式。如果碰到极昼极夜问题，同前处理。
        /* the specified altitide altit: */

        Double cost;
        cost = (Sind(altit) - Sind(lat) * Sind(sdec))
                / (Cosd(lat) * Cosd(sdec));
        if (cost >= 1.0) {
            rc = -1;
            t = 0.0;
        } else {
            if (cost <= -1.0) {
                rc = +1;
                t = 12.0; /* Sun always above altit */
            } else
                t = Acosd(cost) / 15.0; /* The diurnal arc, hours */
        }

		/* Store rise and set times - in hours UT */
        setStart(tsouth - t);
        setEnd(tsouth + t);
        return rc;
    }

	/*
     * private static long Days_since_2000_Jan_0(Date date) { String d2000 =
	 * "2000-01-01"; SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	 * Long ll = 0l; try { ll = date.getTime()-sdf.parse(d2000).getTime(); }
	 * catch (ParseException e) { e.printStackTrace(); } long day =
	 * ll/1000/60/60/24; return day; }
	 */

    private static Double Revolution(Double x) {
        return (x - 360.0 * Math.floor(x * Inv360));
    }

    private static Double Rev180(Double x) {
        return (x - 360.0 * Math.floor(x * Inv360 + 0.5));
    }

    private static Double GMST0(Double d) {
        Double sidtim0;
        sidtim0 = Revolution((180.0 + 356.0470 + 282.9404)
                + (0.9856002585 + 4.70935E-5) * d);
        return sidtim0;
    }

    private static Double Inv360 = 1.0 / 360.0;

    private static Double Sind(Double x) {
        return Math.sin(x * Degrad);
    }

    private static Double Cosd(Double x) {
        return Math.cos(x * Degrad);
    }

	/*
	 * private static Double Tand(Double x) { return Math.tan(x * Degrad);
	 *
	 * }
	 *
	 * private static Double Atand(Double x) { return Radge * Math.atan(x); }
	 *
	 * private static Double Asind(Double x) { return Radge * Math.asin(x); }
	 */

    private static Double Acosd(Double x) {
        return Radge * Math.acos(x);
    }

    private static Double Atan2d(Double y, Double x) {
        return Radge * Math.atan2(y, x);

    }

    private static Double Radge = 180.0 / Math.PI;
    private static Double Degrad = Math.PI / 180.0;

    public static Double getStart() {
        return start;
    }

    public static void setStart(Double start) {
        SunTimes.start = start;
    }

    public static Double getsRA() {
        return sRA;
    }

    public static void setsRA(Double sRA) {
        SunTimes.sRA = sRA;
    }

    public static Double getSdec() {
        return sdec;
    }

    public static void setSdec(Double sdec) {
        SunTimes.sdec = sdec;
    }

    public static Double getSr() {
        return sr;
    }

    public static void setSr(Double sr) {
        SunTimes.sr = sr;
    }

    public static Double getLon() {
        return lon;
    }

    public static void setLon(Double lon) {
        SunTimes.lon = lon;
    }

    public static Double getEnd() {
        return end;
    }

    public static void setEnd(Double end) {
        SunTimes.end = end;
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }

}