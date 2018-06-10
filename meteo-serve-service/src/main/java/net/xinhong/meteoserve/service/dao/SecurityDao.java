package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 16/10/24.
 */
public interface SecurityDao {
    int saveFedbackInfo(String userPID, String clientType, String clientVersion, String desc, String phonenum, String picpath, String email);
}
