package com.xinhong.mids3d.util.math;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/25.
 */
public class WindUtil {



    public  Wind uvCalWsAndWd(double u,double v){
        float wd = (float)(270.0-Math.atan2(v,u)*180.0/Math.PI);
        long ws = Math.round(Math.sqrt(u*u + v*v));
        Wind wind = new Wind(wd,(float)ws);
       return wind;
    }

    public class Wind {
        public float getWd() {
            return wd;
        }

        public float getWs() {
            return ws;
        }

        private  float wd;
        private  float ws;
        public Wind(float wd,float ws){
            this.wd =wd;
            this.ws = ws;
        }
    }
}
