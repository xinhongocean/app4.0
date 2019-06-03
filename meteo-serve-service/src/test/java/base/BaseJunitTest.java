package base;
/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 邓帅
 * @version 创建时间：2016/3/3 0003.
 */
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring/spring-config.xml","/spring/spring-config-redis.xml"})
public class BaseJunitTest {


 /*  static {
        try {
            Log4jConfigurer.initLogging("classpath:conf/log4j.properties");
        }catch (FileNotFoundException e){
            e.printStackTrace();
            System.out.println("cannot Initialize log4j");
        }
    }*/
}
