package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * 获取航班及航线数据
 * Created by xiaoyu on 16/4/19.
 */
//todo:需要考虑增加按照机场名称查询
public interface AirLineDataService {


    /**
     * 根据航班号及日期获取航班
     * @param flightNumber
     * @param year
     * @param month
     * @param day
     * @return
     */
    JSONObject getFlightFromNumber(String flightNumber, String year, String month, String day);

    /**
     * 根据起飞降落机场(四字码)及日期获取航班     *
     * @param deptAptCode
     * @param arrAptCode
     * @param year
     * @param month
     * @param day
     * @return
     */

    JSONObject getFlightFromDeptArr(String deptAptCode, String arrAptCode, String year, String month, String day);


    /**
     * 根据起飞及降落机场(四字码)获取航线名称列表
     * @param deptAptCode
     * @param arrAptCode
     * @return
     */
    JSONObject getAirLineNameListFromDeptArr(String deptAptCode, String arrAptCode);


    /**
     * 根据航线名称获取航线具体信息
     * @param airLineName
     * @return
     */
    JSONObject getAirLineFromName(String airLineName);

}
