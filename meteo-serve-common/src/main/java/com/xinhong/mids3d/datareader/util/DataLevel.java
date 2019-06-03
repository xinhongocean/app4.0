package com.xinhong.mids3d.datareader.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据层次
 * P开头表示气压，D开头表示深度，H开头表示高度
 * @author sjn
 *
 */
public enum DataLevel {
	
	//气压 100,200,300,500,700,850,925,1000,9999
	/**  气压 0010  */
    P0010("P0010","0010", "10hPa",10f),
    /**  气压 0020  */
    P0020("P0020","0020", "20hPa",20f),
    /**  气压 0030  */
    P0030("P0030","0030", "30hPa",30f),
    /**  气压 0050  */
    P0050("P0050","0050", "50hPa",50f),
    /**  气压 0070  */
    P0070("P0070","0070", "70hPa",70f),
	/**  气压 0100  */
    P0100("P0100","0100", "100hPa",100f),
    /**  气压 0150  */
    P0150("P0150","0150", "150hPa",150f),
    /**  气压 0175  */
    P0175("P0175","0175", "175hPa",175f),
    /**  气压 0200 */
    P0200("P0200","0200", "200hPa",200f),
    /**  气压 0230 */
    P0230("P0230","0230", "230hPa",230f),
    /**  气压 0250 
     * 传入250，转成了P0250!无法转成D0250!
     * */
    P0250("P0250","0250", "250hPa",250f),
    /**  气压 0270 */
    P0270("P0270","0270", "270hPa",270f),
    /**  气压 0300 */
    P0300("P0300","0300", "300hPa",300f),
    /**  气压 0350 */
    P0350("P0350","0350", "350hPa",350f),
    /**  气压 0400 */
    P0400("P0400","0400", "400hPa",400f),
    /**  气压 0500	 */
    P0500("P0500","0500", "500hPa",500f),
    /**  气压 0600 */
    P0600("P0600","0600", "600hPa",600f),
    /**  气压 0700	 */
    P0700("P0700","0700", "700hPa",700f),
    /**  气压 0800	 */
    P0800("P0800","0800", "800hPa",800f),
    /**  气压 0850	 */
    P0850("P0850","0850", "850hPa",850f),
    /**  气压 0900	 */
    P0900("P0900","0900", "900hPa",900f),
    /**  气压 0925	 */
    P0925("P0925","0925", "925hPa",925f),
    /**  气压 1000	 */
    P1000("P1000","1000", "1000hPa",1000f),
    /**  气压 9999	 */
    PSurf("PSurf", "9999", "地面",9999f),
    /** 位势稳定度的层次 */
    P500_850("P500_850", "0585", "500hPa-850hPa",500850f),
    /** 垂直风切变700-300hPa层次 */
    P700_300("P700_300", "0730", "700hPa-300hPa",700600f),
    
    //垂直风切变\理查逊数用到层次
    /** 850hPa层 */
    P925_700("P925_700", "0927", "925hPa-700hPa",925700f),
    /** 700hPa层 */
    P850_500("P850_500", "0855", "850hPa-500hPa",850500f),
    /** 500hPa层 */
    P700_400("P700_400", "0740", "700hPa-400hPa",700400f),
    /** 400hPa层 */
    P500_300("P500_300", "0530", "500hPa-300hPa",500300f),
    /** 300hPa层 */
    P400_200("P400_200", "0420", "400hPa-200hPa",400200f),
    /** 200hPa层 */
    P300_100("P300_100", "0310", "300hPa-100hPa",300100f),

  //海洋水文
    /** 表层 */
    D0000("D0000", "0000", "表层", 0,true),
    /** 1米层 */
    D0001("D0001", "0001", "1米层", 1),
    /** 2米层 */
    D0002("D0002", "0002", "2米层", 2),
    /** 3米层 */
    D0003("D0003", "0003", "3米层", 3),
    /** 4米层 */
    D0004("D0004", "0004", "4米层", 4),
    /** 5米层 */
    D0005("D0005", "0005", "5米层", 5,true),
    /** 6米层 */
    D0006("D0006", "0006", "6米层", 6),
    /** 7米层 */
    D0007("D0007", "0007", "7米层", 7),
    /** 8米层 */
    D0008("D0008", "0008", "8米层", 8),
    /** 9米层 */
    D0009("D0009", "0009", "9米层", 9),
    /** 10米层 */
    D0010("D0010", "0010", "10米层", 10,true),
    /** 11米层 */
    D0011("D0011", "0011", "11米层", 11),
    /** 12米层 */
    D0012("D0012", "0012", "12米层", 12),
    /** 13米层 */
    D0013("D0013", "0013", "13米层", 13),
    /** 5米层 */
    D0014("D0014", "0014", "14米层", 14),
    /** 15米层 */
    D0015("D0015", "0015", "15米层", 15,true),
    /** 16米层 */
    D0016("D0016", "0016", "16米层", 16),
    /** 17米层 */
    D0017("D0017", "0017", "17米层", 17),
    /** 18米层 */
    D0018("D0018", "0018", "18米层", 18),
    /** 5米层 */
    D0019("D0019", "0019", "19米层", 19),
    /** 20米层 */
    D0020("D0020", "0020", "20米层", 20,true),
    /** 25米层 */
    D0025("D0025", "0025", "25米层", 25,true),
    /** 30米层 */
    D0030("D0030", "0030", "30米层", 30,true),
    /** 35米层 */
    D0035("D0035", "0035", "35米层", 35,true),
    /** 40米层 */
    D0040("D0040", "0040", "40米层", 40,true),
    /** 45米层 */
    D0045("D0045", "0045", "45米层", 45,true),
    /** 50米层 */
    D0050("D0050", "0050", "50米层", 50,true),
    /** 55米层 */
    D0055("D0055", "0055", "55米层", 55,true),
    /** 60米层 */
    D0060("D0060", "0060", "60米层", 60,true),
    /** 65米层 */
    D0065("D0065", "0065", "65米层", 65,true),
    /** 70米层 */
    D0070("D0070", "0070", "70米层", 70,true),
    /** 75米层 */
    D0075("D0075", "0075", "75米层", 75,true),
    /** 80米层 */
    D0080("D0080", "0080", "80米层", 80,true),
    /** 85米层 */
    D0085("D0085", "0085", "85米层", 85,true),
    /** 90米层 */
    D0090("D0090", "0090", "90米层", 90,true),
    /** 95米层 */
    D0095("D0095", "0095", "95米层", 95,true),
    /** 100米层 */
    D0100("D0100", "0100", "100米层", 100,true),
    /** 105米层 */
    D0105("D0105", "0105", "105米层", 105),
    /** 110米层 */
    D0110("D0110", "0110", "110米层", 110, true),
    /** 115米层 */
    D0115("D0115", "0115", "115米层", 115),
    /** 120米层 */
    D0120("D0120", "0120", "120米层", 120, true),
    /** 125米层 */
    D0125("D0125", "0125", "125米层", 125),
    /** 130米层 */
    D0130("D0130", "0130", "130米层", 130, true),
    /** 135米层 */
    D0135("D0135", "0135", "135米层", 135),
    /** 140米层 */
    D0140("D0140", "0140", "140米层", 140, true),
    /** 145米层 */
    D0145("D0145", "0145", "145米层", 145),
    /** 150米层 */
    D0150("D0150", "0150", "150米层", 150, true),
    /** 155米层 */
    D0155("D0155", "0155", "155米层", 155),
    /** 160米层 */
    D0160("D0160", "0160", "160米层", 160, true),
    /** 165米层 */
    D0165("D0165", "0165", "165米层", 165),
    /** 170米层 */
    D0170("D0170", "0170", "170米层", 170, true),
    /** 175米层 */
    D0175("D0175", "0175", "175米层", 175),
    /** 180米层 */
    D0180("D0180", "0180", "180米层", 180, true),
    /** 185米层 */
    D0185("D0185", "0185", "185米层", 185),
    /** 190米层 */
    D0190("D0190", "0190", "190米层", 190, true),
    /** 195米层 */
    D0195("D0195", "0195", "195米层", 195),
    /** 200米层 */
    D0200("D0200", "0200", "200米层", 200, true),
    /** 205米层 */
    D0205("D0205", "0205", "205米层", 205),
    /** 210米层 */
    D0210("D0210", "0210", "210米层", 210, true),
    /** 215米层 */
    D0215("D0215", "0215", "215米层", 215),
    /** 220米层 */
    D0220("D0220", "0220", "220米层", 220, true),
    /** 225米层 */
    D0225("D0225", "0225", "225米层", 225),
    /** 230米层 */
    D0230("D0230", "0230", "230米层", 230, true),
    /** 235米层 */
    D0235("D0235", "0235", "235米层", 235),
    /** 240米层 */
    D0240("D0240", "0240", "240米层", 240, true),
    /** 245米层 */
    D0245("D0245", "0245", "245米层", 245),
    /** 250米层 */
    D0250("D0250", "0250", "250米层", 250, true),
    /** 255米层 */
    D0255("D0255", "0255", "255米层", 255),
    /** 260米层 */
    D0260("D0260", "0260", "260米层", 260, true),
    /** 265米层 */
    D0265("D0265", "0265", "265米层", 265),
    /** 270米层 */
    D0270("D0270", "0270", "270米层", 270, true),
    /** 275米层 */
    D0275("D0275", "0275", "275米层", 275),
    /** 280米层 */
    D0280("D0280", "0280", "280米层", 280, true),
    /** 285米层 */
    D0285("D0285", "0285", "285米层", 285),
    /** 290米层 */
    D0290("D0290", "0290", "290米层", 290, true),
    /** 295米层 */
    D0295("D0295", "0295", "295米层", 295),
    /** 300米层 */
    D0300("D0300", "0300", "300米层", 300, true),
    /** 310米层 */
    D0310("D0310", "0310", "310米层", 310),
    /** 320米层 */
    D0320("D0320", "0320", "320米层", 320),
    /** 330米层 */
    D0330("D0330", "0330", "330米层", 330),
    /** 340米层 */
    D0340("D0340", "0340", "340米层", 340),
    /** 350米层 */
    D0350("D0350", "0350", "350米层", 350, true),
    /** 360米层 */
    D0360("D0360", "0360", "360米层", 360),
    /** 370米层 */
    D0370("D0370", "0370", "370米层", 370),
    /** 380米层 */
    D0380("D0380", "0380", "380米层", 380),
    /** 390米层 */
    D0390("D0390", "0390", "390米层", 390),
    /** 400米层 */
    D0400("D0400", "0400", "400米层", 400, true),
    /** 410米层 */
    D0410("D0410", "0410", "410米层", 410),
    /** 420米层 */
    D0420("D0420", "0420", "420米层", 420),
    /** 430米层 */
    D0430("D0430", "0430", "430米层", 430),
    /** 440米层 */
    D0440("D0440", "0440", "440米层", 440),
    /** 450米层 */
    D0450("D0450", "0450", "450米层", 450, true),
    /** 460米层 */
    D0460("D0460", "0460", "460米层", 460),
    /** 470米层 */
    D0470("D0470", "0470", "470米层", 470),
    /** 480米层 */
    D0480("D0480", "0480", "480米层", 480),
    /** 490米层 */
    D0490("D0490", "0490", "490米层", 490),
    /** 500米层 */
    D0500("D0500", "0500", "500米层", 500, true),
    /** 550米层 */
    D0550("D0550", "0550", "550米层", 550),
    /** 600米层 */
    D0600("D0600", "0600", "600米层", 600, true),
    /** 650米层 */
    D0650("D0650", "0650", "650米层", 650),
    /** 700米层 */
    D0700("D0700", "0700", "700米层", 700, true),
    /** 750米层 */
    D0750("D0750", "0750", "750米层", 750),
    /** 800米层 */
    D0800("D0800", "0800", "800米层", 800, true),
    /** 850米层 */
    D0850("D0850", "0850", "850米层", 850),
    /** 900米层 */
    D0900("D0900", "0900", "900米层", 900, true),
    /** 950米层 */
    D0950("D0950", "0950", "950米层", 950),
    /** 1000米层 */
    D1000("D1000", "1000", "1000米层", 1000, true),
    /** 1000米层 */
    D1050("D1050", "1050", "1050米层", 1050),
    /** 1000米层 */
    D1100("D1100", "1100", "1100米层", 1100, true),
    /** 1150米层 */
    D1150("D1150", "1150", "1150米层", 1150),
    /** 1200米层 */
    D1200("D1200", "1200", "1200米层", 1200, true),
    /** 1250米层 */
    D1250("D1250", "1250", "1250米层", 1250),
    /** 1300米层 */
    D1300("D1300", "1300", "1300米层", 1300, true),
    /** 1350米层 */
    D1350("D1350", "1350", "1350米层", 1350),
    /** 1400米层 */
    D1400("D1400", "1400", "1400米层", 1400, true),
    /** 1450米层 */
    D1450("D1450", "1450", "1450米层", 1450),
    /** 1500米层 */
    D1500("D1500", "1500", "1500米层", 1500, true),
    /** 1550米层 */
    D1550("D1550", "1550", "1550米层", 1550, true),
    /** 1600米层 */
    D1600("D1600", "1600", "1600米层", 1600, true),
    /** 1650米层 */
    D1650("D1650", "1650", "1650米层", 1650, true),
    /** 1700米层 */
    D1700("D1700", "1700", "1700米层", 1700, true),
    /** 1750米层 */
    D1750("D1750", "1750", "1750米层", 1750),
    /** 1800米层 */
    D1800("D1800", "1800", "1800米层", 1800, true),
    /** 1850米层 */
    D1850("D1850", "1850", "1850米层", 1850, true),
    /** 1900米层 */
    D1900("D1900", "1900", "1900米层", 1900, true),
    /** 1950米层 */
    D1950("D1950", "1950", "1950米层", 1950, true),
    /** 2000米层 */
    D2000("D2000", "2000", "2000米层", 2000),
    /** 2500米层 */
    D2500("D2500", "2500", "2500米层", 2500),
    /** 3000米层 */
    D3000("D3000", "3000", "3000米层", 3000),
    /** 3500米层 */
    D3500("D3500", "3500", "3500米层", 3500),
    /** 4000米层 */
    D4000("D4000", "4000", "4000米层", 4000),
    /** 4500米层 */
    D4500("D4500", "4500", "4500米层", 4500),
    /** 5000米层 */
    D5000("D5000", "5000", "5000米层", 5000),
    /** 5500米层 */
    D5500("D5500", "5500", "5500米层", 5500),
    ;

     
    
    private String value;
    private String fileValue;
    private String chnname;
    private float floatValue;
    private boolean isSigmaStandLevel;
    
    
    DataLevel(String v, String fileValue, String chnname,float floatValue, boolean isSigmaStandLevel){
    	this.value = v;
    	this.fileValue = fileValue;
    	this.chnname = chnname;
    	this.floatValue = floatValue;
    	this.isSigmaStandLevel = isSigmaStandLevel;
    }
    DataLevel(String v, String fileValue, String chnname,float floatValue){
    	this.value = v;
    	this.fileValue = fileValue;
    	this.chnname = chnname;
    	this.floatValue = floatValue;
    }
    
    /**
     * 是否为sigma分层标准层
     * @return
     */
    public boolean isSigmaStandLevel(){
    	return this.isSigmaStandLevel;
    }
    
//    public static DataLevel getMaxDepth(){
//    	return DataLevel.D5500;
//    }
    
    public String value() {
        return value;
    }
    /**
     * 获取文件数据类型
     * 此方法为数据读取模块内部专用，不建议外部使用
     * @return 文件数据类型
     */
    public String getFileValue(){
    	return this.fileValue;
    }
    /**
     * 为兼容目前层次为四位数字情况临时使用
     * 层次全部改为枚举型后去除该方法
     * @return
     */
    public String getNumValue(){
    	return this.fileValue;
    }
    
    public float getLevelFloatValue(){
    	return floatValue;
    }

    /**
     * 获取中文名
     * @return 中文名
     */
    public String getChnname(){
    	return this.chnname;
    }
    
    
    public static DataLevel fromValue(String v) {
    	String strValue = v;
    	
    	//LiuXC 2015-6-12
    	//以下用于解决构建气压层次还是水深难题。（根据正负判断）
    	//进一步应用：可将fromFileValue方法私有化！提高代码质量！
    	if (!v.startsWith("P") && !v.startsWith("D")){
    		try {
        		int levelNum = Integer.parseInt(strValue);           		
                for (DataLevel c: DataLevel.values()) {                	
                    if (Integer.parseInt(c.fileValue) == Math.abs(levelNum)) {
                    	if ((levelNum <= 0) && (c.value.startsWith("D")))                    	
                    			return c;
                    	else if ((levelNum > 0) && (c.value.startsWith("P")))
                    		return c;
                    }
                }
    		}catch( NumberFormatException e){
    			System.err.println("给定层次枚举的值" + strValue + "不正确！无法构建枚举!");
    			return null;
    		}
    	} else {
            for (DataLevel c: DataLevel.values()) {
            	
                if (c.value.equals(v)) {
                    return c;
                }
            }    		
    	}
        System.err.println("无法构建枚举DataLevel，因无当前输入的信息【"+v+"】的相关枚举类型");
        return null;
    }
    /**
     * 此方法升级后将删除.请使用fromValue！      
     * @deprecated
     * @param fileValue
     * @return
     */
    public static DataLevel fromFileValue(String fileValue) {
        for (DataLevel c: DataLevel.values()) {
            if (c.fileValue.equals(fileValue)) {
                return c;
            }
        }
        System.err.println("无法构建枚举DataLevel，因无当前输入的信息【"+fileValue+"】的相关枚举类型");
        return null;
    }
    
    private static List<DataLevel> allSigmaStandardLevel;
    public static List<DataLevel> getAllSigmaStandardLevel(){
    	if(allSigmaStandardLevel != null)
    		return allSigmaStandardLevel;
    	
    	allSigmaStandardLevel = new ArrayList<DataLevel>();
    	for (DataLevel c: DataLevel.values()) {
			if(!c.value().subSequence(0, 1).equals("D") && !c.isSigmaStandLevel())
				continue;
			allSigmaStandardLevel.add(c);
        }
    	return allSigmaStandardLevel;
    }
    /**
     * 获取SIGMA分层的标准层
     * @param startLevel
     * @param endLevel
     * @return
     */
    public static List<DataLevel> getSigmaStandardLevel(DataLevel startLevel,DataLevel endLevel){
    	if(startLevel == null && endLevel == null ){
    		return getAllSigmaStandardLevel();
    	}
    	ArrayList<DataLevel> levels = new ArrayList<DataLevel>();
    	if(startLevel != null && endLevel != null){
    		for (DataLevel c: getAllSigmaStandardLevel()) {
                if (c.getLevelFloatValue() >= startLevel.getLevelFloatValue() &&
                		c.getLevelFloatValue() <= endLevel.getLevelFloatValue()) {
                	levels.add(c);
                }
            }
    		return levels;
    	}
    	if(startLevel != null ){
    		for (DataLevel c: getAllSigmaStandardLevel()) {
                if (c.getLevelFloatValue() >= startLevel.getLevelFloatValue()) {
                	levels.add(c);
                }
            }
    		 return levels;
    	}
    	if(endLevel != null){
    		for (DataLevel c: getAllSigmaStandardLevel()) {
                if (c.getLevelFloatValue() <= endLevel.getLevelFloatValue()) {
                	levels.add(c);
                }
            }
    		 return levels;
    	}
    	return null;
    }
    
    
    
    public static String[] getDataLevel(String v) {
    	ArrayList<String> levels = new ArrayList<String>();
        for (DataLevel c: DataLevel.values()) {
            if (c.value.substring(1).equals(v)) {
            	levels.add(c.getFileValue());
            }
        }
        if(levels.size() == 0)
        	return null;
        String[] ss = new String[levels.size()];
        for(int i=0;i<levels.size();i++)
        	ss[i] = levels.get(i);
        return ss;
    }
    
    public static DataLevel[] getAllDepthLevel() {
    	ArrayList<DataLevel> levels = new ArrayList<DataLevel>();
        for (DataLevel c: DataLevel.values()) {
            if (c.value.substring(0,1).toUpperCase().equals("D")) {
            	levels.add(c);
            }
        }
        if(levels.size() == 0)
        	return null;
        DataLevel[] ss = new DataLevel[levels.size()];
        for(int i=0;i<levels.size();i++)
        	ss[i] = levels.get(i);
        return ss;
    }
    
    public static void main(String[] args) {
    	System.out.println(DataLevel.fromValue("9999"));
    	System.out.println(DataLevel.fromValue("99999"));
    	System.out.println(DataLevel.fromValue("PSurf"));
    	System.out.println(DataLevel.fromValue("0"));
    	System.out.println(DataLevel.fromValue("1000"));
    	System.out.println(DataLevel.fromValue("-1000"));
       	System.out.println(DataLevel.fromValue("-9999.9"));
       	System.out.println(DataLevel.fromValue("D10"));
       	System.out.println(DataLevel.fromValue("10"));
       	System.out.println(DataLevel.fromValue("P0010"));
       	System.out.println(DataLevel.fromValue("0010"));
       	System.out.println(DataLevel.fromValue("-010"));
       	System.out.println(DataLevel.fromValue("927"));
       	System.out.println(DataLevel.fromValue("420"));
       	System.out.println(DataLevel.fromValue("-420"));
       	System.out.println(DataLevel.fromValue("D0055"));
    }
    
    
//    public enum PressLevel extends DataLevel{
//    	
//    }
    
//    public interface DataLevel1{
//    	DataLevel1[] getAllDepthLevel();
//    }
//    
//    public enum PressLevel implements DataLevel1{
//    	//气压 100,200,300,500,700,850,925,1000,9999
//    	/**  气压 0010  */
//        P0010("P0010","0010", "10hPa",10f),
//        /**  气压 0020  */
//        P0020("P0020","0020", "20hPa",20f),
//        /**  气压 0030  */
//        P0030("P0030","0030", "30hPa",30f),
//        /**  气压 0050  */
//        P0050("P0050","0050", "50hPa",50f),
//        /**  气压 0070  */
//        P0070("P0070","0070", "70hPa",70f),
//    	/**  气压 0100  */
//        P0100("P0100","0100", "100hPa",100f),
//        /**  气压 0150  */
//        P0150("P0150","0150", "150hPa",150f),
//        /**  气压 0200 */
//        P0200("P0200","0200", "200hPa",200f),
//        /**  气压 0250 
//         * 传入250，转成了P0250!无法转成D0250!
//         * */
//        P0250("P0250","0250", "250hPa",250f),
//        /**  气压 0300 */
//        P0300("P0300","0300", "300hPa",300f),
//        /**  气压 0400 */
//        P0400("P0400","0400", "400hPa",400f),
//        /**  气压 0500	 */
//        P0500("P0500","0500", "500hPa",500f),
//        /**  气压 0600 */
//        P0600("P0600","0600", "600hPa",600f),
//        /**  气压 0700	 */
//        P0700("P0700","0700", "700hPa",700f),
//        /**  气压 0850	 */
//        P0850("P0850","0850", "850hPa",850f),
//        /**  气压 0925	 */
//        P0925("P0925","0925", "925hPa",925f),
//        /**  气压 1000	 */
//        P1000("P1000","1000", "1000hPa",1000f),
//        /**  气压 9999	 */
//        PSurf("PSurf", "9999", "地面",9999f),
//        /** 位势稳定度的层次 */
//        P500_850("P500_850", "0585", "500hPa-850hPa",500850f),
//        /** 垂直风切变700-300hPa层次 */
//        P700_300("P700_300", "0730", "700hPa-300hPa",700600f),
//        
//        //垂直风切变\理查逊数用到层次
//        /** 850hPa层 */
//        P925_700("P925_700", "0927", "925hPa-700hPa",925700f),
//        /** 700hPa层 */
//        P850_500("P850_500", "0855", "850hPa-500hPa",850500f),
//        /** 500hPa层 */
//        P700_400("P700_400", "0740", "700hPa-400hPa",700400f),
//        /** 400hPa层 */
//        P500_300("P500_300", "0530", "500hPa-300hPa",500300f),
//        /** 300hPa层 */
//        P400_200("P400_200", "0420", "400hPa-200hPa",400200f),
//        /** 200hPa层 */
//        P300_100("P300_100", "0310", "300hPa-100hPa",300100f),
//        ;
//		@Override
//		public DataLevel1[] getAllDepthLevel() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		
//		private String value;
//	    private String fileValue;
//	    private String chnname;
//	    private float floatValue;
//	    private boolean isSigmaStandLevel;
//	    
//	    
//	    PressLevel(String v, String fileValue, String chnname,float floatValue, boolean isSigmaStandLevel){
//	    	this.value = v;
//	    	this.fileValue = fileValue;
//	    	this.chnname = chnname;
//	    	this.floatValue = floatValue;
//	    	this.isSigmaStandLevel = isSigmaStandLevel;
//	    }
//	    PressLevel(String v, String fileValue, String chnname,float floatValue){
//	    	this.value = v;
//	    	this.fileValue = fileValue;
//	    	this.chnname = chnname;
//	    	this.floatValue = floatValue;
//	    }
//	    
//	    /**
//	     * 是否为sigma分层标准层
//	     * @return
//	     */
//	    public boolean isSigmaStandLevel(){
//	    	return this.isSigmaStandLevel;
//	    }
//	    
//	    public String value() {
//	        return value;
//	    }
//	    /**
//	     * 获取文件数据类型
//	     * 此方法为数据读取模块内部专用，不建议外部使用
//	     * @return 文件数据类型
//	     */
//	    public String getFileValue(){
//	    	return this.fileValue;
//	    }
//	    /**
//	     * 为兼容目前层次为四位数字情况临时使用
//	     * 层次全部改为枚举型后去除该方法
//	     * @return
//	     */
//	    public String getNumValue(){
//	    	return this.fileValue;
//	    }
//	    
//	    public float getLevelFloatValue(){
//	    	return floatValue;
//	    }
//
//	    /**
//	     * 获取中文名
//	     * @return 中文名
//	     */
//	    public String getChnname(){
//	    	return this.chnname;
//	    }
//    	
//    }
//    public enum DepthLevel implements DataLevel1{
//    	//海洋水文
//        /** 表层 */
//        D0000("D0000", "0000", "表层", 0,true),
//        /** 5米层 */
//        D0005("D0005", "0005", "5米层", 5,true),
//        /** 10米层 */
//        D0010("D0010", "0010", "10米层", 10,true),
//        /** 15米层 */
//        D0015("D0015", "0015", "15米层", 15,true),
//        /** 20米层 */
//        D0020("D0020", "0020", "20米层", 20,true),
//        /** 25米层 */
//        D0025("D0025", "0025", "25米层", 25,true),
//        /** 30米层 */
//        D0030("D0030", "0030", "30米层", 30,true),
//        /** 35米层 */
//        D0035("D0035", "0035", "35米层", 35,true),
//        /** 40米层 */
//        D0040("D0040", "0040", "40米层", 40,true),
//        /** 45米层 */
//        D0045("D0045", "0045", "45米层", 45,true),
//        /** 50米层 */
//        D0050("D0050", "0050", "50米层", 50,true),
//        /** 55米层 */
//        D0055("D0055", "0055", "55米层", 55,true),
//        /** 60米层 */
//        D0060("D0060", "0060", "60米层", 60,true),
//        /** 65米层 */
//        D0065("D0065", "0065", "65米层", 65,true),
//        /** 70米层 */
//        D0070("D0070", "0070", "70米层", 70,true),
//        /** 75米层 */
//        D0075("D0075", "0075", "75米层", 75,true),
//        /** 80米层 */
//        D0080("D0080", "0080", "80米层", 80,true),
//        /** 85米层 */
//        D0085("D0085", "0085", "85米层", 85,true),
//        /** 90米层 */
//        D0090("D0090", "0090", "90米层", 90,true),
//        /** 95米层 */
//        D0095("D0095", "0095", "95米层", 95,true),
//        /** 100米层 */
//        D0100("D0100", "0100", "100米层", 100,true),
//        /** 105米层 */
//        D0105("D0105", "0105", "105米层", 105),
//        /** 110米层 */
//        D0110("D0110", "0110", "110米层", 110, true),
//        /** 115米层 */
//        D0115("D0115", "0115", "115米层", 115),
//        /** 120米层 */
//        D0120("D0120", "0120", "120米层", 120, true),
//        /** 125米层 */
//        D0125("D0125", "0125", "125米层", 125),
//        /** 130米层 */
//        D0130("D0130", "0130", "130米层", 130, true),
//        /** 135米层 */
//        D0135("D0135", "0135", "135米层", 135),
//        /** 140米层 */
//        D0140("D0140", "0140", "140米层", 140, true),
//        /** 145米层 */
//        D0145("D0145", "0145", "145米层", 145),
//        /** 150米层 */
//        D0150("D0150", "0150", "150米层", 150, true),
//        /** 155米层 */
//        D0155("D0155", "0155", "155米层", 155),
//        /** 160米层 */
//        D0160("D0160", "0160", "160米层", 160, true),
//        /** 165米层 */
//        D0165("D0165", "0165", "165米层", 165),
//        /** 170米层 */
//        D0170("D0170", "0170", "170米层", 170, true),
//        /** 175米层 */
//        D0175("D0175", "0175", "175米层", 175),
//        /** 180米层 */
//        D0180("D0180", "0180", "180米层", 180, true),
//        /** 185米层 */
//        D0185("D0185", "0185", "185米层", 185),
//        /** 190米层 */
//        D0190("D0190", "0190", "190米层", 190, true),
//        /** 195米层 */
//        D0195("D0195", "0195", "195米层", 195),
//        /** 200米层 */
//        D0200("D0200", "0200", "200米层", 200, true),
//        /** 205米层 */
//        D0205("D0205", "0205", "205米层", 205),
//        /** 210米层 */
//        D0210("D0210", "0210", "210米层", 210, true),
//        /** 215米层 */
//        D0215("D0215", "0215", "215米层", 215),
//        /** 220米层 */
//        D0220("D0220", "0220", "220米层", 220, true),
//        /** 225米层 */
//        D0225("D0225", "0225", "225米层", 225),
//        /** 230米层 */
//        D0230("D0230", "0230", "230米层", 230, true),
//        /** 235米层 */
//        D0235("D0235", "0235", "235米层", 235),
//        /** 240米层 */
//        D0240("D0240", "0240", "240米层", 240, true),
//        /** 245米层 */
//        D0245("D0245", "0245", "245米层", 245),
//        /** 250米层 */
//        D0250("D0250", "0250", "250米层", 250, true),
//        /** 255米层 */
//        D0255("D0255", "0255", "255米层", 255),
//        /** 260米层 */
//        D0260("D0260", "0260", "260米层", 260, true),
//        /** 265米层 */
//        D0265("D0265", "0265", "265米层", 265),
//        /** 270米层 */
//        D0270("D0270", "0270", "270米层", 270, true),
//        /** 275米层 */
//        D0275("D0275", "0275", "275米层", 275),
//        /** 280米层 */
//        D0280("D0280", "0280", "280米层", 280, true),
//        /** 285米层 */
//        D0285("D0285", "0285", "285米层", 285),
//        /** 290米层 */
//        D0290("D0290", "0290", "290米层", 290, true),
//        /** 295米层 */
//        D0295("D0295", "0295", "295米层", 295),
//        /** 300米层 */
//        D0300("D0300", "0300", "300米层", 300, true),
//        /** 310米层 */
//        D0310("D0310", "0310", "310米层", 310),
//        /** 320米层 */
//        D0320("D0320", "0320", "320米层", 320),
//        /** 330米层 */
//        D0330("D0330", "0330", "330米层", 330),
//        /** 340米层 */
//        D0340("D0340", "0340", "340米层", 340),
//        /** 350米层 */
//        D0350("D0350", "0350", "350米层", 350, true),
//        /** 360米层 */
//        D0360("D0360", "0360", "360米层", 360),
//        /** 370米层 */
//        D0370("D0370", "0370", "370米层", 370),
//        /** 380米层 */
//        D0380("D0380", "0380", "380米层", 380),
//        /** 390米层 */
//        D0390("D0390", "0390", "390米层", 390),
//        /** 400米层 */
//        D0400("D0400", "0400", "400米层", 400, true),
//        /** 410米层 */
//        D0410("D0410", "0410", "410米层", 410),
//        /** 420米层 */
//        D0420("D0420", "0420", "420米层", 420),
//        /** 430米层 */
//        D0430("D0430", "0430", "430米层", 430),
//        /** 440米层 */
//        D0440("D0440", "0440", "440米层", 440),
//        /** 450米层 */
//        D0450("D0450", "0450", "450米层", 450, true),
//        /** 460米层 */
//        D0460("D0460", "0460", "460米层", 460),
//        /** 470米层 */
//        D0470("D0470", "0470", "470米层", 470),
//        /** 480米层 */
//        D0480("D0480", "0480", "480米层", 480),
//        /** 490米层 */
//        D0490("D0490", "0490", "490米层", 490),
//        /** 500米层 */
//        D0500("D0500", "0500", "500米层", 500, true),
//        /** 550米层 */
//        D0550("D0550", "0550", "550米层", 550),
//        /** 600米层 */
//        D0600("D0600", "0600", "600米层", 600, true),
//        /** 650米层 */
//        D0650("D0650", "0650", "650米层", 650),
//        /** 700米层 */
//        D0700("D0700", "0700", "700米层", 700, true),
//        /** 750米层 */
//        D0750("D0750", "0750", "750米层", 750),
//        /** 800米层 */
//        D0800("D0800", "0800", "800米层", 800, true),
//        /** 850米层 */
//        D0850("D0850", "0850", "850米层", 850),
//        /** 900米层 */
//        D0900("D0900", "0900", "900米层", 900, true),
//        /** 950米层 */
//        D0950("D0950", "0950", "950米层", 950),
//        /** 1000米层 */
//        D1000("D1000", "1000", "1000米层", 1000, true),
//        /** 1000米层 */
//        D1050("D1050", "1050", "1050米层", 1050),
//        /** 1000米层 */
//        D1100("D1100", "1100", "1100米层", 1100, true),
//        /** 1150米层 */
//        D1150("D1150", "1150", "1150米层", 1150),
//        /** 1200米层 */
//        D1200("D1200", "1200", "1200米层", 1200, true),
//        /** 1250米层 */
//        D1250("D1250", "1250", "1250米层", 1250),
//        /** 1300米层 */
//        D1300("D1300", "1300", "1300米层", 1300, true),
//        /** 1350米层 */
//        D1350("D1350", "1350", "1350米层", 1350),
//        /** 1400米层 */
//        D1400("D1400", "1400", "1400米层", 1400, true),
//        /** 1450米层 */
//        D1450("D1450", "1450", "1450米层", 1450),
//        /** 1500米层 */
//        D1500("D1500", "1500", "1500米层", 1500, true),
//        /** 1750米层 */
//        D1750("D1750", "1750", "1750米层", 1750),
//        /** 2000米层 */
//        D2000("D2000", "2000", "2000米层", 2000),
//        /** 2500米层 */
//        D2500("D2500", "2500", "2500米层", 2500),
//        /** 3000米层 */
//        D3000("D3000", "3000", "3000米层", 3000),
//        /** 3500米层 */
//        D3500("D3500", "3500", "3500米层", 3500),
//        /** 4000米层 */
//        D4000("D4000", "4000", "4000米层", 4000),
//        /** 4500米层 */
//        D4500("D4500", "4500", "4500米层", 4500),
//        /** 5000米层 */
//        D5000("D5000", "5000", "5000米层", 5000),
//        /** 5500米层 */
//        D5500("D5500", "5500", "5500米层", 5500),
//    	;
//
//		@Override
//		public DataLevel1[] getAllDepthLevel() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		
//		private String value;
//	    private String fileValue;
//	    private String chnname;
//	    private float floatValue;
//	    private boolean isSigmaStandLevel;
//	    
//	    
//	    DepthLevel(String v, String fileValue, String chnname,float floatValue, boolean isSigmaStandLevel){
//	    	this.value = v;
//	    	this.fileValue = fileValue;
//	    	this.chnname = chnname;
//	    	this.floatValue = floatValue;
//	    	this.isSigmaStandLevel = isSigmaStandLevel;
//	    }
//	    DepthLevel(String v, String fileValue, String chnname,float floatValue){
//	    	this.value = v;
//	    	this.fileValue = fileValue;
//	    	this.chnname = chnname;
//	    	this.floatValue = floatValue;
//	    }
//	    
//	    
//	    /**
//	     * 是否为sigma分层标准层
//	     * @return
//	     */
//	    public boolean isSigmaStandLevel(){
//	    	return this.isSigmaStandLevel;
//	    }
//	    
//	    public String value() {
//	        return value;
//	    }
//	    /**
//	     * 获取文件数据类型
//	     * 此方法为数据读取模块内部专用，不建议外部使用
//	     * @return 文件数据类型
//	     */
//	    public String getFileValue(){
//	    	return this.fileValue;
//	    }
//	    /**
//	     * 为兼容目前层次为四位数字情况临时使用
//	     * 层次全部改为枚举型后去除该方法
//	     * @return
//	     */
//	    public String getNumValue(){
//	    	return this.fileValue;
//	    }
//	    
//	    public float getLevelFloatValue(){
//	    	return floatValue;
//	    }
//
//	    /**
//	     * 获取中文名
//	     * @return 中文名
//	     */
//	    public String getChnname(){
//	    	return this.chnname;
//	    }
//    	
//    }
}
