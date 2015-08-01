package com.devcru.staylocatedapi.controllers;

import java.net.URL;
import java.net.URLClassLoader;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
//import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.devcru.staylocatedapi.dao.UserDao;
import com.devcru.staylocatedapi.objects.JsonResponse;
import com.devcru.staylocatedapi.objects.User;

@Controller
//@JsonIgnoreProperties(ignoreUnknown = true) // Doesn't seem to be necessary, but leaving in for now
@RequestMapping(value = "/*")
public class UserController {
	
	UserDao ud;
	@Autowired
	public void setUserDao(UserDao ud) { this.ud = ud; }
	
	private static final Logger loggerForJay = Logger.getLogger(UserController.class);

	protected JdbcTemplate template;
	@Autowired
	@Qualifier("dataSource")
	public void setDataSource(DataSource ds) { this.template = new JdbcTemplate(ds); }
	
	@RequestMapping(value="/register", method=RequestMethod.POST)
	// FIXME: headers="content-type=application/json" or produces="application/json"
	public @ResponseBody
	JsonResponse register(@RequestBody User user) {
		
		String message = "";
		
		if(ud.insertUser(user)) {
			message = "Account created!";
		} else {
			message = "Account could not be created!";
		}
		
		System.out.println("message: " + message);
		
		return new JsonResponse("OK", message);
		
	}
	
	@RequestMapping(value = "/login", method=RequestMethod.POST)
	public @ResponseBody
	JsonResponse login(@RequestBody User user) {
		
		String message = "";
		
		if(ud.loginUser(user)) {
			message = "Login successful!";
		} else {
			message = "Login failed!";
		}
		
		System.out.println("message: " + message);
		
		return new JsonResponse("OK", message);
	}
	
	// Test if URL context is set up properly
	@RequestMapping(value = "/testget")
	public @ResponseBody
	String testGet() {
		loggerForJay.info("hello info");
		loggerForJay.debug("hello debug");
		loggerForJay.error("hello error"); // ERROR
		System.out.println("sysout hello world!"); // stdout
		
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		 
        URL[] urls = ((URLClassLoader)cl).getURLs();
 
        for(URL url: urls){
        	System.out.println(url.getFile());
        }		
		
		return "Hello world";
	}
	
}
