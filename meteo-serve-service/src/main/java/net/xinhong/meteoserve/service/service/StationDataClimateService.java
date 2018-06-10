package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

/**
 * Created by xiaoyu on 16/7/19.
 * 城镇气候产品
 */
@Service
public interface StationDataClimateService {

    /**
     * 获取某个站点气候产品基本信息,包括:累年日/旬/月统计信息.
     * @param strCode
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getForYearsDataFromCode(String strCode, String year, String month, String day, String hour, String minute);
}
