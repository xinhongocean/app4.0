package net.xinhong.meteoserve.common.tool;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:xml文件操作工具类 <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 邓帅
 * @version 创建时间：2016/3/4 0004.
 */
public class XmlUtil {

    private File file;
    private String filePaht;
    private Document document;

    public XmlUtil(String filePath) {
       // this.filePaht = this.getClassLoader().getResource(filePath).getPath();
        this.filePaht = filePath;
        this.file = new File(filePath);
        this.initDocument();
    }

    public XmlUtil(File file){
        this.file = file;
        this.initDocument();
    }

    private ClassLoader  getClassLoader(){
       return  XmlUtil.class.getClassLoader();
    }

    /**
     * 加载XML文档
     */
    private void initDocument(){
        try {

            SAXReader reader = new SAXReader();
            Document document = reader.read(file);
            this.document = document;
        } catch (DocumentException e) {
            e.printStackTrace();
         //   logger.error("加载xml文件失败！文件路径：{}",filePaht);
        }
    }

    /**
     * 获取XML文档跟节点
     * @return
     */
    public Element getRoot(){
        return this.document.getRootElement();
    }

    /**
     * 获取所有的子元素
     * @param element
     * @return
     */
    public List<Element> getNodes(Element element){
        return element.elements();
    }

    /**
     * 根据xml节点路径获取Nodes
     * @param nodePath //xx/x
     * @return
     */
    public List<Element> getNodes(String nodePath){
        return this.document.selectNodes(nodePath);
    }

    /**
     * 根据xml路径获取唯一Node
     * @param nodePath //xx/x
     * @return
     */
    public Element getSignNode(String nodePath){
        return (Element)this.document.selectSingleNode(nodePath);
    }


    public List<Element> getChilds(Element element){
        return element.elements();
    }
    /**
     * 根据路径获取某个节点的数据
     * @param nodePath
     * @return
     */
    public String getNodeValueByPath(String nodePath){
        return this.getSignNode(nodePath).getText();
    }

    /**
     * 在获取当前节点下某个路径的Node
     * @param element
     * @param nodePath /XX/XX
     * @return
     */
    public Element getElementByDocument(Element element,String nodePath){
        return (Element)element.getDocument().selectSingleNode(nodePath);
    }
    /**
     * 根据节点获取节点文本信息
     * @param element
     * @return
     */
    public String getNodeValue(Element element){
        return  element.getText();
    }
    /**
     * 获取某个节点的属性值
     * @param element
     * @param attrName
     * @return
     */
    public String getNodeAttrValue(Element element,String attrName){

        return element.attribute("attrName").getStringValue();
    }
    /**
     * 获取该节点的所有属性
     * @param element
     * @return Map<attrName,attrValue>
     */
    public Map<String,String> getNodeAttrValues(Element element){
        Map<String,String> map = new HashMap<String,String>();
        Attribute attr = null;
        for (int i=0;i<element.attributeCount();i++){
            attr = element.attribute(i);
            map.put(attr.getName(),attr.getStringValue()) ;
        }
        return map;
    }
    /**
     * 获取节点名字
     * @param element
     * @return
     */
    public String getNodeName(Element element){
        return element.getName();
    }

    public Document getDocument() {
        return document;
    }


}
