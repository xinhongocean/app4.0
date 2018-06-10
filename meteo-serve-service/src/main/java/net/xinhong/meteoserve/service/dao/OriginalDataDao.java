package net.xinhong.meteoserve.service.dao;

import net.xinhong.meteoserve.service.domain.origin.GTSPPData;
import net.xinhong.meteoserve.service.domain.origin.ZHS_ICOADS_ELES;
import net.xinhong.meteoserve.service.domain.origin.ZH_TYPH_INT;
import net.xinhong.meteoserve.service.domain.origin.ZH_TYPH_JMA;
import net.xinhong.meteoserve.service.domain.origin.ZH_TYPH_TPC;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by wingsby on 2018/1/3.
 */
@AutoScanAndDAO
public interface OriginalDataDao {

    List<ZHS_ICOADS_ELES> getICOADS(@Param("grid") int gridid, @Param("sdate") String sdate, @Param("edate") String edate,
                                    @Param("sline") int sline,@Param("eline")int eline);
    int getICOADSPages(@Param("grid") int gridid, @Param("sdate") String sdate, @Param("edate") String edate);

    List getINTTYPHIDX(@Param("year") int year);

    List<ZH_TYPH_INT> getINTTYPH(@Param("id") String id);

    List getJMATYPHIDX(@Param("year") int year);

    List<ZH_TYPH_JMA> getJMATYPH(@Param("id") String id);

    List getTPCTYPHIDX(@Param("year") int year);
    List<ZH_TYPH_TPC> getTPCTYPH(@Param("id") String id);

    List<GTSPPData> getGtsppBuoy(@Param("id") String id);
}
