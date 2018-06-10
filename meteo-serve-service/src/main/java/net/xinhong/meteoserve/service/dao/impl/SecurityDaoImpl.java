package net.xinhong.meteoserve.service.dao.impl;

import net.xinhong.meteoserve.service.dao.SecurityDao;
import net.xinhong.meteoserve.service.domain.UserFeedBackBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * Created by xiaoyu on 16/10/24.
 */
@Repository
public class SecurityDaoImpl extends DaoSupport implements SecurityDao {
    private static final Log logger = LogFactory.getLog(SecurityDaoImpl.class);
    private static final String NAMESPACE = "net.xinhong.meteoserve.service.security";

    @Autowired
    private SqlSessionTemplate sqlSession;
    @Override
    public int saveFedbackInfo(String userPID, String clientType, String clientVersion, String desc, String phonenum, String picpath, String email) {
        UserFeedBackBean bean = new UserFeedBackBean();
        bean.setClienttype(clientType);
        bean.setPid(userPID);
        bean.setClientversion(clientVersion);
        bean.setFeedbackdesc(desc);
        bean.setPhonenum(phonenum);
        bean.setPicpath(picpath);
        bean.setEmail(email);
        int res = this.sqlSession.insert(this.NAMESPACE + ".appendUserFeedBack", bean);
        return res;
    }

    @Override
    protected void checkDaoConfig() throws IllegalArgumentException {
        Assert.notNull(this.sqlSession, "Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required");
    }
}
