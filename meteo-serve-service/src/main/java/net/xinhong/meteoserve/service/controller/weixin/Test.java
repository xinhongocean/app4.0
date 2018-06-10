package net.xinhong.meteoserve.service.controller.weixin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by xiaoyu on 16/8/18.
 */
public class Test {
    public static String sendRequest(String json, String uri) {
        URL url;

        StringBuffer sb = new StringBuffer();
        try {
            url = new URL(uri);
            byte[] data = json.getBytes("UTF-8");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // conn.setConnectTimeout(5000);
            conn.setRequestMethod("POST");//设置POST方式获取数据
            conn.setDoOutput(true);

            /*conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=" + encoding);
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));*/
            OutputStream outStream = conn.getOutputStream();
            outStream.write(data);
            outStream.flush();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader reader = new BufferedReader(in);
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            outStream.close();
            conn.disconnect();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "程序异常";
        }
    }
    public static void main(String[] args) {
        String xml = "<xml>\n" +
                " <ToUserName><![CDATA[toUser]]></ToUserName>" +
                " <FromUserName><![CDATA[fromUser]]></FromUserName>" +
                " <CreateTime>1348831860</CreateTime>" +
                " <MsgType><![CDATA[text]]></MsgType>" +
                " <Content><![CDATA[this is a test]]></Content>" +
                " <MsgId>1234567890123456</MsgId>" +
                " </xml>";
        String str = sendRequest(xml, "http://192.168.0.120:8080/weixin");
        System.out.println(str);
    }
}
