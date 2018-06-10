package net.xinhong.meteoserve.service.domain.origin;

/**
 * Created by wingsby on 2018/1/11.
 */
public class GTSPPData {
    private String format_version;
    private int gtspp_station_id;
    private String crc;
    private int best_quality_flag;
    private double time;
    private int time_quality_flag;
    private float latitude;
    private float longitude;
    private int position_quality_flag;
    private String qc_version;
    private String gtspp_temperature_instrument_code;
    private String gtspp_platform_code;
    private String data_type;
    private int one_deg_sq;
    private String cruise_id;
    private String source_id;
    private String stream_ident;
    private String uflag;
    private short no_prof;
    private String prof_type;
    private float z;
    private int z_variable_quality_flag;
    private float temperature;
    private int temperature_quality_flag;
    private float salinity;
    private int salinity_quality_flag;
    private short no_surf;
    private String surfacecodes_pcode;
    private String surfacecodes_cparm;
    private short no_hist;
    private String hist_identcode;
    private String hist_prccode;
    private String hist_version;
    private String hist_prcdate;
    private String hist_actcode;
    private String hist_actparm;
    private String hist_auxid;
    private String hist_ovalue;
    private int crs;


    public String getFormat_version() {
        return format_version;
    }

    public void setFormat_version(String format_version) {
        this.format_version = format_version;
    }

    public int getGtspp_station_id() {
        return gtspp_station_id;
    }

    public void setGtspp_station_id(int gtspp_station_id) {
        this.gtspp_station_id = gtspp_station_id;
    }

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    public int getBest_quality_flag() {
        return best_quality_flag;
    }

    public void setBest_quality_flag(int best_quality_flag) {
        this.best_quality_flag = best_quality_flag;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public int getTime_quality_flag() {
        return time_quality_flag;
    }

    public void setTime_quality_flag(int time_quality_flag) {
        this.time_quality_flag = time_quality_flag;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public int getPosition_quality_flag() {
        return position_quality_flag;
    }

    public void setPosition_quality_flag(int position_quality_flag) {
        this.position_quality_flag = position_quality_flag;
    }

    public String getQc_version() {
        return qc_version;
    }

    public void setQc_version(String qc_version) {
        this.qc_version = qc_version;
    }

    public String getGtspp_temperature_instrument_code() {
        return gtspp_temperature_instrument_code;
    }

    public void setGtspp_temperature_instrument_code(String gtspp_temperature_instrument_code) {
        this.gtspp_temperature_instrument_code = gtspp_temperature_instrument_code;
    }

    public String getGtspp_platform_code() {
        return gtspp_platform_code;
    }

    public void setGtspp_platform_code(String gtspp_platform_code) {
        this.gtspp_platform_code = gtspp_platform_code;
    }

    public String getData_type() {
        return data_type;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    public int getOne_deg_sq() {
        return one_deg_sq;
    }

    public void setOne_deg_sq(int one_deg_sq) {
        this.one_deg_sq = one_deg_sq;
    }

    public String getCruise_id() {
        return cruise_id;
    }

    public void setCruise_id(String cruise_id) {
        this.cruise_id = cruise_id;
    }

    public String getSource_id() {
        return source_id;
    }

    public void setSource_id(String source_id) {
        this.source_id = source_id;
    }

    public String getStream_ident() {
        return stream_ident;
    }

    public void setStream_ident(String stream_ident) {
        this.stream_ident = stream_ident;
    }

    public String getUflag() {
        return uflag;
    }

    public void setUflag(String uflag) {
        this.uflag = uflag;
    }

    public short getNo_prof() {
        return no_prof;
    }

    public void setNo_prof(short no_prof) {
        this.no_prof = no_prof;
    }

    public String getProf_type() {
        return prof_type;
    }

    public void setProf_type(String prof_type) {
        this.prof_type = prof_type;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public int getZ_variable_quality_flag() {
        return z_variable_quality_flag;
    }

    public void setZ_variable_quality_flag(int z_variable_quality_flag) {
        this.z_variable_quality_flag = z_variable_quality_flag;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public int getTemperature_quality_flag() {
        return temperature_quality_flag;
    }

    public void setTemperature_quality_flag(int temperature_quality_flag) {
        this.temperature_quality_flag = temperature_quality_flag;
    }

    public float getSalinity() {
        return salinity;
    }

    public void setSalinity(float salinity) {
        this.salinity = salinity;
    }

    public int getSalinity_quality_flag() {
        return salinity_quality_flag;
    }

    public void setSalinity_quality_flag(int salinity_quality_flag) {
        this.salinity_quality_flag = salinity_quality_flag;
    }

    public short getNo_surf() {
        return no_surf;
    }

    public void setNo_surf(short no_surf) {
        this.no_surf = no_surf;
    }

    public String getSurfacecodes_pcode() {
        return surfacecodes_pcode;
    }

    public void setSurfacecodes_pcode(String surfacecodes_pcode) {
        this.surfacecodes_pcode = surfacecodes_pcode;
    }

    public String getSurfacecodes_cparm() {
        return surfacecodes_cparm;
    }

    public void setSurfacecodes_cparm(String surfacecodes_cparm) {
        this.surfacecodes_cparm = surfacecodes_cparm;
    }

    public short getNo_hist() {
        return no_hist;
    }

    public void setNo_hist(short no_hist) {
        this.no_hist = no_hist;
    }

    public String getHist_identcode() {
        return hist_identcode;
    }

    public void setHist_identcode(String hist_identcode) {
        this.hist_identcode = hist_identcode;
    }

    public String getHist_prccode() {
        return hist_prccode;
    }

    public void setHist_prccode(String hist_prccode) {
        this.hist_prccode = hist_prccode;
    }

    public String getHist_version() {
        return hist_version;
    }

    public void setHist_version(String hist_version) {
        this.hist_version = hist_version;
    }

    public String getHist_prcdate() {
        return hist_prcdate;
    }

    public void setHist_prcdate(String hist_prcdate) {
        this.hist_prcdate = hist_prcdate;
    }

    public String getHist_actcode() {
        return hist_actcode;
    }

    public void setHist_actcode(String hist_actcode) {
        this.hist_actcode = hist_actcode;
    }

    public String getHist_actparm() {
        return hist_actparm;
    }

    public void setHist_actparm(String hist_actparm) {
        this.hist_actparm = hist_actparm;
    }

    public String getHist_auxid() {
        return hist_auxid;
    }

    public void setHist_auxid(String hist_auxid) {
        this.hist_auxid = hist_auxid;
    }

    public String getHist_ovalue() {
        return hist_ovalue;
    }

    public void setHist_ovalue(String hist_ovalue) {
        this.hist_ovalue = hist_ovalue;
    }

    public int getCrs() {
        return crs;
    }

    public void setCrs(int crs) {
        this.crs = crs;
    }
}
