package net.xinhong.meteoserve.service.common;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.tool.XmlUtil;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 邓帅
 * @version 创建时间：2016/3/7 0007.
 */
public class DataColumnUtil {

    private static Logger logger = LoggerFactory.getLogger(DataColumnUtil.class);


    /**
     * 加载所有数据类型的数据项配置
     *
     * @return
     */
    public static String getColumns(String fileName) {
        BufferedReader buffer = null;
        Map<String, String> dataMap = new HashMap<>();
        StringBuilder data = new StringBuilder();
        File file = null;
        try {
            String path = DataColumnUtil.class.getResource("/").getPath();
            file = new File(URLDecoder.decode(path, "UTF-8") + File.separator + "dataconf" + File.separator + fileName);
            buffer = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String temp;
            while ((temp = buffer.readLine()) != null) {
                data.append(temp);
            }
        } catch (FileNotFoundException e) {
            logger.error("没有找到文件，路径{}", file.getPath(), e);
        } catch (IOException e) {
            logger.error("加载{}数据列配置信息失败！{}", fileName, e);
        } finally {
            if (buffer != null)
                try {
                    buffer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return data.toString();
    }


    public static JSONObject getFieldColInfo(XmlUtil xmlUtil, JSONObject jsonObj) {
        Element element = xmlUtil.getSignNode("//mds/COLUMNS");
        List<Element> elList = xmlUtil.getChilds(element);
        JSONObject fieldInfo = new JSONObject();
        for (int k = 0; k < elList.size(); k++) {
            Map<String, String> attrValues = xmlUtil.getNodeAttrValues(elList.get(k));
            for (String s : jsonObj.keySet()) {
                if (attrValues.get("ENG").equals(jsonObj.getString(s))) {
                    JSONObject tmpJson = new JSONObject();
                    tmpJson.put("index", k);
                    tmpJson.put("FORMAT", attrValues.get("FORMAT"));
                    fieldInfo.put(s, tmpJson);
                }
            }
        }
        return fieldInfo;
    }

    public static JSONObject getDataColInfo(XmlUtil xmlUtil, JSONObject jsonObj) {
        Element element = xmlUtil.getSignNode("//mds/COLUMNS");
        List<Element> elList = xmlUtil.getChilds(element);
        JSONObject dataInfo = new JSONObject();
        for (int k = 0; k < elList.size(); k++) {
            Map<String, String> attrValues = xmlUtil.getNodeAttrValues(elList.get(k));
            for (String s : jsonObj.keySet()) {
                if (attrValues.get("ENG").equals(jsonObj.getJSONObject(s).getString("ENG"))) {
                    JSONObject tmpJson = new JSONObject();
                    tmpJson.put("index", k);
                    tmpJson.put("FORMAT", attrValues.get("FORMAT"));
                    dataInfo.put(s, tmpJson);
                }
            }
        }
        return dataInfo;
    }
}
