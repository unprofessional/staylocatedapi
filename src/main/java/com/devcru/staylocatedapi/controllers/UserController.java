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
		System.out.println("getListOfUsers()");
		// get list of users... what should we return?  Entire user objects?
		return new JsonResponse("OK", "getListOfUsers()");
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
		System.out.println("getAccountDetails()");
		// get user account details
		return new JsonResponse("OK", "getAccountDetails()");
	}
	
	// TODO: Determine if User is necessary in all methods
	@RequestMapping(value="/{uuid}", method=RequestMethod.DELETE)
	public @ResponseBody
	JsonResponse deleteUser(@PathVariable ("uuid") String userUuid, @RequestBody User user) {
		System.out.println("deleteUser()");
		// remove user account
		return new JsonResponse("OK", "deleteUser()");
	}
	
	@RequestMapping(value="/{uuid}/profile", method=RequestMethod.GET)
	public @ResponseBody
	JsonResponse getUserProfile(@PathVariable ("uuid") String userUuid) {
		System.out.println("getUserProfile()");
		// get user profile
		return new JsonResponse("OK", "getUserProfile()");
	}

	@RequestMapping(value="/{uuid}/profile", method=RequestMethod.PUT)
	public @ResponseBody
	JsonResponse updateUserProfile(@PathVariable ("uuid") String userUuid, @RequestBody User user) {
		System.out.println("updateUserProfile()");
		// update user profile (only if self)
		// XXX: Do we change this to a root of profile for self???
		return new JsonResponse("OK", "updateUserProfile()");
	}
	
	@RequestMapping(value="/{uuid}/contacts", method=RequestMethod.GET)
	public @ResponseBody
	JsonResponse getUserContacts(@PathVariable ("uuid") String userUuid) {
		System.out.println("getUserContacts() -- list of contacts");
		// get list of user contactss
		return new JsonResponse("OK", "getUserContacts()");
	}
	
	@RequestMapping(value="/{uuid}/contacts", method=RequestMethod.POST)
	public @ResponseBody
	JsonResponse addRequest(@PathVariable ("uuid") String userUuid, @RequestBody User user) {
		System.out.println("addRequest() -- add contact request");
		// add contact request
		return new JsonResponse("OK", "addRequest()");
	}
	
	@RequestMapping(value="/{uuid}/contacts/{uuid2}", method=RequestMethod.PUT)
	public @ResponseBody
	JsonResponse approveRequest(@PathVariable ("uuid") String userUuid, String userUuid2, @RequestBody User user) {
		System.out.println("approveRequest() -- approve contact request");
		// confirm contact request
		return new JsonResponse("OK", "approveRequest()");
	}
	
	@RequestMapping(value="/{uuid}/contacts/{uuid2}", method=RequestMethod.DELETE)
	public @ResponseBody
	JsonResponse deleteContact(@PathVariable ("uuid") String userUuid, String userUuid2, @RequestBody User user) {
		System.out.println("deleteContact() -- delete request or contact");
		// delete request or contact
		return new JsonResponse("OK", "deleteContact()");
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
