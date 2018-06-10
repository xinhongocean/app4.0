package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.weixin.SHA1;
import net.xinhong.meteoserve.service.dao.SecurityDao;
import net.xinhong.meteoserve.service.service.SecurityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

/**
 * Created by xiaoyu on 16/9/20.
 */
@Service
public class SecurityServiceImpl implements SecurityService {
    private static final Log logger = LogFactory.getLog(SecurityServiceImpl.class);

    private static final String APPCLIENTINFOKEY  = "security.clientinfo";
  //  private static final String LATESTVERSIONNUM = "2.61";
    private static final String LATESTVERSIONNUM = "3.1";
  //  private static final String LATESTVERSIOINFO = "春节快乐!2.5.2版本更新内容:\n 1.全国pm2.5未来变化趋势分布图; \n 2.增加欧洲预报产品图片显示;\n 3.增加日本传真图显示;\n 4.调整UI界面.";
  //  private static final String LATESTVERSIOINFO = "2.8.1版本更新内容(如更新失败,可去各应用商店更新):\n 1.新增葵花8云图二级产品平面图及任意位置的时空剖面图; \n 2.新增手工交互标绘功能;\n 3.新增民航机场跑道方向、机场起降条件等;\n 4.调整机场实况报文显示.";
  //  private static final String LATESTVERSIOINFO = "3.0版本更新内容(如更新失败,可去各应用商店更新):\n 1.新增全国单站雷达图显示; \n 2.新增中国及周边6小时及24小时实况降水;\n 3.新增航空危险产品显示：雷暴概率、能见度、云底高等;\n 4.增加台风历史资料显示.";
    private static final String LATESTVERSIOINFO = "3.1版本更新内容(如更新失败,可去各应用商店更新):\n 1.新增实况填图功能; \n 2.新增地图个性化设置功能;\n 3.新增数值产品部分要素;\n 4.增加部分单站雷达图及机场报文查询.";
    //  private static final String LATESTVERSIOINFO = "";
    private static final String MINVERSIONNUM = "2.8";

    @Resource
    private JedisCluster jedisCluster;

    @Autowired
    private SecurityDao securityDao;

    @Override
    public JSONObject getLatestVersion(){
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SUCCESSFUL.getStatusCode());
        resJSON.put("latestversion", LATESTVERSIONNUM);
        resJSON.put("latestverisoninfo", LATESTVERSIOINFO);
        return resJSON;
    }

    @Override
    public JSONObject searchClientinfoLatlng(String year, String month, String day, String strDayNum, String delayseconds) {
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.PARAM_ERROR.getMessage());
        if (year == null || month == null || day == null){
            return resJSON;
        }
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMdd");

        boolean isSearchLastDayNum = false; //是否查询设置日期前一天当前时间之后的用户数据

        year = StringUtils.leftPad(year, 4,   "0");
        month = StringUtils.leftPad(month, 2, "0");
        day   = StringUtils.leftPad(day, 2,   "0");
        final int DEFAULTDAYNUM = 10;//默认取十天活跃时间
        float dayNum = DEFAULTDAYNUM;
        if (strDayNum != null && !strDayNum.isEmpty()){
            try{
                dayNum = Float.parseFloat(strDayNum);
            }
            catch(NumberFormatException ex){
                dayNum = DEFAULTDAYNUM;
            }
        }
        if (dayNum < 0 || dayNum > 365){
            dayNum = DEFAULTDAYNUM;
        }

        int secNums = -1;
        if (delayseconds != null && !delayseconds.isEmpty()){
            try{
                secNums = Integer.parseInt(delayseconds);
            }
            catch(NumberFormatException ex){
                secNums = -1;
            }
        }
//        if (dayNum < 1 && dayNum > 0){ // 0<dayNum <1时，换算为多少秒
//            secNums = Math.round(dayNum * 3600 * 24);
//            dayNum = 1;
//        }

        //查询活跃用户信息
        DateTime date = DateTime.parse(year + month + day , dateformat);
        DateTime sdate = date.minusDays((int)dayNum);
        final String activeTimes = "activetimes";
        final String latlngDetail = "latlngdetail";
        int timesnum = 0;
        JSONObject latlngsJSON = new JSONObject();
        JSONObject activeUsers = new JSONObject();

        Set<String> fieldList0 = jedisCluster.hkeys(APPCLIENTINFOKEY + ":" + date.toString(dateformat));
        if (secNums > 0 && fieldList0 != null){ //secNums > 0表明需首先查询记录secNums秒之内的活跃用户
            for (String field : fieldList0){
                String activeTime = field.split("_")[1];
                if (secNums > 0){ //secNums > 0表明查询secNums秒之内的数据
                    long lastsecs = new Duration(DateTime.parse(activeTime, DateTimeFormat.forPattern("yyyyMMddHHmmss")),
                            DateTime.now()).getStandardSeconds();
                    if (lastsecs <= secNums) {
                        activeUsers.put(field.split("_")[0], activeTime);
                    }
                }
            }
        }

        //从设置的日期逐日向前查询
        String curHHmmss = DateTime.now().toString("HHmmss");
        float searchDayNum = dayNum;
        if (isSearchLastDayNum)
            searchDayNum += 1; //这里用dayNum+1，是在取用户信息时，由于当前天还不是完整一天，添加所取时间的第一天前一天的相应小时内用户数

        JSONObject versionJSON = new JSONObject();
        int iOSUsersNum = 0;
        int AndroidUsersNum = 0;
        for (int i = 0; i < searchDayNum; i++){
            Set<String> fieldList;
            if (i == 0){
                fieldList = fieldList0;
            } else {
                fieldList = jedisCluster.hkeys(APPCLIENTINFOKEY + ":" + date.toString(dateformat));
            }

            if (fieldList != null){
                Map<String, String> keyvalueMap = jedisCluster.hgetAll(APPCLIENTINFOKEY + ":" + date.toString(dateformat));
                int fieldcount = 0;
                for (String field : fieldList){
                    String activeTime = field.split("_")[1];
                    String user = field.split("_")[0];
                    if (secNums > 0 ){
                        if (activeUsers.getString(user) == null) //表明在secNums内没有活动
                            continue;
                    }

                    if (i == dayNum){
                        String activeHHmmss= activeTime.substring(8);
                        if (activeTime.substring(8).compareTo(curHHmmss)<0)
                            continue;
                    }

                    String value = keyvalueMap.get(field); //Android_2.8.1_enterbackgroud_35.479744|110.4735

//                    if (!value.contains("lanch")) //只统计启动的次数
//                        continue;
                    fieldcount++;

                    String[] values = value.split("_");
                    if (values.length < 4)
                        continue;

                    String type = values[0];
                    if (type.equals("iOS"))
                        iOSUsersNum++;
                    if (type.equals("Android"))
                        AndroidUsersNum++;
                    String version = "v" + values[1].replace(".", "_");
                    if (versionJSON.getInteger(version) != null){
                        Integer tmpVersionNum = versionJSON.getInteger(version) + 1;
                        versionJSON.put(version, tmpVersionNum);
                    } else {
                        versionJSON.put(version, 1);
                    }

                    String operate = values[2];
                    String[] latlngs = values[3].split("\\|");
                    String lat = String.format("%.4f", Math.round(Float.parseFloat(latlngs[0])*10000)/10000.0f);
                    String lng = String.format("%.4f", Math.round(Float.parseFloat(latlngs[1])*10000)/10000.0f);
                    JSONArray tmpary = latlngsJSON.getJSONArray(lat + "_" + lng);
                    if (tmpary == null){
                        tmpary = new JSONArray();
                        tmpary.add(1);
                        tmpary.add(activeTime); //活动时间
                        tmpary.add(type);
                        tmpary.add(values[1]);
                        tmpary.add(operate);
                    } else  {
                        tmpary.set(0, Integer.parseInt(tmpary.get(0).toString())+1);
                        if (activeTime.compareTo(tmpary.get(1).toString())>0){
                            tmpary.set(1, activeTime);
                            tmpary.set(2, type);
                            tmpary.set(3, values[1]);
                            tmpary.set(4, operate);
                        }
                    }
                    latlngsJSON.put(lat + "_" + lng, tmpary);
                }
                if (secNums > 0){ //删除不在secNums之内的数据
                    List<String> removeKeys = new ArrayList<>();
                    for (String user : latlngsJSON.keySet()){
                        JSONArray ary = latlngsJSON.getJSONArray(user);
                        if (ary == null)
                            continue;
                        long lastsecs = new Duration(DateTime.parse(ary.get(1).toString(), DateTimeFormat.forPattern("yyyyMMddHHmmss")),
                                DateTime.now()).getStandardSeconds()+2; //假设查询耗费2秒
                        if (lastsecs > secNums) {
                            removeKeys.add(user);
                        }
                    }
                    if (removeKeys.size() > 0){
                        for (String user : removeKeys){
                            latlngsJSON.remove(user);
                        }
                    }
                }
             //   timesnum += fieldcount;
                timesnum += fieldList.size();
            }
            date = date.minusDays(1);
        }
        JSONObject resDataJSON = new JSONObject();
        resDataJSON.put(activeTimes, timesnum);
        resDataJSON.put("versionstat", versionJSON);
        resDataJSON.put("iOSusers", iOSUsersNum);
        resDataJSON.put("androidusers", AndroidUsersNum);
        resDataJSON.put(latlngDetail, latlngsJSON);
        String time = sdate.toString(dateformat) + "_" + sdate.plusDays(DEFAULTDAYNUM).toString(dateformat);
        resJSON.put(ResJsonConst.TIME, time);
        resJSON.put(ResJsonConst.DATA, resDataJSON);
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SUCCESSFUL.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SUCCESSFUL.getMessage());
        return resJSON;
    }

    @Override
    public JSONObject searchClientinfo(String year, String month, String day, String strDayNum) {
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.PARAM_ERROR.getMessage());
        if (year == null || month == null || day == null){
            return resJSON;
        }
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMdd");

        year = StringUtils.leftPad(year, 4,   "0");
        month = StringUtils.leftPad(month, 2, "0");
        day   = StringUtils.leftPad(day, 2,   "0");
        final int DEFAULTDAYNUM = 10;//默认取十天活跃时间
        int dayNum = DEFAULTDAYNUM;
        if (strDayNum != null && !strDayNum.isEmpty()){
            try{
                dayNum = Integer.parseInt(strDayNum);
            }
            catch(NumberFormatException ex){
                dayNum = DEFAULTDAYNUM;
            }
        }
        if (dayNum < 0 || dayNum > 365){
            dayNum = DEFAULTDAYNUM;
        }
        //查询活跃用户信息
        DateTime date = DateTime.parse(year + month + day , dateformat);
        DateTime sdate = date.minusDays(dayNum);
        final String activeUsers = "activeusers";
        final String activeTimes = "activetimes";
        final String usersDetail = "usersdetail";
        int timesnum = 0;
        JSONObject usersPIDJSON = new JSONObject();
        //从设置的日期逐日向前查询
        for (int i = 0; i < dayNum; i++){
            Set<String> fieldList = jedisCluster.hkeys(APPCLIENTINFOKEY + ":" + date.toString(dateformat));
            if (fieldList != null){
                for (String field : fieldList){
                    String userPID = (field.split("_"))[0];
//                    String value = jedisCluster.hget(APPCLIENTINFOKEY + ":" + date.toString(dateformat), field);
                    Integer num = usersPIDJSON.getInteger(userPID);
                    if (num == null) num = 0;
                    usersPIDJSON.put(userPID, ++num);
                }
                timesnum += fieldList.size();
            }
            date = date.minusDays(1);
        }
        JSONObject resDataJSON = new JSONObject();
        resDataJSON.put(activeUsers, usersPIDJSON.keySet().size());
        resDataJSON.put(activeTimes, timesnum);
        resDataJSON.put(usersDetail, usersPIDJSON);
        String time = sdate.toString(dateformat) + "_" + sdate.plusDays(DEFAULTDAYNUM).toString(dateformat);
        resJSON.put(ResJsonConst.TIME, time);
        resJSON.put(ResJsonConst.DATA, resDataJSON);
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SUCCESSFUL.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SUCCESSFUL.getMessage());
        return resJSON;
    }


    @Override
    public JSONObject saveClientinfo(String userPID, String clientType, String clientVersion, String operateType,
                                     String sLat, String sLng) {
        //1.获取传入的各类用户信息,并判断传入的正确性
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.PARAM_ERROR.getMessage());
        if (userPID == null || clientVersion == null) {
            return resJSON;
        }

        Float lat = null, lng = null;
        if (sLat != null && sLng != null){
            try{
                lat = Float.parseFloat(sLat);
                lng = Float.parseFloat(sLng);
            }
            catch (NumberFormatException ex){
                logger.error("经纬度数据转换失败:" + ex);
                return resJSON;
            }
        }

        if (operateType == null)
            operateType = "unknown";
        if (clientVersion == null)
            clientVersion = "unknown";

        DateTime curDate = DateTime.now();
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        //2.保存用户活跃信息
        String clientinfoKey = APPCLIENTINFOKEY + ":" + curDate.toString("yyyy")
                + curDate.toString("MM") + curDate.toString("dd");     //key由日期组成
        String field = userPID + "_" + curDate.toString(dateformat);   //由用户PID+时间组成
        String value = clientType + "_" + clientVersion + "_" + operateType;
        if (lat != null && lng != null){
            value += "_" + lat + "|" + lng;
        }
        jedisCluster.hset(clientinfoKey, field, value);

        //3.根据终端及版本判断返回信息
        if (clientVersion.length() >= 3){
            try{
                String versionNum = clientVersion.toString();
                if (versionNum.compareToIgnoreCase(LATESTVERSIONNUM) >= 0){
                    resJSON.put(ResJsonConst.STATUSCODE, ResStatus.UNVERIFIED_VALIDVERSION.getStatusCode());
                    resJSON.put(ResJsonConst.STATUSMSG, ResStatus.UNVERIFIED_VALIDVERSION.getMessage());
                } else if (versionNum.compareToIgnoreCase(MINVERSIONNUM)>=0){
                    resJSON.put(ResJsonConst.STATUSCODE, ResStatus.UNVERIFIED_OLDVALIDVERSION.getStatusCode());
                    resJSON.put(ResJsonConst.STATUSMSG, ResStatus.UNVERIFIED_OLDVALIDVERSION.getMessage());
                } else {
                    resJSON.put(ResJsonConst.STATUSCODE, ResStatus.UNVERIFIED_INVALIDVERSION.getStatusCode());
                    resJSON.put(ResJsonConst.STATUSMSG, ResStatus.UNVERIFIED_INVALIDVERSION.getMessage());
                }
                return resJSON;
            }
            catch (java.lang.NumberFormatException ex){
                logger.error("版本数值转换失败:" + ex);
                return resJSON;
            }
        } else {
            return resJSON;
        }
    }

    @Override
    public JSONObject saveFedbackInfo(String userPID, String clientType, String clientVersion, String desc, String phonenum, String picpath, String email) {
        //1.获取传入的各类用户信息,并判断传入的正确性
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.PARAM_ERROR.getMessage());
        if (userPID == null || clientVersion == null || desc == null || desc.isEmpty()) {
            return resJSON;
        }
        int insertid = securityDao.saveFedbackInfo(userPID,clientType, clientVersion, desc, phonenum, picpath, email);
        if (insertid > 0){
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SUCCESSFUL.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SUCCESSFUL.getMessage());
            resJSON.put("insertid", insertid);
            //插入成功后,发送邮件
            Thread send = new Thread(new Runnable(){
                public void run(){
                    sendEmail(insertid, userPID,clientType, clientVersion, desc, phonenum, picpath, email);
                }
            });
            send.start();

        } else {
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SEARCH_ERROR.getMessage());
        }
        return resJSON;
    }

    @Override
    public JSONObject getKey(String userPID, String clientType, String clientVersion, String sLat, String sLng) {
        //根据PID 经纬度 时间等生成Key
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.PARAM_ERROR.getMessage());
        if (userPID == null || clientVersion == null) {
            return resJSON;
        }
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SUCCESSFUL.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SUCCESSFUL.getMessage());
        JSONObject data = new JSONObject();

        DateTime date = DateTime.now();
        String soureKey = userPID + "_" + clientType  + "_" + date.toString();
        if (sLat != null && !sLat.isEmpty()){
            soureKey += sLat;
        }
        if (sLng != null && !sLng.isEmpty()){
            soureKey += sLng;
        }
        String resKey = new SHA1().getDigestOfString(soureKey.getBytes());

        data.put("key", resKey);
        resJSON.put(ResJsonConst.DATA, data);
        return resJSON;
    }


    private void sendEmail(int id, String userPID, String clientType, String clientVersion, String desc, String phonenum, String picpath, String email) {

        try {
            final Properties props = new Properties();
        /*
         * 可用的属性： mail.store.protocol / mail.transport.protocol / mail.host /
         * mail.user / mail.from
         */
            // 表示SMTP发送邮件，需要进行身份验证
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.host", "smtp.xinhong.net");
//            // 发件人的账号
//            props.put("mail.user", "app@xinhong.net");
//            // 访问SMTP服务时需要提供的密码
//            props.put("mail.password", "app123");


//            props.put("mail.smtp.starttls.enable","false");//使用 STARTTLS安全连接
//            props.put("mail.smtp.host", "smtp.xinhong.net");
//            // 发件人的账号
//            props.put("mail.user", "app@xinhong.net");
//            // 访问SMTP服务时需要提供的密码
//            props.put("mail.password", "app123");

            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable","true");//使用 STARTTLS安全连接
            props.put("mail.smtp.host", "smtp.sina.com");
            // 发件人的账号
            props.put("mail.user", "xinhongapp@sina.com");
            // 访问SMTP服务时需要提供的密码
            props.put("mail.password", "xinhongapp2002");

            // 构建授权信息，用于进行SMTP进行身份验证
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    // 用户名、密码
                    String userName = props.getProperty("mail.user");
                    String password = props.getProperty("mail.password");
                    return new PasswordAuthentication(userName, password);
                }
            };
            // 使用环境属性和授权信息，创建邮件会话
            Session mailSession = Session.getInstance(props, authenticator);
            // 创建邮件消息
            MimeMessage message = new MimeMessage(mailSession);
            // 设置发件人
            InternetAddress from = new InternetAddress(
                    props.getProperty("mail.user"));
            message.setFrom(from);

            // 设置收件人
            InternetAddress to = new InternetAddress("app@xinhong.net");
            message.setRecipient(MimeMessage.RecipientType.TO, to);

            // 设置抄送
            InternetAddress cc = new InternetAddress("liuxiaochang@xinhong.net");
            message.setRecipient(MimeMessage.RecipientType.CC, cc);

            // 设置密送，其他的收件人不能看到密送的邮件地址
//            InternetAddress bcc = new InternetAddress("liuxiaochang@xinhong.com");
//            message.setRecipient(MimeMessage.RecipientType.CC, bcc);

            // 设置邮件标题
            message.setSubject("app意见反馈_" + userPID);

            StringBuffer content = new StringBuffer();
            content.append("<h2>请按照以下描述信息分析后用邮件或电话进行反馈.谢谢!</h2>").append("<br><br>");

            content.append("<h4>时间:     </h4><h5>").append(DateTime.now().toString("yyyy-MM-dd HH:mm:ss")).append("</h5><br>");
            content.append("<h4>userPID:  </h4><h5>").append(userPID).append("</h5><br>");
            content.append("<h4>客户端类型:</h4><h5>").append(clientType).append("</h5><br>");
            content.append("<h4>app版本:  </h4><h5>").append(clientVersion).append("</h5><br>");
            content.append("<h4>问题描述:  </h4><h5>").append(desc).append("</h5><br>");
            content.append("<h4>图片位置:  </h4><h5>").append(picpath).append("</h5><br>");
            content.append("<h4>电话:     </h4><h5>").append(phonenum).append("</h5><br>");
            content.append("<h4>email:    </h4><h5>").append(email).append("</h5><br>");

            // 设置邮件的内容体
            message.setContent(content.toString(), "text/html;charset=UTF-8");

            // 发送邮件
            Transport.send(message);
        } catch (MessagingException ex) {
            logger.error("反馈意见邮件发送失败:" + ex);
        }
    }

}
