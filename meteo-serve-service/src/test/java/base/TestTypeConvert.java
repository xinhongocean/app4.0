package base;

/**
 * Created by xiaoyu on 16/9/21.
 */
public class TestTypeConvert {


    public static void main(String[] args) {
        Float aa = Float.parseFloat("");
        Float bb = Float.parseFloat(null);
        Float cc = Float.parseFloat(" ");
        Float dd = Float.parseFloat("abc1");
        Float ee = Float.parseFloat("123.6.1");
        Float ff = Float.parseFloat("123.59");

        System.out.println( ee + ff);
    }
}
