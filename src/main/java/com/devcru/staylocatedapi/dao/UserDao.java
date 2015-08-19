package com.devcru.staylocatedapi.dao;

import com.devcru.staylocatedapi.objects.User;

public interface UserDao {

	// User-related
	// GET (admin-only)
	//boolean getUsers();
	
	// POST
	boolean insertUser(User user);

	// PUT
	boolean updateUser(User user);

	// DELETE
	boolean deleteUser(User user);
	
	// Contacts-related
	// GET (self-only, for now)
	boolean viewContacts(User self);
	
	// POST
	boolean createContactRequest(User user1, User user2);
	
	// PUT
	boolean updateContactRequest(User sender, User recipient);
	
	// DELETE
	boolean deleteContact(User sender, User recipient);
	
	// Support methods (may ultimately not be necessary)
	// POST
	boolean verifyUserCreds(User user);

}
