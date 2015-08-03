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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.devcru.staylocatedapi.dao.UserDao;
import com.devcru.staylocatedapi.objects.JsonResponse;
import com.devcru.staylocatedapi.objects.User;

// XXX: Follow the REST API naming conventions (ex: app/noun/01234)
// http://www.restapitutorial.com/lessons/restfulresourcenaming.html

@Controller
//@JsonIgnoreProperties(ignoreUnknown = true) // Doesn't seem to be necessary, but leaving in for now
@RequestMapping(value = "/users/*")
public class UserController {
	
	UserDao ud;
	@Autowired
	public void setUserDao(UserDao ud) { this.ud = ud; }
	
	private static final Logger loggerForJay = Logger.getLogger(UserController.class);

	protected JdbcTemplate template;
	@Autowired
	@Qualifier("dataSource")
	public void setDataSource(DataSource ds) { this.template = new JdbcTemplate(ds); }
	
	@RequestMapping(value="/", method=RequestMethod.GET)
	public @ResponseBody
	JsonResponse getListOfUsers() {
		// get list of users... what should we return?  Entire user objects?
		return new JsonResponse("OK", "");
	}
	
	@RequestMapping(value="/", method=RequestMethod.POST)
	// FIXME: headers="content-type=application/json" or produces="application/json"
	public @ResponseBody
	JsonResponse registerUser(@RequestBody User user) {
		String message = "";
		
		if(ud.insertUser(user)) {
			message = "Account created!";
		} else {
			message = "Account could not be created!";
		}
		
		System.out.println("message: " + message);
		
		return new JsonResponse("OK", message);
	}
	
	@RequestMapping(value="/{uuid}", method=RequestMethod.GET)
	public @ResponseBody
	JsonResponse getAccountDetails(@PathVariable ("uuid") String userUuid) {
		// get user account details
		return new JsonResponse("OK", "");
	}
	
	@RequestMapping(value="/{uuid}", method=RequestMethod.DELETE)
	public @ResponseBody
	JsonResponse deleteUser(@PathVariable ("uuid") String userUuid, @RequestBody User user) {
		// remove user account
		return new JsonResponse("OK", "");
	}
	
	@RequestMapping(value="/{uuid}/profiles", method=RequestMethod.GET)
	public @ResponseBody
	JsonResponse getUserProfile(@PathVariable ("uuid") String userUuid) {
		// get user profile
		return new JsonResponse("OK", "");
	}
	
	@RequestMapping(value="/{uuid}/profiles", method=RequestMethod.POST)
	public @ResponseBody
	JsonResponse replaceUserProfile(@PathVariable ("uuid") String userUuid, @RequestBody User user) {
		// update user profile
		return new JsonResponse("OK", "");
	}
	
	@RequestMapping(value="/{uuid}/contacts", method=RequestMethod.GET)
	public @ResponseBody
	JsonResponse getUserContacts(@PathVariable ("uuid") String userUuid) {
		// get list of user contactss
		return new JsonResponse("OK", "");
	}
	
	@RequestMapping(value="/{uuid}/contacts", method=RequestMethod.POST)
	public @ResponseBody
	JsonResponse addContact(@PathVariable ("uuid") String userUuid, @RequestBody User user) {
		// add user contact
		return new JsonResponse("OK", "");
	}
	
	// TODO: Figure out how to add the delete Contacts method (due to two uuid variables)
	// Do we daisy chain them as independently managed resources?
	// i.e. /user/{uuid} + /contacts/{uuid}
	// Does this mean profile and contacts will each have to be their own base URL?
	
	// ???: Is this method necessary with exclusive OAuth2?
	@RequestMapping(value = "/credentials", method=RequestMethod.POST)
	public @ResponseBody
	JsonResponse verifyCredentials(@RequestBody User user) {
		
		String message = "";
		
		if(ud.verifyUserCreds(user)) {
			message = "Login successful!";
		} else {
			message = "Login failed!";
		}
		
		System.out.println("message: " + message);
		
		return new JsonResponse("OK", message);
	}
	
	// Test if URL context is set up properly
	@RequestMapping(value = "/examples")
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
