package com.devcru.staylocatedapi.dao;

import java.util.List;
import java.util.UUID;

import com.devcru.staylocatedapi.objects.Contact;
import com.devcru.staylocatedapi.objects.Profile;
import com.devcru.staylocatedapi.objects.User;

public interface UserDao {

	/*
	 * User-related
	 */
	// GET (admin-only)
	//boolean getUsers();
	
	// POST
	boolean createUser(User user);

	// PUT
	boolean updateUser(User user);

	// DELETE
	boolean deleteUser(User user);
	
	/*
	 * Contacts-related
	 */
	// GET (self-only, for now)
	List<Contact> viewContacts(User self);
	
	// POST
	boolean createContactRequest(User user1, User user2);
	
	// PUT
	boolean updateContactRequest(int status, User sender, User recipient);
	boolean createContact(User requester, User accepter); //also used when updating requests
	
	// DELETE
	boolean deleteContact(User sender, User recipient);
	
	// Support HTTP methods (may ultimately not be necessary)
	// POST
	boolean verifyUserCreds(User user);
	
	/*
	 * Profile-related
	 */
	// POST (created when a user is created)
	boolean createProfile(Profile profile);
	
	// GET
	boolean getProfile(User user);
	
	// PUT
	boolean updateProfile(Profile profile);
	
	/*
	 * SUPPORT HTTP-agnostic methods
	 */
	boolean checkUserExists(String username);
	String getUuid(String username);
	String getUsername(UUID userUuid);
	int getContactRequestStatus(UUID userUuid1, UUID userUuid2);
	boolean checkContactRequestExists(UUID uuid1, UUID uuid2);
	boolean checkContactExists(UUID uuid1, UUID uuid2);
	User getUser(User user);

}
