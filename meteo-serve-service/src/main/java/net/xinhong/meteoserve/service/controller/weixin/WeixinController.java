package net.xinhong.meteoserve.service.controller.weixin;

import net.xinhong.meteoserve.service.common.weixin.SHA1;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaoyu on 16/8/18.
 */
@Controller
//@RequestMapping(value = "/weixin")
public class WeixinController {
    private static final Log logger = LogFactory.getLog(WeixinController.class);

    final private static String weixinhao = "xinhonggaoke";

    @RequestMapping(value = "/weixin", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    public final void procmsg(InputStream request, HttpServletResponse response) {


        response.setCharacterEncoding("UTF-8");

        try{
            InputStream is = request;

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer requestMsg = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) {
                requestMsg.append(str);
            }
            String strMsg = requestMsg.toString();
            //strMsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + strMsg;

            //接收微信发过来的xml格式
            Document document = DocumentHelper.parseText(strMsg);
            Map<String, String> requestMap = new HashMap<>();
            Node node;
            for (int i=0;i<document.getRootElement().nodeCount();i++){
                node = document.getRootElement().node(i);
                requestMap.put(node.getName(), node.getStringValue()) ;
            }
            //发送方帐号(open_id)
            String fromUserName = requestMap.get("FromUserName");
            //公众帐号
            String toUserName = requestMap.get("ToUserName");
            //消息类型
            String msgType = requestMap.get("MsgType");
            //消息创建时间
            String createTime = requestMap.get("CreateTime");
            //微信服务器post过来的内容
            String weixinContent = requestMap.get("Content");

            Long returnTime = Calendar.getInstance().getTimeInMillis()/1000;

            logger.info("发送用户" + fromUserName + "发送给" + toUserName +  ",发送过来的文本消息内容："+weixinContent);

            StringBuffer responseMessage = new StringBuffer();
            responseMessage.append("<xml>");
            responseMessage.append("<ToUserName><![CDATA[" + fromUserName + "]]></ToUserName>");
            responseMessage.append("<FromUserName><![CDATA[" + toUserName + "]]></FromUserName>");
            responseMessage.append("<CreateTime>" + returnTime + "</CreateTime>");
            responseMessage.append("<MsgType><![CDATA[" + msgType + "]]></MsgType>");
            responseMessage.append("<Content><![CDATA[你说的是：" + weixinContent + "，吗？]]></Content>");
            responseMessage.append("</xml>");

            // 响应回复消息
            response.getWriter().print(responseMessage.toString());
//            PrintWriter out = response.getWriter();
//            out.print(responseMessage);
//            out.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    @RequestMapping(value = "/weixin", method = {RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void sign(HttpServletRequest request, HttpServletResponse response) {

        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostr = request.getParameter("echostr");


        String strSign = "微信sign结果:" + signature + "," + timestamp + "," + nonce + "," + echostr;
        logger.info(strSign);

        String[] str = { weixinhao, timestamp, nonce };
        Arrays.sort(str); // 字典序排序
        String bigStr = str[0] + str[1] + str[2];
        // SHA1加密
        String digest = new SHA1().getDigestOfString(bigStr.getBytes())
                .toLowerCase();
        // 确认请求来至微信
        if (digest == null || !digest.equals(signature)) {

            logger.error("微信认证失败! digest=" + digest);

        } else {
            try {
                //.getWriter().print("微信认证成功:" + echostr);
                response.getWriter().print(echostr);
                logger.info("微信认证成功:echostr=" + echostr);
            } catch (IOException e) {
                logger.error("微信认证失败,异常为:" + e);
                e.printStackTrace();
            }
        }

    }



}
