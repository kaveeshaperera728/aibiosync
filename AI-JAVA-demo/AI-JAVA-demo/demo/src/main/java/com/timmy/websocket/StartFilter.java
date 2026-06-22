package com.timmy.websocket;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.java_websocket.WebSocketImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.timmy.websocket.*;


public class StartFilter implements Filter {

    public void destroy() {

    }

    public void doFilter(ServletRequest arg0, ServletResponse arg1,
            FilterChain arg2) throws IOException, ServletException {

    }

    public void init(FilterConfig arg0) throws ServletException {
        this.startWebsocketInstantMsg();
    }

    /**
     * 启动即时聊天服务
     */
    public void startWebsocketInstantMsg() {
    	ApplicationContext ac = new FileSystemXmlApplicationContext("classpath:spring-mybatis.xml"); 
    	WSServer ws=(WSServer) ac.getBean("webSocket");
		ws.start();
	//	ws.onMessage( , message);
	//	String  message="{\"cmd\":\"getuserlist\",\"stn\":true}";
    }
}