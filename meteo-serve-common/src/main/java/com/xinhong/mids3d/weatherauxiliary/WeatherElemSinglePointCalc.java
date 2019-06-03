package com.xinhong.mids3d.weatherauxiliary;

//public class WeatherElemSinglePointCacl {
//	
//
//}

import java.util.ArrayList;
import java.util.List;

import com.xinhong.mids3d.core.isoline.IsolineUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 各物理量计算类
 *
 * @author
 */
public class WeatherElemSinglePointCalc {

    private static Log logger = LogFactory.getLog(WeatherElemSinglePointCalc.class);

    //工具类 避免产生实例
    private WeatherElemSinglePointCalc() {
    }

    /**
     * 根据风向wd和风速ws计算U分量
     *
     * @param wd 风向
     * @param ws 风速
     * @return U分量
     */
    public static double wind2U(double wd, double ws) {
        if (wd == NULLVAL || ws == NULLVAL) {
            return NULLVAL;
        }
        double tmpAngle = 270.0 - wd;
        return ws * Math.cos(Math.toRadians(tmpAngle));
    }

    /**
     * 根据风向wd和风速ws计算V分量
     *
     * @param wd 风向wd
     * @param ws 风速ws
     * @return V分量
     */
    public static double wind2V(double wd, double ws) {
        if (wd == NULLVAL || ws == NULLVAL) {
            return NULLVAL;
        }
        double tmpAngle = 270.0 - wd;
        return (double) (ws * Math.sin(Math.toRadians(tmpAngle)));
    }

    /**
     * 根据风速wd风向ws计算u和v
     *
     * @param wd 风向
     * @param ws 风速
     * @param u  U分量
     * @param v  V分量
     */
    public static void wind2UVAry(double[][] wd, double[][] ws, double[][] u, double[][] v) {
        int wdRow = wd.length;
        int wdCol = wd[0].length;
        int wsRow = ws.length;
        int wsCol = ws[0].length;
        if (wdRow != wsRow) {
            logger.info("由风速风向计算UV时，风速风向长度不同");
            return;
        }
        if (wdCol != wsCol) {
            logger.info("由风速风向计算UV时，风速风向长度不同");
            return;
        }

        double tmp;
        for (int i = 0; i < wdRow; i++) {
            for (int j = 0; j < wdCol; j++) {
                if (wd[i][j] == NULLVAL || ws[i][j] == NULLVAL) {
                    u[i][j] = NULLVAL;
                    v[i][j] = NULLVAL;
                } else {
                    // /////////
//					tmp = (270.0 - wd[i][j]) * Math.PI / 180.0;
                    tmp = Math.toRadians(270.0 - wd[i][j]);
                    // ////////
                    u[i][j] = (double) (ws[i][j] * Math.cos(tmp));
                    v[i][j] = (double) (ws[i][j] * Math.sin(tmp));
                }
            }
        }
    }

    /**
     * 根据U和V计算风向
     *
     * @param u U分量
     * @param v V分量
     * @return 风向
     */
    public static double uv2WD(double u, double v) {
        if (u == NULLVAL || v == NULLVAL) {
            return NULLVAL;
        }
        // 计算风向
        double wd = 0.0;
        if (u == 0) {
            if (v > 0.0) {
//				wd = 180.0;
                wd = 360.0;
            } else if (v < 0.0) {
//				wd = 360.0;
                wd = 180.0;
            } else {
                wd = 0.0;//静风
            }
        } else {
            double tmpAngle = Math.toDegrees(Math.atan(v / u));
            // if(v>=0.0 && u>0.0){
            // wd = 270.0 - tmpAngle;
            // }
            // if(v<0.0 && u>0.0){
            // wd = 270.0 + tmpAngle;
            // }
            // if(v>=0 && u<0.0){
            // wd = 90.0 + tmpAngle;
            // }
            // if(v<0.0 && u<0.0){
            // wd = 90.0 - tmpAngle;
            // }
            if (u > 0.0f) {
                wd = 270.0 - tmpAngle;
            }
            if (u < 0.0f) {
                wd = 90.0 - tmpAngle;
            }
        }
        return (double) wd;

    }

    /**
     * 根据U和V计算风速
     *
     * @param u U分量
     * @param v V分量
     * @return
     */
    public static double uv2WS(double u, double v) {
        // 计算风速
        if (u == NULLVAL || v == NULLVAL) {
            return NULLVAL;
        }
        return (double) Math.sqrt(u * u + v * v);
    }

    /**
     * 根据U和V计算风速
     *
     * @param u    U分量
     * @param v    V分量
     * @param rate 放大倍数
     * @return
     */
    public static double uv2WS(double u, double v, double rate) {
        // 计算风速
        if (u == NULLVAL || v == NULLVAL) {
            return NULLVAL;
        }
        return (double) Math.sqrt(u * u + v * v) * rate;
    }

    /**
     * 根据U和V计算风速ws和风向wd
     *
     * @param u  U分量
     * @param v  V分量
     * @param wd 风向
     * @param ws 风速
     */
    public static void uv2WindAry(double[][] u, double[][] v, double[][] wd,
                                  double[][] ws) {
        int uRow = u.length;
        int uCol = u[0].length;
        int vRow = v.length;
        int vCol = v[0].length;
        if (uRow != vRow) {
            logger.info("由UV计算风速风向时，UV长度不同");
            return;
        }
        if (uCol != vCol) {
            logger.info("由UV计算风速风向时，UV长度不同");
            return;
        }

        // double tmp;
        for (int i = 0; i < uRow; i++) {
            for (int j = 0; j < uCol; j++) {
                if (u[i][j] == NULLVAL || v[i][j] == NULLVAL) {
                    ws[i][j] = NULLVAL;
                    wd[i][j] = NULLVAL;
                } else {
                    ws[i][j] = (double) Math.sqrt(u[i][j] * u[i][j] + v[i][j] * v[i][j]);
                    wd[i][j] = uv2WD(u[i][j], v[i][j]);
                    // tmp=270.0-Math.toDegrees(Math.atan2(v[i][j],u[i][j]));
                    // wd[i][j]=(double) (tmp%360.0);
                }
            }
        }
    }

    /**
     * 根据U和V计算风向WD
     *
     * @param u  U分量
     * @param v  V分量
     * @param wd 风向WD
     */
    public static void uv2WD(double[][] u, double[][] v, double[][] wd) {
        int uRow = u.length;
        int uCol = u[0].length;
        int vRow = v.length;
        int vCol = v[0].length;
        if (uRow != vRow) {
            logger.info("由UV计算风速风向时，UV长度不同");
            return;
        }
        if (uCol != vCol) {
            logger.info("由UV计算风速风向时，UV长度不同");
            return;
        }

        // double tmp;
        for (int i = 0; i < uRow; i++) {
            for (int j = 0; j < uCol; j++) {
                wd[i][j] = uv2WD(u[i][j], v[i][j]);
                // tmp=270.0-Math.toDegrees(Math.atan2(v[i][j],u[i][j]));
                // wd[i][j]=(double) (tmp%360.0);
            }
        }
    }

    /**
     * 根据U和V计算风速WS
     *
     * @param u  U分量
     * @param v  V分量
     * @param ws 风速WS
     */
    public static void uv2WS(double[][] u, double[][] v, double[][] ws) {
        int uRow = u.length;
        int uCol = u[0].length;
        int vRow = v.length;
        int vCol = v[0].length;
        if (uRow != vRow) {
            logger.info("由UV计算风速风向时，UV长度不同");
            return;
        }
        if (uCol != vCol) {
            logger.info("由UV计算风速风向时，UV长度不同");
            return;
        }

        // double tmp;
        for (int i = 0; i < uRow; i++) {
            for (int j = 0; j < uCol; j++) {
                if (u[i][j] == NULLVAL || v[i][j] == NULLVAL) {
                    ws[i][j] = NULLVAL;
                } else {
                    ws[i][j] = (double) Math.sqrt(u[i][j] * u[i][j] + v[i][j] * v[i][j]);
                }
            }
        }
    }


    /**
     * 根据U和V计算流速
     *
     * @param u    U分量
     * @param v    V分量
     * @param rate 放大倍数 针对海流
     * @return 流速
     */
    public static double[][] uv2WS(double[][] u, double[][] v, double rate) {
        int uRow = u.length;
        int uCol = u[0].length;
        int vRow = v.length;
        int vCol = v[0].length;
        if (uRow != vRow) {
            logger.info("由UV计算风速风向时，UV长度不同");
            return null;
        }
        if (uCol != vCol) {
            logger.info("由UV计算风速风向时，UV长度不同");
            return null;
        }

        double[][] ws = new double[uRow][uCol];
        // double tmp;
        for (int i = 0; i < uRow; i++) {
            for (int j = 0; j < uCol; j++) {
                if (u[i][j] == NULLVAL || v[i][j] == NULLVAL) {
                    ws[i][j] = NULLVAL;
                } else {
                    ws[i][j] = (double) Math.sqrt(u[i][j] * u[i][j] + v[i][j] * v[i][j]) * rate;
                }
            }
        }
        return ws;
    }

    /**
     * 计算K指数 单站或单点(主要针对预报产品) K=(T_850-T_500 )+T_d850-(T-T_d )700  //(T-T_d)700表示T_700-T_d700;
     *
     * @param tt850 850hPa的温度 单位：℃
     * @param tt500 500hPa的温度 单位：℃
     * @param th850 850hPa的温度露点差 单位：℃
     * @param th700 700hPa温度露点差 单位：℃
     * @return K指数, 单位：℃
     */
    public static double kExponent(double tt850, double tt500, double th850, double th700) {
//		double absoluteTT850 = tt850 + T0;
//		double absoluteTT500 = tt500 + T0;
//		double absoluteTD850 = td850 + T0;
//		double absoluteTH700 = th700 + T0;
//
//		return (absoluteTT850-absoluteTT500) + absoluteTD850 -absoluteTH700;

        if (tt850 == NULLVAL || tt500 == NULLVAL || th850 == NULLVAL || th700 == NULLVAL) {
            logger.info("有参数为空值，不能计算");
            return NULLVAL;
        }
        double td850 = tt850 - th850;
        return tt850 - tt500 + td850 - th700;
    }

    /**
     * 计算K指数 单站或单点
     *
     * @param tt850 850hPa的温度, 单位：℃
     * @param tt500 500hPa的温度, 单位：℃
     * @param td850 850hPa的露点温度, 单位：℃
     * @param tt700 700hPa的温度, 用于计算700hPa的温度露点差, 单位：℃
     * @param td700 700hPa的露点温度, 用于计算700hPa的温度露点差, 单位：℃
     * @return K指数, 单位：℃
     */
    public static double kExponent(double tt850, double tt500, double td850, double tt700, double td700) {
        return kExponent(tt850, tt500, (tt850 - td850), (tt700 - td700));
    }

    /**
     * 数据(维数)是否相同
     *
     * @param dimList
     * @return
     */
    public static boolean isSameDimension(List<Integer> dimList) {
        for (int i = 0; i < dimList.size() - 1; i++) {
            if (!dimList.get(i).equals(dimList.get(i + 1))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算强天气威胁指数 单站或单点
     * I=12T_d850+20(TT-49)+2f_850+f_500+125(S+0.2)
     * 说明：I 代表SWEAT；
     * T_d850为850hPa露点温度，若是负数，此项为0；
     * 全总指数TT=（T850+Td850）-2T500，若TT小于49，则20(TT-49)项等于0；
     * f_850为850hPa风速（海里/小时），以米/秒为单位的风速应乘以2；//注意速度*2
     * f_500为500hPa风速（海里/小时），以米/秒为单位的风速应乘以2；//注意速度*2
     * S=sin(α_500-α_850 )，α_500 、α_850分别代表500hPa风向与850hPa风向；
     * 最后一项125(S+0.2)在下列4个条件中任意不具备时为零：
     * 850hPa风向在130-250°之间， 500hPa风向在210-310°之间，
     * 500hPa风向减850hPa风向为正，850hPa及500hPa风速至少等于15海里/小时（7.5米/秒）
     *
     * @param th850  850hPa温度露点差, 单位：℃
     * @param tt850  850hPa的温度, 单位：℃
     * @param tt500  500hPa的温度, 单位：℃
     * @param uu850  850hPa的风U分量             参数直接传风速和风向吧 ，U、V不是直接观测数据（db_x）
     * @param vv850  850hPa的风V分量
     * @param uu500  500hPa的风U分量
     * @param vv500  500hPa的风V分量
     * @param isWDWS uuvv是否是风向风速
     * @param isTH   th850是th还是td
     * @return 强天气威胁指数 无量纲
     */
    public static double sweat(double th850, double tt850, double tt500, double uu850, double vv850, double uu500, double vv500, boolean isWDWS, boolean isTH) {
        double wd850 = uu850;
        double ws850 = vv850;
        double wd500 = uu500;
        double ws500 = vv500;
        if (!isWDWS) {
            wd850 = uv2WD(uu850, vv850);
            ws850 = uv2WS(uu850, vv850);
            wd500 = uv2WD(uu500, vv500);
            ws500 = uv2WS(uu500, vv500);
        }
        double td850 = th850;
        if (isTH) {
            td850 = tt850 - th850;
        }

        if (td850 == NULLVAL || tt850 == NULLVAL || tt500 == NULLVAL || ws850 == NULLVAL || wd850 == NULLVAL || ws500 == NULLVAL || wd500 == NULLVAL) {
            logger.info("有参数为空值，不能计算");
            return NULLVAL;
        }
        double s = Math.sin(Math.toRadians(wd500 - wd850));
        double tmp = 0;
        if (wd850 >= 130 && wd850 <= 250 && wd500 >= 210 && wd500 <= 310 &&
                (wd500 - wd850) > 0 && ws850 >= 7.5 && ws500 >= 7.5) {
            tmp = 125 * (s + 0.2);
        }
        double tt = (tt850 + td850) - 2 * tt500;
        if (tt < 49) {
            tt = 49;
        }
        if (td850 < 0) {
            td850 = 0;
        }

        return (double) (12 * td850 + 20 * (tt - 49) + 2 * 2 * ws850 + 2 * ws500 + tmp);
    }

    /**
     * 计算强天气威胁指数 单站或单点   参数直接传观测值 db_x
     *
     * @param td850 850hPa露点温度, 单位：℃
     * @param tt850 850hPa的温度, 单位：℃
     * @param tt500 500hPa的温度, 单位：℃
     * @param ws850 850hPa的风速
     * @param wd850 850hPa的风向
     * @param ws500 500hPa的风速
     * @param wd500 500hPa的风向
     * @return 强天气威胁指数 无量纲
     */
    public static double swCalc(double td850, double tt850, double tt500, double ws850, double wd850, double ws500, double wd500) {
        if (td850 == NULLVAL || tt850 == NULLVAL || tt500 == NULLVAL || ws850 == NULLVAL || wd850 == NULLVAL || ws500 == NULLVAL || wd500 == NULLVAL) {
            logger.info("有参数为空值，不能计算");
            return NULLVAL;
        }
        double s = Math.sin(Math.toRadians(wd500 - wd850));
        double tmp = 0;
        if (wd850 >= 130 && wd850 <= 250 && wd500 >= 210 && wd500 <= 310 &&
                (wd500 - wd850) > 0 && ws850 >= 7.5 && ws500 >= 7.5) {
            tmp = 125 * (s + 0.2);
        }
        double tt = (tt850 + td850) - 2 * tt500;
        if (tt < 49) {
            tt = 49;
        }
        if (td850 < 0) {
            td850 = 0;
        }

        return (double) (12 * td850 + 20 * (tt - 49) + 2 * 2 * ws850 + 2 * ws500 + tmp);
    }
    //test
//	public static void main(String[] args) {
//		double res = swCalc(-6.7f, -2.5f, -19.7f, 2.1f, 140, 13.4f, 275);
//		System.out.println(res);
//	}

    /**
     * 位势稳定度指数（对流性稳定度指数）
     *
     * @param upTB   上层大气压的假相当位温 db_x
     * @param downTB
     * @return 位势稳定度指数: >0 对流性稳定，<0 对流性不稳定，单位：℃
     */
    public static double conve(double upTB, double downTB) {
        return upTB - downTB;
    }

    /**
     * 水的蒸发潜热evaporation of water latent heat
     *
     * @param tc tc为凝结高度上的绝对温度 单位°K
     * @return 水的蒸发潜热 单位：卡.克-1
     */
    private static double waterLatentHeatKelvin(double tc) {
        if (tc == NULLVAL) {
            return NULLVAL;
        }
        return L0 - CL * (tc - T0);
    }

    /**
     * 水的蒸发潜热evaporation of water llatent heat
     *
     * @param tc tc为凝结高度上的摄氏温度 单位 ℃
     * @return 水的蒸发潜热 单位：卡.克-1
     */
    private static double waterLatentHeatCelsius(double tc) {
        if (tc == NULLVAL) {
            return NULLVAL;
        }
        return L0 - CL * tc;
    }

    //	public static double Rd = (double) (6.8557782f*10e-2);//干空气的比气体常数，单位：卡.克-1.度-1
    public static double Rd = 287.05f;//干空气的比气体常数，单位：J.Kg-1.K-1
    public static double Cp1 = 0.24f;//干空气的定压比热, 单位：卡.克-1.度-1
    //	public static double Cpd = 0.2403f;//干空气的定压比热, 卡.克-1.开-1
    public static double Cpd = 1004f;//干空气的定压比热, 单位：J.Kg-1.K-1
    public static double Cp = 3.4f;//干空气的定压比热, 单位：J.克-1.度-1
    public static double Kd = Rd / Cpd;//干空气的自由膨胀指数，即泊松方程指数

    public static double L0 = 597.4f;//水汽的凝结潜热 单位：卡.克-1
    public static double Latent = 8512.08f;//凝结潜热 单位：J／g
    public static double CL = 0.57f;//水汽的凝结潜热随温度的变化率 单位 卡.克-1.开-1

    public static double T0 = 273.16f;//0度时的绝对温度 单位 °K
    public static double E0 = 6.11f;//0度的饱和水汽压 单位：hPa
    public static double CONSTANTVal = 0.622f;//常数： Rd/Rv = 0.622 Rd:干空气的比气体常数 287.05 J.Kg-1.K-1; Rv:水汽的比气体常数 461.5 J.Kg-1.K-1
    public static double Dq = (double) 1E-4;//
    public static double ConstG = 9.8f;//重力加速度
    public static double ConstWaterA = 7.5f;//水面
    public static double ConstWaterB = 237.3f;//p2:a=7.63,b=241.9
    public static double ConstIceA = 9.5f;//冰面
    public static double ConstIceB = 265.5f;//

    //此方法到底应不应该用Kd(cj)代替 Latent/Cp(db_x)

    /**
     * 凝结高度处温度Tk
     *
     * @param td 露点温度 摄氏温度
     * @param tt 温度 摄氏温度
     * @return 绝对温度
     */
    public static double tk(double td, double tt) {
        if (td == NULLVAL || tt == NULLVAL) {
            return NULLVAL;
        }
        double absoluteTD = T0 + td;//绝对温度
        double absoluteTT = T0 + tt;
//		return (double) (absoluteTD*(CONSTANTVal*Latent/Cp/absoluteTD-1)/((CONSTANTVal*Latent/Cp/absoluteTD-1)+(Math.log(absoluteTT/absoluteTD))));
        return (double) (absoluteTD * (CONSTANTVal * Kd / absoluteTD - 1) / ((CONSTANTVal * Kd / absoluteTD - 1) + (Math.log(absoluteTT / absoluteTD))));

    }

    /**
     * 凝结高度处气压Pk
     *
     * @param t0
     * @param p0
     * @param tk
     * @return
     */
    public static double pk(double t0, double p0, double tk) {
        return (double) (p0 / (Math.pow(t0 / tk, 1 / 0.288f)));
    }

    //等饱和比湿方程dy1/dx1 dy1/dt
    private static double dy1dt(double tk, double deltT) {
        return (double) (-5434 / Math.pow((tk - deltT + T0), 2));
    }

    //干绝热方程dy2/dx2 dy2/dt
    private static double dy2dt(double tk) {
        return (double) (-1 / 0.288f * (1 / (tk + 2.5f * Dq * 1E3 + T0)));
    }

    // Δt
    private static double deltaT(double tk, double pk) {
//		return  (double) (tk - Math.log(Math.pow(Math.E, tk) - Dq/622.0f/6.11f*pk));
//		return  (double) (tk - Math.log(Math.exp(tk) - Dq/622.0f/6.11f*pk));
        double tmp = saturationVP(tk) - Dq / 622.0f / E0 * pk;
        return tk - getTFromVP(tmp);
    }

    //dt
    private static double dT(double deltaT, double tk) {
        double tmp1 = deltaT;
        double tmp2 = dy1dt(tk, tmp1);
        double tmp3 = dy2dt(tk);

        return (tmp1 + 0.25f) / (tmp2 / tmp3 - 1);
    }

    //湿绝热线上T
    private static double wetAdiabatT(double tk, double deltaT, double dT) {
        return tk - deltaT - dT;
    }

    //由tk和pk计算假相当位温
    private static double wetTb(double tk, double pk) {
        double mixingRatio = (double) (CONSTANTVal * saturationVP(tk - T0) / pk);
        return (double) (tk * Math.pow(1000.0f / pk, Kd) * Math.exp((L0 / Cp1) * mixingRatio / tk));
//		double mixingRatio = isobaricSurfaceMixingRatioFromVPPP(saturationVP(tk-T0),pk);//等压面上的混合比
//		return (double) (tk*Math.pow(1000.0f/pk, Kd)*Math.exp(Kd*mixingRatio/tk));
    }

    /**
     * 气块自850hPa高度上绝热上升到500hPa高度所具有的温度
     *
     * @param tt500
     * @param tt
     * @param td
     * @return
     */
    private static double parcelT(double tt500, double tt, double td) {
        double pp = 850;
        double tk = tk(td, tt) - T0;//计算凝结高度处温度Tk
        double pk = pk(tt, pp, tk);//计算凝结高度处气压Pk
        double wetP = 500.0f;
        double tb = wetTb(tk + T0, pk);//计算假相当位温 湿绝热线上假相当位温tb守恒 不变是个常数
//		double tb = (double) setase(pk, tk);//计算假相当位温
        double deltaT = deltaT(tk, pk);
        double dT = dT(deltaT, tk);
        double wetT = wetAdiabatT(tk, deltaT, dT);
        double tmpTB = wetTb(wetT + T0, wetP);
        double tb0 = tmpTB;
        double wetT0 = wetT;
        while (tmpTB - tb > 0) {
            tb0 = tmpTB;
            wetT0 = wetT;
            wetT = wetAdiabatT(wetT, deltaT, dT);
            tmpTB = wetTb(wetT + T0, wetP);
        }
        if (Math.abs(tb0 - tb) < Math.abs(tmpTB - tb)) {
            wetT = wetT0;
        }
//		System.out.println(Math.abs(tb0-tb));
//		System.out.println(Math.abs(tmpTB-tb));
//		System.out.println("---------");
        return wetT;
    }

    /**
     * 假相当位温(zcj)
     *
     * @param pp
     * @param tt
     * @return
     */
    protected static double setase(double pp, double tt) {
        if (pp == NULLVAL || tt == NULLVAL) {
            return NULLVAL;
        }
        double es = saturationVP(tt);
        double lv = 2500000.0f - 2368.0f * tt;
        double ws = 0.622f * es / (pp - es);
        return (double) ((T0 + tt) * Math.pow((1000.0f / (pp - es)), (Rd / Cpd)) * Math.exp(lv * ws / (Cpd * (T0 + tt))));
    }

    /**
     * @param setase
     * @param pp
     * @return
     */
    protected static double t_setase(double setase, double pp) {
        if (setase == NULLVAL || pp == NULLVAL) {
            return NULLVAL;
        }

        double t = -84.5f;
        if (setase(pp, t) > setase) {
            return NULLVAL;
        } else {
            double delt = 0.1f;
            do {
                t = t + delt;
            } while (setase(pp, t) < setase);

            if (setase(pp, t) > setase) {
                double res = t - delt + delt * (setase - setase(pp, (t - delt))) / (setase(pp, t) - setase(pp, (t - delt)));
                return res;
            }
            return t;
        }
    }

    /**
     * 计算沙氏指数(IDL)
     *
     * @param tt500
     * @param tt850
     * @param th850
     * @param isTD
     * @return
     */
    public static double calculateSSI(double tt500, double tt850, double th850, boolean isTD) {
        if (tt500 == NULLVAL || tt850 == NULLVAL || th850 == NULLVAL) {
            return NULLVAL;
        }
        double td850 = th850;
        if (!isTD) {
            td850 = tt850 - th850;
        }
        double tk = tk(td850, tt850) - T0;//计算凝结高度处温度Tk
        double pk = pk(tt850, 850, tk);//计算凝结高度处气压Pk
        double ts = t_setase(setase(pk, tk), 500.0f);
        double res = tt500 - ts;
        return (double) res;
    }

    /**
     * 计算沙氏指数(IDL)
     *
     * @param tt500
     * @param tt850
     * @param th850
     * @param isTD
     * @return
     */
    public static double[] calculateSSI(double[] tt500, double[] tt850, double[] th850, boolean isTD) {
        List<Integer> dimList = new ArrayList<Integer>(4);
        dimList.add(tt500.length);
        dimList.add(tt850.length);
        dimList.add(th850.length);
        if (!isSameDimension(dimList)) {
            logger.info("In WeatherElemCalc中计算沙氏指数时传入的参数维数不同，不能计算");
            return null;
        }

        int row = tt500.length;
        double[] res = new double[row];
        for (int i = 0; i < row; i++) {
            res[i] = calculateSSI(tt500[i], tt850[i], th850[i], isTD);
        }
        return res;
    }

    /**
     * 计算沙氏指数(IDL)
     *
     * @param tt500
     * @param tt850
     * @param th850
     * @param isTD
     * @return
     */
    public static double[][] calculateSSI(double[][] tt500, double[][] tt850, double[][] th850, boolean isTD) {
        List<Integer> dimList = new ArrayList<Integer>(4);
        dimList.add(tt500.length);
        dimList.add(tt850.length);
        dimList.add(th850.length);
        if (!isSameDimension(dimList)) {
            logger.info("In WeatherElemCalc中计算沙氏指数时传入的参数维数不同，不能计算");
            return null;
        }
        dimList = new ArrayList<Integer>(4);
        dimList.add(tt500[0].length);
        dimList.add(tt850[0].length);
        dimList.add(th850[0].length);
        if (!isSameDimension(dimList)) {
            logger.info("In WeatherElemCalc中计算沙氏指数时传入的参数维数不同，不能计算");
            return null;
        }
        int row = tt500.length;
        int col = tt500[0].length;
        double[][] res = new double[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                res[i][j] = calculateSSI(tt500[i][j], tt850[i][j], th850[i][j], isTD);
            }
        }
        return res;
    }

    /**
     * 计算沙氏指数 单站或单点
     *
     * @param tt500
     * @param tt850
     * @param pp850
     * @param th850
     * @param isTH  是否为TH还是TD
     * @return
     */
    public static double si(double tt500, double tt850, double th850, boolean isTH) {
        double td850 = th850;
        if (isTH) {
            td850 = tt850 - th850;
        }
        //SI=T500-T'
        //说明：T500为等压面上的环境温度；T'气块温度(气块自850hPa高度上绝热上升到500hPa高度所具有的温度)。
        return tt500 - parcelT(tt500, tt850, td850);
    }


    /**
     * 饱和水汽压(Magnus计算饱和水汽压经验公式)
     *
     * @param tt(td代替tt时为实际水汽压)
     * @return
     */
    public static double saturationVP(double tt) {
        if (tt == NULLVAL) {
            return NULLVAL;
        }
        double a = ConstIceA;//冰面
        double b = ConstIceB;
        if (tt > 0.0075) {
            a = ConstWaterA;//水面
            b = ConstWaterB;
        }
        //return (double) (E0*Math.exp(a*tt/(b+tt)));//降低了精度
        return (double) (E0 * (Math.pow(10, a * tt / (b + tt))));//E0=6.11
    }

    private static double getTFromVP(double vp) {
        if (vp == NULLVAL) {
            return NULLVAL;
        }
        double a = ConstIceA;
        double b = ConstIceB;
        if (vp < E0) {
            a = ConstWaterA;
            b = ConstWaterB;
        }
        double tmp = Math.log10(vp / E0);
        return (double) (b / (a / tmp - 1));
    }

    /**
     * 等压面上的混合比
     *
     * @param tt 温度 ，摄氏度
     * @param p  等压面气压值（hPa）毫巴
     * @return
     */
    public static double isobaricSurfaceMixingRatioFromTTPP(double tt, double td, double pp) {
        if (tt == NULLVAL || pp == NULLVAL) {
            return NULLVAL;
        }
//		double vp = vapourPressure(tt, td);
        double vp = saturationVP(td);
        return CONSTANTVal * vp / (pp - vp);
    }

    /**
     * 地面比湿
     *
     * @param ps 测站地面气压
     * @param td 露点温度
     * @param tt 温度
     * @return
     */
    public static double surfaceSpecificHumidity(double ps, double td, double tt) {
//		double vp  = vapourPressure(tt, td);//实际水汽压
        double vp = saturationVP(td);//实际水汽压
        return CONSTANTVal * vp / (ps - 0.378f * vp);
    }

    /**
     * 等压面上的混合比
     *
     * @param vp 水汽压
     * @param p  气压
     * @return
     */
    public static double isobaricSurfaceMixingRatioFromVPPP(double vp, double p) {
        if (vp == NULLVAL || p == NULLVAL) {
            return NULLVAL;
        }
        return CONSTANTVal * vp / (p - vp);
    }

    /**
     * 饱和比湿qs
     *
     * @param tt
     * @param ps 测站地面气压
     * @return
     */
    public static double saturationSpecificHumidity(double tt, double ps) {
        double vp = saturationVP(tt);//饱和水汽压
        return CONSTANTVal * vp / (ps - 0.378f * vp);//需不需要略去0.378*e
    }

    /**
     * 起始抬升高度处湿静力温度
     *
     * @param tt
     * @param hh
     * @param ps 地面气压
     * @param td
     * @return
     */
    public static double moistStaticT(double tt, double hh, double ps, double td) {
        double qs = surfaceSpecificHumidity(ps, td, tt);
        return tt + ConstG / Cpd * hh + Kd * qs;
    }

    /**
     * 高度H处饱和湿静力温度
     *
     * @param tt
     * @param hh
     * @param ps
     * @return
     */
    public static double saturationMoistStaticT(double tt, double hh, double ps) {
        double qs = saturationSpecificHumidity(tt, ps);//饱和比湿
        return tt + ConstG / Cpd * hh + Kd * qs;
    }

    /**
     * 条件性稳定度指数
     *
     * @param tt1
     * @param hh1
     * @param td1
     * @param tt2
     * @param hh2
     * @param td2
     * @param ps  测站地面气压
     * @return
     */
    public static double conditionalStabilityExponent(double tt1, double hh1, double td1, double tt2, double hh2, double td2, double ps) {
        double moistST1 = saturationMoistStaticT(tt1, hh1, ps);//高度H处饱和湿静力温度
        double moistST2 = moistStaticT(tt2, hh2, ps, td2);//起始抬升高度处湿静力温度
        return moistST1 - moistST2;
    }

    /**
     * 假相当位温pseudo-equivalent potential temperature 单点或单站
     *
     * @param tt   等压面上的温度 摄氏度
     * @param pp   等压面上的气压 毫巴
     * @param th   露点温度 摄氏度
     * @param isTH 第三位是否为TH还是TD
     * @return
     */
    public static double tb(double tt, double pp, double th, boolean isTH) {
        double td = th;
        if (tt == NULLVAL || pp == NULLVAL || td == NULLVAL) {
            return NULLVAL;
        }
        if (isTH) {
            td = tt - th;
        }

        double absoluteT = T0 + tt;//绝对温度
//		double vapourP = vapourPressure(tt,td);
        double vapourP = saturationVP(td);//实际水汽压
//		double vapourP = saturationVP(tt);//实际水汽压
        double mixingRatio = isobaricSurfaceMixingRatioFromVPPP(vapourP, pp);//等压面上的混合比
        double tc = tk(td, tt);//凝结高度处的温度（绝对温度）
        double lc = waterLatentHeatKelvin(tc);//水的蒸发潜热
        return (double) (absoluteT * Math.pow(1000 / (pp - vapourP), Kd) * Math.exp(lc * mixingRatio / Cp1 / tc));
    }


    /**
     * 强垂直（水平）风切变风向 单位：° 单点或单站
     *
     * @param wd1    高度1上的风向或uu1
     * @param ws1    高度1上的风速或vv1
     * @param wd2    高度2上的风向或uu2
     * @param ws2    高度2上的风速或vv2
     * @param isWDWS 是否为风速风向
     * @return
     */
    public static double windShearWD(double wd1, double ws1, double wd2, double ws2, boolean isWDWS) {
        double u1 = wd1;
        double v1 = ws1;
        double u2 = wd2;
        double v2 = ws2;
        if (isWDWS) {
            u1 = wind2U(wd1, ws1);
            v1 = wind2V(wd1, ws1);
            u2 = wind2U(wd2, ws2);
            v2 = wind2V(wd2, ws2);
        }
        if (u1 == NULLVAL || v1 == NULLVAL || u2 == NULLVAL || v2 == NULLVAL) {
            return NULLVAL;
        }
        if (u2 == u1) {
            if (v2 > v1) {
                return 180;
            } else if (v2 < v1) {
                return 360;
            } else {
                return 0;//静风
            }
        }

        double tmpAngle = Math.toDegrees(Math.atan((v2 - v1) / (u2 - u1)));
        if (u2 > u1) {
            return (double) (270 - tmpAngle);
        } else {
            return (double) (90 - tmpAngle);
        }
    }

    /**
     * 强垂直（水平）风切变风大小 单位：s-1 量级 1e-3 单站或单点
     *
     * @param wd1    高度z1上的风向
     * @param ws1    高度z1上的风速
     * @param z1     高度1
     * @param wd2    高度z2上的风向
     * @param ws2    高度z2上的风速
     * @param z2     高度2
     * @param isWDWS 是否为风速风向
     * @return 风切变的大小
     */
    public static double windShearWS(double wd1, double ws1, double z1, double wd2, double ws2, double z2, boolean isWDWS) {
        double u1 = wd1;
        double v1 = ws1;
        double u2 = wd2;
        double v2 = ws2;
        if (isWDWS) {
            u1 = wind2U(wd1, ws1);
            v1 = wind2V(wd1, ws1);
            u2 = wind2U(wd2, ws2);
            v2 = wind2V(wd2, ws2);
        }
        if (u1 == NULLVAL || v1 == NULLVAL || z1 == NULLVAL || u2 == NULLVAL || v2 == NULLVAL || z2 == NULLVAL) {
            return NULLVAL;
        }
        return (double) (Math.sqrt((u2 - u1) * (u2 - u1) + (v2 - v1) * (v2 - v1)) / Math.abs(z2 - z1) * 1E3);
    }

    /**
     * 理查逊数（理查森数） 无量纲 单点或单站
     *
     * @param p1     等压面1的气压
     * @param p2     等压面2的气压
     * @param t1     等压面1的温度
     * @param t2     等压面2的温度
     * @param wd1    等压面1的风向
     * @param wd2    等压面2的风向
     * @param ws1    等压面1的风速
     * @param ws2    等压面2的风速
     * @param isWDWS 是否为风速风向
     * @return 理查森数
     */
    public static double ri(double p1, double p2, double t1, double t2, double wd1, double wd2, double ws1, double ws2, boolean isWDWS) {
        double u1 = wd1;
        double v1 = ws1;
        double u2 = wd2;
        double v2 = ws2;
        if (isWDWS) {
            u1 = wind2U(wd1, ws1);
            u2 = wind2U(wd2, ws2);
            v1 = wind2V(wd1, ws1);
            v2 = wind2V(wd2, ws2);
        }

        if (p1 == NULLVAL || p2 == NULLVAL || t1 == NULLVAL || t2 == NULLVAL || u1 == NULLVAL || u2 == NULLVAL || v1 == NULLVAL || v2 == NULLVAL) {
            return NULLVAL;
        }

        double deltaP = p1 - p2;//两等压面间的气压差
        double deltaU = u1 - u2;//两等压面之间的风速U分量差
        double deltaV = v1 - v2;//两等压面之间的风速V分量差
        double meanP = (p1 + p2) / 2;//两等压面的气压平均值
        double meanT = (t1 + T0 + t2 + T0) / 2;//两等压面的温度平均值
        double deltaT = t1 - t2;//两等压面间的温度差
//		//计算时可将温度差改为虚温，结果更严谨
//		double e1 = saturationVP(t1);//饱和水汽压
//		double e2 = saturationVP(t2);//饱和水汽压
//		double q1 = 0.622f*e1/p1;//比湿
//		double q2 = 0.622f*e2/p2;//比湿
//		double tv1 = (1+0.61f*q1)*t1;//虚温
//		double tv2 = (1+0.61f*q2)*t2;//虚温
//		double deltaT = tv1 - tv2;//虚温差
//
        return -1 * Rd * deltaP / meanP * (deltaT - (meanT * Kd) * (deltaP / meanP)) / (deltaU * deltaU + deltaV * deltaV);
    }


    /**
     * 变温、变压、变高、tt-td、tt-th
     *
     * @param val1
     * @param val2
     * @return
     */
    public static double valSubtract(double val1, double val2) {
        if (val1 == NULLVAL || val2 == NULLVAL) {
            return NULLVAL;
        }

        return val1 - val2;
    }

    public static double[] valSubtract(double[] val1, double[] val2) {
        if (val1.length != val2.length) {
            logger.info("In WeatherEelmCalc中计算两个数相减时，维数不同，无法计算");
            return null;
        }
        int size = val1.length;
        double[] val = new double[size];
        for (int i = 0; i < size; i++) {
            if (val1[i] == NULLVAL || val2[i] == NULLVAL) {
                val[i] = NULLVAL;
            } else {
                val[i] = val1[i] - val2[i];
            }
        }
        return val;
    }

    public static double[][] valSubtract(double[][] val1, double[][] val2) {
        if (val1.length != val2.length) {
            logger.info("In WeatherEelmCalc中计算两个数相减时，维数不同，无法计算");
            return null;
        }
        int row = val1.length;
        if (val1[0].length != val2[0].length) {
            logger.info("In WeatherEelmCalc中计算两个数相减时，维数不同，无法计算");
            return null;
        }
        int col = val1[0].length;
        double[][] val = new double[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (val1[i][j] == NULLVAL || val2[i][j] == NULLVAL) {
                    val[i][j] = NULLVAL;
                } else {
                    val[i][j] = val1[i][j] - val2[i][j];
                }
            }
        }
        return val;
    }

    /**
     * 累积 plus
     *
     * @param val1
     * @param val2
     * @return
     */
    public static double valPlus(double val1, double val2) {
        if (val1 == NULLVAL || val2 == NULLVAL) {
            return NULLVAL;
        }

        return val1 + val2;
    }

    public static double[] valPlus(double[] val1, double[] val2) {
        if (val1.length != val2.length) {
            logger.info("In WeatherEelmCalc中计算两个数相减时，维数不同，无法计算");
            return null;
        }
        int size = val1.length;
        double[] val = new double[size];
        for (int i = 0; i < size; i++) {
            if (val1[i] == NULLVAL || val2[i] == NULLVAL) {
                val[i] = NULLVAL;
            } else {
                val[i] = valPlus(val1[i], val2[i]);
            }
        }
        return val;
    }

    public static double[][] valPlus(double[][] val1, double[][] val2) {
        if (val1.length != val2.length) {
            logger.info("In WeatherEelmCalc中计算两个数相减时，维数不同，无法计算");
            return null;
        }
        int row = val1.length;
        if (val1[0].length != val2[0].length) {
            logger.info("In WeatherEelmCalc中计算两个数相减时，维数不同，无法计算");
            return null;
        }
        int col = val1[0].length;
        double[][] val = new double[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (val1[i][j] == NULLVAL || val2[i][j] == NULLVAL) {
                    val[i][j] = NULLVAL;
                } else {
                    val[i][j] = valPlus(val1[i][j], val2[i][j]);
                }
            }
        }
        return val;
    }

    private final static double NULLVAL = IsolineUtil.NULLVAL;

    ////////////////////ZM-CAPE////////////////

    /**
     * @param p  每个气压层气压
     * @param t  温度 摄氏度
     * @param th 露点温度 摄氏度
     * @return
     */

    public static double cape(double[] p, double[] t, double[] th, boolean isTH) {
        double p0 = p[0];
        double t0 = t[0];
        double td0 = th[0];
        double[] td = th;

        if (isTH) {
            td0 = t[0] - th[0];
            for (int i = 0; i < p.length; i++) {
                td[i] = t[i] - th[i];
            }
        }

        //凝结高度出温度气压
        double[] tcpc = getTcPc(td0, t0, p0);
        double tc0 = tcpc[0];
        double pc0 = tcpc[1];

//		double tc0 = tk(td0,t0) - T0;
//		double pc0 = pk(t0,p0,tc0);
//		System.out.println("tc="+tc0 + ", pc="+pc0);

        //假相当位温
        double thse0 = getSeIndex(tc0, pc0);

        double[] te = cal_te(p, t);
        double[] ta = cal_ta(p0, t0, pc0, tc0, thse0);

        return cal_vir_engy(p, t, td, te, ta);
    }

    /**
     * 凝结点处温度、气压
     *
     * @param td 露点温度
     * @param t  温度
     * @param p  气压
     * @return K
     */
    private static double[] getTcPc(double td, double t, double p) {
        double[] res = new double[2];
        double th = t - td;
        if (th < 0) {
            th = 0;
        }
        //温度t
        double tc = (double) (t - th * 0.976f / (0.976f - 8.33f * Dq * Math.pow(ConstWaterB + td, 2) / (T0 + td)));
        //气压p
        double pc = (double) (p * Math.pow((tc + T0) / (t + T0), 3.5f));

        res[0] = tc;
        res[1] = pc;
        return res;
    }

    /**
     * 假相当位温
     *
     * @param t 温度
     * @param p 气压
     * @return
     */
    //TODO 有待改进公式最后部分凝结温度
    private static double getSeIndex(double t, double p) {
        double es = saturationVP(t);
        double ld = (double) (2.501 * Math.pow(10, 6) - 2368 * t);
        double qk = (double) 0.622 * es / (p - es);
        return (double) ((t + T0) * Math.pow((1000.0f / (p - es)), (Rd / Cpd)) * Math.exp((ld * qk) / (Cpd * (t + T0))));
    }//此方法返回公式中后面部分有问题 （t+T0）不为凝结高度上的绝对温度。

    /**
     * 计算状态曲线各整层高度上的温度
     *
     * @param pp0  某格点起始层压强P0(hpa)
     * @param tp0  某格点起始层温度T0(摄氏度)
     * @param pc   某格点凝结点处压强PC(hpa)
     * @param tc   某格点凝结点处温度TC(摄氏度)
     * @param thse 某格点凝结点处假相当位温
     * @return 该格点各整层高度上的温度(摄氏度)
     */
    private static double[] cal_ta(double pp0, double tp0, double pc, double tc, double thse) {
        double[] ta = new double[110];
        for (int i = 0; i < 110; i++) {
            ta[i] = -130;
        }

        //干绝热过程
        int kpc = (int) (pp0 / 10.0f) - (int) (pc / 10.0f);
        double p_l = ((int) (pp0 / 10.0f)) * 10.0f;
        double thd = (double) ((tp0 + T0) * Math.pow((1000.0f / pp0), Rd / Cpd));
//		double[] ta = new double[kpc];
        for (int i = 0; i < kpc; i++) {
            ta[i] = (double) (thd * Math.pow((p_l / 1000.0f), Rd / Cpd) - T0);
            //	cout<<"TA11111="<<TA[I]<<endl;
            p_l -= 10.0f;
        }

        //湿绝热过程
        int in = kpc;
        double t1 = tc;
        double p1 = pc;
        int kzero = 1;
        while (p_l > 100.0) {
            double thse1 = getSeIndex(t1, p_l);
            double thse2 = getSeIndex(t1 - 0.1f, p_l);
            double t2 = t1 - (thse1 - thse) / (thse1 - thse2) * 0.1f;
            if (t2 < 0 && kzero == 1) {
                thse = getSeIndex(t2, p_l);
                kzero = 0;
            }
            in += 1;
            ta[in] = t2;
            t1 = t2;
            p_l -= 10.0f;
        }
        return ta;
    }

    /**
     * 计算层结每隔 10HPA 的温度（对数气压线性内插）
     *
     * @param pp 各个气压层气压
     * @param tp 每个气压层 温度 度
     * @return
     */
    private static double[] cal_te(double[] pp, double[] tp) {
        double[] te = new double[110];
        for (int k = 0; k < 110; k++) {
            te[k] = -60.0f;
        }
        int kmax = pp.length;
        int i_nl = kmax - 1;
        int i = 0;
        int in = 0;

        while (i + 1 <= i_nl) {
            double p_l = (int) ((pp[i] / 10.0f)) * 10.0f;
            double p_t = (int) (pp[i + 1] / 10.0f) * 10.0f + 10.0f;

            while (p_l >= p_t) {//浮点型比较 不准确
                te[in] = (double) (tp[i] + (tp[i] - tp[i + 1]) / Math.log(pp[i] / pp[i + 1]) * Math.log(p_l / pp[i]));
                in += 1;
                p_l -= 10.0f;
                if (p_t > pp[i]) {//浮点型比较 不准确
                    in -= 1;
                }
            }
            i += 1;
        }

        if (pp[i_nl] >= 100 && ((int) (pp[i_nl] / 10.0f)) * 10.0f == pp[i_nl]) {
            te[in] = tp[i_nl];
        }

        return te;
    }

    private static double cal_vir_engy(double[] p, double[] t, double[] td, double[] te, double[] ta) {
//		double[] tde = new double[110]; //露点温度
        double[] pds = new double[110];
        double p_l = 0.0f;
        int size = p.length;
        for (int k = 0; k < size; k++) {
            if (td[k] > 90.0) {
                td[k] = t[k] - 30;
            }
        }

        double[] tde = cal_te(p, td);
        int np = 0;
        p_l = ((int) (p[0] / 10.0f)) * 10.0f;
        while (p_l > 100.0) {
            pds[np] = p_l;
            p_l -= 10.0f;
            np += 1;
        }
        np -= 1;
//		double[] qe = new double[110];
//		double[] qse = new double[110];

        double[] qe = cal_q(tde, pds);
        double[] qse = cal_q(ta, pds);

        double capev = 0.0f;

        p_l = ((int) (p[0] / 10.0f)) * 10;
        double p_t = p_l - 10.0f;
        double con = -1.0f * NULLVAL;
        for (int k = 0; k <= np - 1; k++) {
            if (ta[k] > te[k] && ta[k + 1] > te[k + 1] && qse[k + 1] < con && qe[k + 1] < con && qse[k] < con && qe[k] < con) {
                capev = (double) (capev + Rd * ((1 + 0.61f * qse[k]) * (ta[k] + T0) + (1 + 0.61f * qse[k + 1]) * (ta[k + 1] + T0) - (1 + 0.61 * qe[k]) * (te[k] + T0) - (1 + 0.61 * qe[k + 1]) * (te[k + 1] + T0)) / 2.0 * Math.log(p_l / p_t));
                //CAPEV=CAPEV+R*(TA[ndx]+TA[ndx+1]-TE[ndx]-TE[ndx+1])/2.*log(P_L/P_T);
            }
            p_l = p_t;
            p_t -= 10.0f;
            if (p_t < p[size - 1]) {
                break;
            }
        }

        return capev;
    }

    private static double[] cal_q(double[] td, double[] p) {
        int len = td.length;
        double[] e = new double[len];
        e = cal_e(td);
        double[] q = new double[len];
        for (int k = 0; k < len; k++) {
            if (e[k] < 100.0f) {
                q[k] = CONSTANTVal * e[k] / (p[k] - 0.378f * e[k]);
            } else {
                q[k] = -1.0f * NULLVAL;
            }
        }

        return q;
    }

    //各气压层水汽压
    private static double[] cal_e(double[] t) {
        int len = t.length;
        double[] e = new double[len];
        for (int k = 0; k < len; k++) {
            if (t[k] > -150 && t[k] < 60.0) {
                e[k] = saturationVP(t[k]);
            } else {
                e[k] = -1.0f * NULLVAL;
            }
        }
        return e;
    }
    //////////////////CAPE////////////////////

///////////////////////计算CAPE////////////////////////////////////

    /**
     * @param rh 相对湿度
     * @param tk 温度 (K)
     * @param p  气压
     * @return
     */
    public static double cape_tmp(double[] rh, double[] tk, double[] p) {
        int len = p.length;
        double cape = 0;

        //计算thse && td
        double[] citase = new double[len];
        double[] tdk = new double[len];
        double[] q = new double[len];

        double[] t = new double[len];
        double[] td = new double[len];
        double[] fp = new double[len];

        for (int i = 0; i < len; i++) {
            double[] tmp = tem_se(rh[i], tk[i], p[i]);
            q[i] = tmp[0];
            tdk[i] = tmp[1];
            citase[i] = tmp[2];

            t[i] = tk[i] - T0;
            td[i] = tdk[i] - T0;
            fp[i] = rh[i];
        }
        //////
//		do k=1,z
//	     t(k)=tk(34,4,k)-273.16
//	     td(k)=tdk(34,4,k)-273.16
//	     fp(k)=f(34,4,k)
//	     write(*,*)p(k),t(k),td(k),fp(k)  !都是34，4这个点的
//	     enddo
        //计算LCL处的气压，温度，假相当位温 (也就是抬升凝结高度)
        double lp = p[0];
        double m = t[0];
        double n = td[0];
        double tc = (double) (m - 0.976f * (m - n) / (0.976f - 0.000833f * (Math.pow(237.3f + n, 2) / (T0 + n))));// !LCL处的温度
        double pc = (double) (lp * (Math.pow((T0 + tc) / (273.16 + m), 3.50f)));
        double thse = ths(tc, pc);

        //计算自由对流高度LFC
        double py = pc;//!pc是抬升凝结高度处的气压
        double ty = 0;
        double lfcp = 0;
        double lfct = 0;
        while (py >= 50) {
            for (int i = 0; i < len - 1; i++) {
                if (py > p[i + 1] && py < p[i]) {
                    ty = (t[i] - t[i + 1]) * (py - p[i]) / (p[i] - p[i + 1]) + t[i];
                }
            }
            double ts = ttt(py, thse, -50, tc);
            if (Math.abs(ts - ty) < 0.01f) {
                break;
            }
            py = py - 0.5f;
        }

        if (py < p[len - 1]) {
            return 0;
        } else {
            lfcp = py;
            lfct = ty;
        }

        //计算平衡高度EL
        double elp = 0;
        double elt = 0;
        py = lfcp - 5;
        ty = 0;
        while (py >= 50) {
            for (int i = 0; i < len - 1; i++) {
                ty = (t[i] - t[i + 1]) * (py - p[i + 1]) / (p[i] - p[i + 1]) + t[i + 1];
            }
            double ts = ttt(py, thse, -50, lfct);
            if (Math.abs(ts - ty) < 0.01f) {
                break;
            }
            py = py - 0.5f;
        }

        if (py < p[len - 1]) {
            elp = p[len - 1];
            elt = t[len - 1];
        } else {
            elp = py;
            elt = ty;
        }

        //计算CAPE
        double total = 0.0f;
        double rd = 287;
        double pp = lfcp;
        double tt = 0;
        while (pp >= elp) {
            for (int i = 0; i < len - 1; i++) {
                if ((pp - p[i]) * (pp - p[i + 1]) <= 0) {
                    tt = (t[i] - t[i + 1]) * (pp - p[i + 1]) / (p[i] - p[i + 1]) + t[i + 1];
                }
            }
            double tpp = ttp(pp, thse, tt);
            double tppv = (double) ((tpp + T0) * (1 + 0.378f * (E0 * Math.pow(10, ConstWaterA * tpp / (ConstWaterB + tpp))) / pp));
            double[] tcetde = tenvironment(pp, len, t, td, p);
            double tce = tcetde[0];
            double tde = tcetde[1];
            double tenv = (double) ((tce + T0) * (1 + 0.378f * (E0 * Math.pow(10, ConstWaterA * tde / (ConstWaterB + tde))) / pp));
            cape = (rd * (tppv - tenv) / pp) * 0.5f;
            total = total + cape;
            pp = pp - 0.5f;
        }
        return cape;
    }

    //计算假相当位温和露点的子程序
    private static double[] tem_se(double f, double t, double p) {
        double[] res = new double[3];
        double d = 4.9283f;
        double c = 6764.9f;
        double et = (double) (E0 * (Math.pow(T0 / t, d) * Math.exp(c / T0 - c / t)));
        double qs = 0.622f * et / (p - 0.378f * et);
        double q = qs * f / 100.0f;
        double td = tem_td(q, t, p);
        c = (double) (0.28586f * Math.log((1000.0f / p)));
        d = q / (338.52f - 0.24f * t + 1.24f * td);
        double se = (double) (t * Math.exp((c + 2500 * d)));
        res[0] = q;
        res[1] = td;
        res[2] = se;
        return res;
    }

    private static double tem_td(double q, double t, double p) {
        double td = t;
        double qs = q + 1;
        double a = 6764.9f;
        double b = 4.9283f;
        while (q <= qs) {
            td = td - 0.02f;
            if (td < 200) {
                return td;
            }
            double et = (double) (E0 * (Math.pow(T0 / td, b)) * Math.exp(a / T0 - a / td));
            qs = 0.622f * et / (p - 0.378f * et);
        }
        return td;
    }

    //由气压求湿绝热上升的气块温度
    private static double ttt(double pp, double thse, double elt, double lfct) {
        double tt = elt;
        while (tt <= lfct) {
            double ths = ths(tt, pp);
            ths = ths - T0;
            if (Math.abs(ths - thse) < 0.1f) {
                break;
            }
            tt = tt + 0.5f;
        }
        return tt;
    }

    //由某处气压求湿绝热上升的气块温度
    private static double ttp(double pp, double thse, double tt) {
        double min = tt - 50;
        double max = tt + 50;
        double tpp = min;
        while (tpp <= max) {
            double ths = ths(tpp, pp);
            ths = ths - T0;
            if (Math.abs(ths - thse) < 0.1f) {
                break;
            }
            tpp = tpp + 0.5f;
        }
        return tpp;
    }

    //由气压求环境温度
    private static double[] tenvironment(double pp, int iz, double[] tc, double[] td, double[] p) {
        double[] res = new double[2];
        for (int i = 0; i < iz - 1; i++) {
            if ((pp - p[i]) * (pp - p[i + 1]) <= 0) {
                res[0] = ((tc[i] - tc[i + 1]) * (pp - p[i + 1])) / (p[i] - p[i + 1]) + tc[i + 1];
                res[1] = ((td[i] - td[i + 1]) * (pp - p[i + 1])) / (p[i] - p[i + 1]) + td[i + 1];
            }
        }
        return res;
    }

    private static double ths(double tt, double pp) {
        double e = (double) (E0 * Math.pow(10, ConstWaterA * tt / (ConstWaterB + tt)));
        double q = 0.622f * e / (pp - 0.378f * e);
        return (double) ((tt + T0) * Math.exp(0.28586f * Math.log(1000.0f / pp) + 2500 * q / (338.52f - 0.24f * (T0 + tt) + 1.24f * tt)));
    }
}
