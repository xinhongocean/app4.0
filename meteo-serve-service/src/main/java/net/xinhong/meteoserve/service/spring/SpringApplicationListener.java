package net.xinhong.meteoserve.service.spring;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Description: 重写spring监听，用于初始化资源<br>
  * @author 作者 <a>刘晓昌</a>
 * @version 创建时间：2016/3/3.
 */
public class SpringApplicationListener extends ContextLoaderListener {
	public void contextInitialized(ServletContextEvent event) {
		super.contextInitialized(event);
		
		ServletContext sc = event.getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(sc);
		//以下可以进行启动应用时处理所需要的数据(如站点信息等）。
		//......
		System.out.println("SpringApplicationListener......");
		//初始化时加载机场基本信息
		Thread loadAirportInfo = new Thread(new Runnable(){
			public void run(){
				loadAirportInfo();
			}
		});
		loadAirportInfo.start();

	}


	private void loadAirportInfo(){

	}
}
