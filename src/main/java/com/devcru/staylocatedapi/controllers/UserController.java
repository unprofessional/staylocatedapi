package com.devcru.staylocatedapi.controllers;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
//import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.devcru.staylocatedapi.dao.UserDao;
import com.devcru.staylocatedapi.objects.ContactRequest;
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
	JsonResponse getAccountDetails(@PathVariable("uuid") String userUuid) {
		System.out.println("getAccountDetails()");
		// get user account details
		return new JsonResponse("OK", "getAccountDetails()");
	}
	
	// TODO: Determine if User is necessary in all methods
	@RequestMapping(value="/{uuid}", method=RequestMethod.DELETE)
	public @ResponseBody
	JsonResponse deleteUser(@PathVariable("uuid") String userUuid, @RequestBody User user) {
		System.out.println("deleteUser()");
		// remove user account
		return new JsonResponse("OK", "deleteUser()");
	}
	
	@RequestMapping(value="/{uuid}/profile", method=RequestMethod.GET)
	public @ResponseBody
	JsonResponse getUserProfile(@PathVariable("uuid") String userUuid) {
		System.out.println("getUserProfile()");
		// get user profile
		return new JsonResponse("OK", "getUserProfile()");
	}

	@RequestMapping(value="/{uuid}/profile", method=RequestMethod.PUT)
	public @ResponseBody
	JsonResponse updateUserProfile(@PathVariable("uuid") String userUuid, @RequestBody User user) {
		System.out.println("updateUserProfile()");
		// update user profile (only if self)
		// XXX: Do we change this to a root of profile for self???
		return new JsonResponse("OK", "updateUserProfile()");
	}
	
	@RequestMapping(value="/{uuid}/contacts", method=RequestMethod.GET)
	public @ResponseBody
	JsonResponse getUserContacts(@PathVariable("uuid") String userUuid) {
		System.out.println("getUserContacts() -- list of contacts");
		// get list of user contacts
		return new JsonResponse("OK", "getUserContacts()");
	}
	
	@RequestMapping(value="/{uuid}/contacts", method=RequestMethod.POST)
	public @ResponseBody
	JsonResponse createContactRequest(@PathVariable("uuid") UUID senderUuid, @RequestBody User recipientUser) {
		// User must be self to make a contact request, else this does nothing
		String message = "";
		
		User senderUser = new User();
		String senderUsername = ud.getUsername(senderUuid);
		
		senderUser.setUuid(senderUuid);
		senderUser.setUsername(senderUsername);
		
		String recipientUsername = recipientUser.getUsername();
		UUID recipientUuid = UUID.fromString(ud.getUuid(recipientUsername));
		recipientUser.setUuid(recipientUuid);
		
		System.out.println("DEBUG: senderUsername: " + senderUsername);
		System.out.println("DEBUG: senderUser.getUsername(): " + senderUser.getUsername());
		
		if(isSelf(senderUser)) {
			message = "Accessor is self, creating contact request";
			ud.createContactRequest(senderUser, recipientUser);
		} else {
			message = "Accessor is not self, doing nothing";
		}
		
		System.out.println(message);
		
		return new JsonResponse("OK", message);
	}
	
	@RequestMapping(value="/{uuid1}/contacts/{uuid2}", method=RequestMethod.GET)
	public @ResponseBody
	JsonResponse getRequestState(@PathVariable("uuid1") UUID userUuid1, @PathVariable("uuid2") UUID userUuid2) {
		// User must be self AND sender to make view own contact request status, else this does nothing
		// Should return: 0 (pending), 1 (denied), 2 (approved), or -1 (no request)
		String key = null;
		String message = null;
		
		User senderUser = new User();
		String senderUsername = ud.getUsername(userUuid1);
		senderUser.setUsername(senderUsername);
		
		if(isSelf(senderUser)) {
			key = "ContactRequestStatus";
			message = "" + ud.getContactRequestStatus(userUuid1, userUuid2);
			// FIXME: I don't like converting to a String, would prefer data-type consistency
		} else {
			key = "Error";
			message = "Accessor is not self, doing nothing";
		}
		
		return new JsonResponse(key, message);
	}
	
	@RequestMapping(value="/{uuid}/contacts/{uuid2}", method=RequestMethod.PUT)
	public @ResponseBody
	JsonResponse approveRequest(@PathVariable("uuid") UUID userUuid1, @PathVariable("uuid2") UUID userUuid2,
			@RequestBody ContactRequest contactRequest) {
		
		int status = contactRequest.getStatus();
		System.out.println("DEBUG: status: " + status);
		
		String key = "OK";
		String message = null;
			
		// TODO: REFACTOR ALL METHODS THAT APPLY!!! (this has officially become a problem)
		// TODO: Should make use of the boolean return values in the UserDaoImpl methods...
		// TODO: i.e. if(true){ message = "it worked"; } else { "it failed"; }
		
		User senderUser = new User();
		String senderUsername = ud.getUsername(userUuid1);
		senderUser.setUuid(userUuid1);
		senderUser.setUsername(senderUsername);
		
		User recipientUser = new User();
		String recipientUsername = ud.getUsername(userUuid2);
		recipientUser.setUuid(userUuid2);
		recipientUser.setUsername(recipientUsername);
		
		boolean isSender = isSelf(senderUser);
		boolean isRecipient = isSelf(recipientUser);
		boolean isAllowed = isSender ? isRecipient : false;
		
		System.out.println("isSender: " + isSender);
		System.out.println("isRecipient: " + isRecipient);
		System.out.println("isAllowed: " + isAllowed);
		
		if(isAllowed) {
		
			if (status == 1) {
				if (isSender) {
					message = "Accessor is originator of request with status 1, canceling request: ";
				} else if (isRecipient) {
					message = "Accessor is recipient of request with status 1, rejecting request: ";
				}
				if(ud.updateContactRequest(status, senderUser, recipientUser)) {
					message += "Success";
				} else {
					key = "Error";
					message += "Failure";
				}					
			} else if (status == 2) {
				if (isSender) {
					message = "Accessor is recipient of request with status 2, approving request: ";
				}
				if(ud.updateContactRequest(status, senderUser, recipientUser)) {
					message += "Success";
				} else {
					key = "Error";
					message += "Failure";
				}
				if(ud.createContact(senderUser, recipientUser)) {
					message += "Success";
				} else {
					key = "Error";
					message += "Failure";
				}
			} else {
				key = "Error";
				message = "Invalid status code";
			}
		
		} else {
			key = "Error";
			message = "Request does not exist";
		}
		
		return new JsonResponse(key, message);
	}
	
	@RequestMapping(value="/{uuid}/contacts/{uuid2}", method=RequestMethod.DELETE)
	public @ResponseBody
	JsonResponse deleteContact(@PathVariable("uuid") String userUuid, String userUuid2, @RequestBody User user) {
		System.out.println("deleteContact() -- delete request or contact");
		// delete request or contact (only if self)
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
	
	// Helper methods (Move to Utils?)
	// Would this be safer to pass in a String username instead?
	public boolean isSelf(User user) {
		
		System.out.println("DEBUG: user.getUsername(): " + user.getUsername());
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		String authName = authentication.getName();
		String username = user.getUsername();
		
		System.out.println("authName: " + authName);
		System.out.println("username: " + username);
		
		if(username.equals(authName)) {
			System.out.println("authName and username match!");
			return true;
		} else {
			System.out.println("authName and username DO NOT match!");
			return false;
		}
	}
	
	@RequestMapping(value = "/testself", method=RequestMethod.POST)
	public @ResponseBody
	JsonResponse testSelf(@RequestBody User user) {
		
		String message = "";
		
		if(isSelf(user)) {
			message = "User is self";
		} else {
			message = "User is NOT self";
		}
		
		return new JsonResponse("OK", message);
		
	}
	
}
