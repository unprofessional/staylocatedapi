package com.devcru.staylocatedapi.controllers;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
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
import org.springframework.web.bind.annotation.ResponseStatus;

import com.devcru.staylocatedapi.dao.UserDao;
import com.devcru.staylocatedapi.objects.Contact;
import com.devcru.staylocatedapi.objects.ContactRequest;
import com.devcru.staylocatedapi.objects.JsonResponse;
import com.devcru.staylocatedapi.objects.Profile;
import com.devcru.staylocatedapi.objects.User;
import com.devcru.staylocatedapi.objects.UserProfileWrapper;

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
	
	/*
	 * Users endpoints
	 */
	@RequestMapping(value="/", method=RequestMethod.GET)
	public @ResponseBody
	JsonResponse getListOfUsers() {
		// ADMIN ONLY
		return new JsonResponse("OK", "getListOfUsers()");
	}
	
	@RequestMapping(value="/", method=RequestMethod.POST,
			produces="application/xml", consumes="application/json")
	public @ResponseBody
	JsonResponse registerUser(@RequestBody UserProfileWrapper requestWrapper) {
		
		User user = new User();
		user = requestWrapper.getUser();
		
		Profile profile = new Profile();
		profile = requestWrapper.getProfile();
		
		String key = "OK";
		String message = "";
		
		if(ud.createUser(user)) {
			message = "Account created!";
			
			// Q: Cascade with account creation SQL in DaoImpl?
			// A: For now, what we're doing makes more sense unless we capture the extraneous fields
			// using simple Strings and the like, which would look silly in my opinion
			// i.e. the method signature would be createUser(user, firstName, lastName, etc)
			
			String username = user.getUsername();
			UUID userUuid = ud.getUuid(username);
			profile.setUser_id(userUuid);
			
			System.out.println("user.getUuid(): " + userUuid);
			System.out.println("profile.getUser_id: " + profile.getUser_id());
			
			if(ud.createProfile(profile)) {
				message += " Profile created!";
			} else {
				key = "Error";
				message += " Profile creation failed!";
			}
			
		} else {
			key = "Error";
			message = "Account could not be created!";
		}
		
		return new JsonResponse(key, message);
	}
	
	// !!! (SATURDAY) TODO !!!: Test with application/xml to see what does what
	// This will determine if this will be applied to all other endpoints
	@RequestMapping(value="/{uuid}", method=RequestMethod.GET,
			produces="application/json", consumes="application/json")
	public @ResponseBody
	User getAccountDetails(@PathVariable("uuid") UUID userUuid) {
		
		User user = new User();
		String username = ud.getUsername(userUuid);
		user.setUuid(userUuid);
		user.setUsername(username);
		
		if(isSelf(user)) {
			user = ud.getUser(user);
		} else { // Make room for non-self views?
			System.out.println("User is not self");
			return null;
		}
		
		return user;
	}
	
	@RequestMapping(value="/{uuid}", method=RequestMethod.DELETE)
	public @ResponseBody
	JsonResponse deleteUser(@PathVariable("uuid") UUID userUuid, @RequestBody User user) {
		
		String key = "OK";
		String message = "";
		
		user.setUuid(userUuid);
		
		if(ud.deleteUser(user)) {
			message = "User delete success!";
		} else {
			key = "Error";
			message = "User delete failed!";
		}
		
		return new JsonResponse(key, message);
	}
	
	/*
	 * Profile endpoints
	 */
	@RequestMapping(value="/{uuid}/profile", method=RequestMethod.GET)
	public @ResponseBody
	Profile getUserProfile(@PathVariable("uuid") UUID userUuid) {
		
		// TODO: ifSelf, show all.  !ifSelf, show limited stuff (future feature)
		// For now, just show all
		
		// User user = new User();
		// user.setUuid(userUuid);
		
		// String username = ud.getUsername(userUuid);
		// user.setUsername(username);
		
		//if(isSelf(user)) { // etc } else { // etc }
		
		Profile profile = new Profile();
		
		User user = new User();
		user.setUuid(userUuid);
		
		profile = ud.getProfile(user);
		
		return profile;
	}

	@RequestMapping(value="/{uuid}/profile", method=RequestMethod.PUT)
	public @ResponseBody
	JsonResponse updateUserProfile(@PathVariable("uuid") UUID userUuid, @RequestBody Profile profile) {
		
		// update user profile (only if self)
		String key = "OK";
		String message = "";
		
		User user = new User();
		user.setUuid(userUuid);
		
		String username = ud.getUsername(userUuid);
		user.setUsername(username);
		
		if(isSelf(user)) {
			
			profile.setUser_id(userUuid);
		
			if(ud.updateProfile(profile)){
				message = "Profile update success";
			} else {
				key = "Error";
				message = "Profile update failed";
			}
		
		} else {
			key = "Error";
			message = "User is not self, doing nothing...";
		}
		
		return new JsonResponse(key, message);
	}
	
	/*
	 * Contacts endpoints
	 */
	@RequestMapping(value="/{uuid}/contacts", method=RequestMethod.GET)
	public @ResponseBody
	List<Contact> getUserContacts(@PathVariable("uuid") UUID userUuid) {
		
		// get list of user contacts
		String username = ud.getUsername(userUuid);
		
		User user = new User();
		user.setUsername(username);
		user.setUuid(userUuid);
		
		List<Contact> contacts = null;
		
		if(isSelf(user)) {
		
			contacts = ud.viewContacts(user);
			
			if(null != contacts) {
				for(int i = 0; i < contacts.size(); i++) {
					System.out.println("contacts.get(i): " + contacts.get(i));
				}
			} else {
				System.out.println("contacts is null/empty");
			}
		
		} else {
			System.out.println("User is not self, doing nothing...");
		}
		
		return contacts;
	}
	
	@RequestMapping(value="/{uuid}/contacts", method=RequestMethod.POST)
	public @ResponseBody
	JsonResponse createContactRequest(@PathVariable("uuid") UUID senderUuid, @RequestBody User recipientUser) {
		
		String key = "OK";
		String message = "";
		
		User senderUser = new User();
		String senderUsername = ud.getUsername(senderUuid);
		
		senderUser.setUuid(senderUuid);
		senderUser.setUsername(senderUsername);
		
		String recipientUsername = recipientUser.getUsername();
		UUID recipientUuid = ud.getUuid(recipientUsername);
		recipientUser.setUuid(recipientUuid);
		
		System.out.println("DEBUG: senderUsername: " + senderUsername);
		System.out.println("DEBUG: senderUser.getUsername(): " + senderUser.getUsername());
		
		// User must be self to make a contact request, else this does nothing
		if(isSelf(senderUser)) {
			message = "Accessor is self, creating contact request";
			ud.createContactRequest(senderUser, recipientUser);
		} else {
			key = "Error";
			message = "Accessor is not self, doing nothing";
		}
		
		System.out.println(message);
		
		return new JsonResponse(key, message);
	}
	
	@RequestMapping(value="/{uuid1}/contacts/{uuid2}", method=RequestMethod.GET)
	public @ResponseBody
	JsonResponse getRequestState(@PathVariable("uuid1") UUID userUuid1, @PathVariable("uuid2") UUID userUuid2) {
		// Should return: 0 (pending), 1 (denied), 2 (approved), or -1 (no request)
		String key = null;
		String message = null;
		
		User senderUser = new User();
		String senderUsername = ud.getUsername(userUuid1);
		senderUser.setUsername(senderUsername);
		
		// User must be self AND sender to view own contact request status, else this does nothing
		if(isSelf(senderUser)) {
			key = "ContactRequestStatus";
			message = "" + ud.getContactRequestStatus(userUuid1, userUuid2);
		} else {
			key = "Error";
			message = "Accessor is not self, doing nothing";
		}
		
		return new JsonResponse(key, message);
	}
	
	@RequestMapping(value="/{uuid1}/contacts/{uuid2}", method=RequestMethod.PUT)
	public @ResponseBody
	JsonResponse approveRequest(@PathVariable("uuid1") UUID userUuid1, @PathVariable("uuid2") UUID userUuid2,
			@RequestBody ContactRequest contactRequest) {
		
		int status = contactRequest.getStatus();
		System.out.println("DEBUG: status: " + status);
		
		String key = "OK";
		String message = null;
			
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
			
			System.out.println("isSender: " + isSender);
			System.out.println("isRecipient: " + isRecipient);
			
			if(isSender || isRecipient) {
				
			switch (status) {
			case 1:
				if (isSender) {
					message = "Accessor is originator of request with status 1, canceling request: ";
				} else if (isRecipient) {
					message = "Accessor is recipient of request with status 1, rejecting request: ";
				}
				// Execute update
				if (ud.updateContactRequest(status, senderUser, recipientUser)) {
					message += "Update Success";
				} else {
					key = "Error";
					message += "Update Failure";
				}
				break;
				
			case 2:
				if (isRecipient) {
					message = "Accessor is recipient of request with status 2, approving request: ";
					// Execute update
					if (ud.updateContactRequest(status, senderUser, recipientUser)) {
						message += "Update Success";
					} else {
						key = "Error";
						message += "Update Failure";
					}
					// Execute create
					if (ud.createContact(senderUser, recipientUser)) {
						message += ": Create Success";
					} else {
						key = "Error";
						message += ": Create Failure";
					}
				} else {
					key = "Error";
					message = "Accessor not allowed for this status code";
				}
				break;
				
			default:
				key = "Error";
				message = "Invalid status code";
				break;
			}
		
		} else {
			key = "Error";
			message = "Accessors are neither sender nor recipient";
		}
		
		return new JsonResponse(key, message);
	}
	
	@RequestMapping(value="/{uuid1}/contacts/{uuid2}", method=RequestMethod.DELETE)
	public @ResponseBody
	JsonResponse deleteContact(@PathVariable("uuid1") UUID userUuid1, @PathVariable("uuid2") UUID userUuid2) {
		
		String key = "OK";
		String message = null;
		
		User requesterUser = new User();
		String requesterUsername = ud.getUsername(userUuid1);
		requesterUser.setUuid(userUuid1);
		requesterUser.setUsername(requesterUsername);
		
		User accepterUser = new User();
		String accepterUsername = ud.getUsername(userUuid2);
		accepterUser.setUuid(userUuid2);
		accepterUser.setUsername(accepterUsername);
		
		boolean isRequester = isSelf(requesterUser);
		boolean isAccepter = isSelf(accepterUser);
		
		System.out.println("isSender: " + isRequester);
		System.out.println("isRecipient: " + isAccepter);
		
		// Must be requester or accepter (i.e. involved in the relationship) to cut ties
		if(isRequester || isAccepter) {
			// Delete Contact and Request
			if(ud.deleteContact(requesterUser, accepterUser)) {
				message = "Delete Success";
			} else {
				key = "Error";
				message = "Delete Failure";
			}
		}
		
		return new JsonResponse(key, message);
	}
	
	/*
	 * Misc other endpoints (test and diagnostics)
	 */
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
        
      //ud.getUser(user);
		
		return "Hello world";
	}
	
	/* 
	 * Helper methods (Utils?)
	 */
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
	
	@RequestMapping(value = "/testgetuser", method=RequestMethod.POST)
	public @ResponseBody
	User testGetUser(@RequestBody User user) {
		
		user = ud.getUser(user);
		
		return user;
		
	}
	
}
