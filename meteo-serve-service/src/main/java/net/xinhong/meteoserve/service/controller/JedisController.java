package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by xiaoyu on 16/6/13.
 * Jedis相关信息查看
 */
@Controller
@RequestMapping(value = "/redis")
public class JedisController {
    private static final Log logger = LogFactory.getLog(JedisController.class);

    @Autowired
    private JedisCluster jedisCluster;


    @RequestMapping(value = "/clusterinfo", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void JedisInfo(HttpServletRequest request, HttpServletResponse response) {

        logger.info("进行集群信息查询:");
        long tt = System.currentTimeMillis();
        JSONObject resJSON = new JSONObject();
        try {
            resJSON.put(ResJsonConst.STATUSCODE, 1);
            Map<String, JedisPool> clusterNodes =  jedisCluster.getClusterNodes();
            if (clusterNodes == null || clusterNodes.isEmpty())
                return;

            JSONObject infoJSON = new JSONObject();
            for (String nodeIP : clusterNodes.keySet()){
                JedisPool pool = clusterNodes.get(nodeIP);
                String info =  pool.getResource().clusterInfo();
                String[] infos = info.split("\r\n");

                infoJSON.put(nodeIP, JSONArray.toJSON(infos));
                break;
            }
            resJSON.put(ResJsonConst.STATUSCODE, 0);
            resJSON.put(ResJsonConst.DATA, infoJSON);

            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponseDomain(response, resJSON);
        } catch (Exception e) {
            resJSON.put(ResJsonConst.STATUSCODE, 302);
            logger.error("redis culsterinfo信息查询失败:" + e);
            JSONUtil.writeJSONToResponseDomain(response, resJSON);
        }
    }

    @RequestMapping(value = "/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void JedisClusterInfo(HttpServletRequest request, HttpServletResponse response) {

        logger.info("进行节点信息查询:");
        long tt = System.currentTimeMillis();
        JSONObject resJSON = new JSONObject();
        try {
            resJSON.put(ResJsonConst.STATUSCODE, 1);
            Map<String, JedisPool> clusterNodes =  jedisCluster.getClusterNodes();
            if (clusterNodes == null || clusterNodes.isEmpty())
                return;


            JSONObject dataJSON = new JSONObject();
            for (String nodeIP : clusterNodes.keySet()){ //逐个节点

                JedisPool pool = clusterNodes.get(nodeIP);
                String info =  pool.getResource().info();
                if (info == null || info.length()<2)
                    continue;
                JSONObject nodeinfoJSON = new JSONObject();
                String[] nodeinfos = info.split("#"); //#分解为逐个信息项目(#Server  #Memory...)
                for (int k = 0; k < nodeinfos.length; k++){
                    String[] infos = nodeinfos[k].split("\r\n");
                    if (infos == null || infos.length < 2)
                        continue;

                    JSONArray infovalues = new JSONArray();
                    for (int m = 1; m<infos.length; m++){
                        infovalues.add(infos[m]);
                    }
                    nodeinfoJSON.put(infos[0], infovalues); //#分割后第一个为标识
                }

                dataJSON.put(nodeIP, nodeinfoJSON);
                break;
            }
            resJSON.put(ResJsonConst.STATUSCODE, 0);
            resJSON.put(ResJsonConst.DATA, dataJSON);

            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponseDomain(response, resJSON);
        } catch (Exception e) {
            resJSON.put(ResJsonConst.STATUSCODE, 302);
            logger.error("redis info信息查询失败:" + e);
            JSONUtil.writeJSONToResponseDomain(response, resJSON);
        }
    }


    // surf, high, cityfc, typh, isoline,station,gfs,image,himawari8
    @RequestMapping(value = "/keys", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void JedisKeys(HttpServletRequest request, HttpServletResponse response) {
        long tt = System.currentTimeMillis();
        JSONObject resJSON = new JSONObject();
        String type = request.getParameter("type");
        logger.info("进行key信息查询:type =" + type );
        if  (type == null || type.isEmpty() ){
            resJSON.put(ResJsonConst.STATUSCODE, 301);
            return;
        }

        try {
            resJSON.put(ResJsonConst.STATUSCODE, 1);
            Map<String, JedisPool> clusterNodes =  jedisCluster.getClusterNodes();
            if (clusterNodes == null || clusterNodes.isEmpty())
                return;

            List<String> keynameList = new ArrayList<>();
            for (String nodeIP : clusterNodes.keySet()){
                JedisPool pool = clusterNodes.get(nodeIP);
                Set<String> keys = pool.getResource().keys("*" + type + "*");
                for (String keyname : keys){
                    String len = " ";
                    try {
                        len = " " + jedisCluster.hlen(keyname);
                    }catch (JedisDataException ex){
                        len = "非Hash结构";
                    }

                    keynameList.add(nodeIP + "_" + keyname + " " + len);
                }
            }
            Collections.sort(keynameList);

            resJSON.put(ResJsonConst.STATUSCODE, 0);
            resJSON.put(ResJsonConst.DATA, JSONArray.toJSON(keynameList));

            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponseDomain(response, resJSON);
        } catch (Exception e) {

            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SEARCH_ERROR.getMessage());
            logger.error("redis keys信息查询失败:" + e);
            JSONUtil.writeJSONToResponseDomain(response, resJSON);
        }
    }
}
