package net.xinhong.meteoserve.common.wavewatch;


import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wingsby on 2018/1/27.
 */
public class WaveWatchDataReader {
    // todo unfinished
    private static final float[] lonslats = new float[]{0f, 170f, -40f, 60f};
    private static final float[] dataLonsLats = new float[]{0f, 359.5f, -80f, 80f};
    public static final float resolution = 0.5f;

    public static void main(String[] args) {
        readGrib("D:\\20180111\\gwes00.glo_30m.t00z.grib2", "Mean_period_of_combined_wind_waves_and_swell", 6);
    }

    private static float[][] readGrib(String ncfile, String elem, int hour) {
        NetcdfFile datafile = null;
        try {
            datafile = NetcdfFile.open(ncfile);
            Map<String, Object> map = new HashMap<>();
            int datadim = 0;
            //确定长宽
            Variable lonvar = datafile.findVariable("lon");
            float[] lons = (float[]) lonvar.read().get1DJavaArray(DataType.FLOAT);
            Variable latvar = datafile.findVariable("lat");
            float[] lats = (float[]) lonvar.read().get1DJavaArray(DataType.FLOAT);
            Variable timevar = datafile.findVariable("time");
            float[] times = (float[]) lonvar.read().get1DJavaArray(DataType.FLOAT);

            int idx = 0;
            for (int i = 0; i < times.length; i++) {
                if (Math.abs(times[i] - hour) < 1e-6) {
                    idx = i;
                    break;
                }
            }
            int[] range = new int[]{Math.round((lonslats[0] - dataLonsLats[0]) / resolution),
                    Math.round((lonslats[1] - dataLonsLats[1]) / resolution),
                    Math.round((lonslats[0] - dataLonsLats[0]) / resolution),
                    Math.round((lonslats[0] - dataLonsLats[0]) / resolution)};
            for (Variable var : datafile.getVariables()) {
                if (var.getFullName().contains(elem) || var.getFullName().contains(EleNames(elem))) {
                    float[][][] data = (float[][][]) var.read().copyToNDJavaArray();
                    float[][] res = new float[range[1] - range[0] + 1][range[3] - range[2] + 1];
                    for(int i=range[0];i<=range[1];i++){
                        for(int j=range[2];j<=range[2];j++){
                            //先最大维，个人认为应该是idx
                            res[i-range[0]][j-range[2]]=data[idx][i][j];
                        }
                    }
                }
            }
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                datafile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String EleNames(String elem) {
        return elem;
    }

}
