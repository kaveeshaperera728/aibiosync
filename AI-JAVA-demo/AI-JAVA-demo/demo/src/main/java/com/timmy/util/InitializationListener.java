package com.timmy.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.timmy.job.SendOrderJob;

public class InitializationListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		ApplicationContext act = WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
		SendOrderJob sendOrderJob = (SendOrderJob) act.getBean("sendOrderJob");
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendOrderJob.startThread();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ApplicationContext act = WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
		// TODO Auto-generated method stub
		SendOrderJob sendOrderJob = (SendOrderJob) act.getBean("sendOrderJob");
		sendOrderJob.stopThread();
	}

}
