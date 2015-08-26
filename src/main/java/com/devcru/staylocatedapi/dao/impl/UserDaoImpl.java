package com.devcru.staylocatedapi.dao.impl;

/*
 * XXX: Theory question(s):
 * Q1) We are assigning single values back to List<String> types on some queries while using String types
 * 		on others -- which is better?
 * A1) List<String> can absorb multiple values, while String can only do so for a single value.
 * 
 * Q2) Should we look into finding a way to return message strings to place into the JsonResponse object?
 * A2) For now, we can put this aside... the boolean isSuccess messages are sufficient enough.  Anything
 * 		beyond this should probably be a Log4J thing... so find out how to make it work in Heroku!
 * 
 * Q3) Should we create our own custom ResultSetExtractor for the number of times we use it?
 * A3) Yes, since ResultSetExtractor is necessary when using the template's query method which is allowed
 * 		to return no results, as opposed to the queryFor* method which MUST return at least one result.
 * 
 * Q4) We are enforcing some atomicity logic here (i.e. ensure there are no request relationship duplicates).
 * 		Should we offload this logic to the Controller side?  Does it classify as business logic?
 * A4) My intuition tells me that ensuring atomicity is a data layer concern and, as such, should remain
 * 		in the DAO.
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.devcru.staylocatedapi.dao.UserDao;
import com.devcru.staylocatedapi.objects.Contact;
import com.devcru.staylocatedapi.objects.User;

public class UserDaoImpl implements UserDao {
	
	protected JdbcTemplate template;
	@Autowired
	@Qualifier("dataSource")
	public void setDataSource(DataSource ds) { this.template = new JdbcTemplate(ds); }
	
	// To be used for all query() calls since they allow for possible null returns
	// whereas queryForWhatever() does not
	ResultSetExtractor<String> rse = new ResultSetExtractor<String>() {
		@Override
		public String extractData(ResultSet rs) throws SQLException,
				DataAccessException {
			return (rs.next() ? rs.getString(1) : null);
		}
	};

	@Override
	public boolean insertUser(User user) {
		
		boolean isSuccess = false;
		
		String username = user.getUsername();
		String password = user.getPassword();
		
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(password);
		
		String message = "", sql = null;
		
		sql = "INSERT INTO users (username, password) VALUES (?, ?)";
		
		if(checkUserExists(username)) {
			message = "Username exists! Doing nothing!";
		} else {
			message = "Username not found! Creating account!";
			try {
				template.update(sql, new Object[]{username, encodedPassword});
				isSuccess = true;
			} catch (DataAccessException e) {
				e.printStackTrace();
				isSuccess = false;
			}
		}
		
		System.out.println("message: " + message);
		
		return isSuccess;
	}
	
	@Override
	public boolean verifyUserCreds(User user) {
		
		String username = user.getUsername();
		String password = user.getPassword();
		
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String sql = "SELECT password FROM users WHERE username = ?";
		String encodedPassword = "";
		
		try {
			// Retrieve encodedPassword from storage
			encodedPassword = template.query(sql, new Object[] {username}, rse);
		} catch (DataAccessException e) {
			e.printStackTrace();
			return false;
		}

		// Check for null value returned from the SQL query execution
		if (encodedPassword.isEmpty()) {
			System.out.println("BAD: encodedPassword was empty! No results found for username: " + username);
			return false;
		} else {
			System.out.println("GOOD: encodedPassword contains 1 element! Processing");
			
			// This takes the raw pass and encodes it and then checks if it matches the encoded pass in storage
			boolean passwordMatches = passwordEncoder.matches(password, encodedPassword);
			if (passwordMatches) { return true; }
			else { return false; }
		}
	}
	
	@Override
	public boolean updateUser(User user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteUser(User user) {
		// TODO: Delete EVERYTHING to ensure data integrity.  Use a cascading SQL statement.
		/*
		 * Checklist -- delete from:
		 * 1) users
		 * 2) contact_requests
		 * 3) contacts
		 */
		
		return false;
	}

	@Override
	public List<Contact> viewContacts(User self) {
		
		UUID uuid = self.getUuid();
		
		String sql = "SELECT * FROM contacts WHERE requester_id = ?";
		
		List<Map<String, Object>> rows = null;
		
		try {
			rows = template.queryForList(sql, new Object[]{uuid});
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		
		// Debug
		for(int i = 0; i < rows.size(); i++) {
			System.out.println("rows.get(i): " + rows.get(i));
		}
		
		List<Contact> contacts = new ArrayList<Contact>();
		
		for (Map<String, Object> row : rows) {
			Contact contact = new Contact();
			contact.setRequester_id((UUID)row.get("requester_id"));
			contact.setAccepter_id((UUID)row.get("accepter_id"));
			contact.setTime_added((Timestamp)row.get("time_added"));
			
			contacts.add(contact);
		}
		
		// Debug
		for(int i = 0; i < contacts.size(); i++) {
			System.out.println("contacts.get(i): " + contacts.get(i));
		}
		
		return contacts;
	}

	@Override
	public boolean createContactRequest(User user1, User user2) {
		
		String username1 = user1.getUsername();
		String username2 = user2.getUsername();
		
		UUID senderUuid = user1.getUuid();
		UUID recipientUuid = user2.getUuid();
		
		System.out.println("DEBUG: senderUuid.toString(): " + senderUuid.toString());
		System.out.println("DEBUG: recipientUuid.toString(): " + recipientUuid.toString());
		
		boolean isSuccess = false;
		String sql = "INSERT INTO contact_requests (sender_id, recipient_id, status)"
				+ "VALUES(?, ?, ?)";
		
		// Check if request (and its inverse) exists, if not, go ahead and create it
		if(!checkContactRequestExists(senderUuid, recipientUuid)) {
		
			// Check if the users exist, if even one doesn't, do nothing
			if(checkUserExists(username1) && checkUserExists(username2)) {
				try {
					// status codes: 0 (pending), 1 (rejected), 2 (accepted)
					template.update(sql, new Object[]{senderUuid, recipientUuid, 0});
					isSuccess = true;
				} catch (DataAccessException e) {
					e.printStackTrace();
					isSuccess = false;
				}
			} else if (!checkUserExists(username1) && !checkUserExists(username2)){
				isSuccess = false;
				System.out.println("username1 AND username2 both not found1");
			} else if (!checkUserExists(username1)){
				isSuccess = false;
				System.out.println("username1 not found!");
			} else if (!checkUserExists(username2)) {
				isSuccess = false;
				System.out.println("username2 not found!");
			}
		
		} else {
			System.out.println("The request already exists, doing nothing...");
		}
		
		return isSuccess;
	}

	@Override
	public boolean updateContactRequest(int status, User sender, User recipient) {
		
		System.out.println("DaoImpl: Updating contactRequest...");
		
		boolean isSuccess = false;
		
		UUID senderUuid = sender.getUuid();
		UUID recipientUuid = recipient.getUuid();
		
		System.out.println("DaoImpl: status: " + status);
		System.out.println("DaoImpl: senderUuid: " + senderUuid);
		System.out.println("DaoImpl: recipientUuid: " + recipientUuid);
		
		String sql = "UPDATE contact_requests SET status = ? WHERE sender_id = ? AND recipient_id = ?";
		
		if(sender != null && recipient != null) {
			try {
				template.update(sql, new Object[]{status, senderUuid, recipientUuid});
				isSuccess = true;
			} catch (DataAccessException e) {
				e.printStackTrace();
				isSuccess = false;
			}
		} else {
			System.out.println("DaoImpl: sender or recipient are NULL, doing nothing");
		}
		
		return isSuccess;
	}
	
	@Override
	public boolean createContact(User requester, User accepter) {
		
		boolean isSuccess = false;
		
		String requesterUsername = requester.getUsername();
		String accepterUsername = accepter.getUsername();
		
		UUID requesterUuid = requester.getUuid();
		UUID accepterUuid = accepter.getUuid();
		
		String sql = "INSERT INTO contacts (requester_id, accepter_id) VALUES(?, ?)";
		
		// Check if contact relationship exists, if not, go ahead and create it
		if(!checkContactExists(requesterUuid, accepterUuid)) {
		
			// Check if the users exist, if even one doesn't, do nothing
			if(checkUserExists(requesterUsername) && checkUserExists(accepterUsername)) {
				try {
					template.update(sql, new Object[]{requesterUuid, accepterUuid});
					isSuccess = true;
				} catch (DataAccessException e) {
					e.printStackTrace();
					isSuccess = false;
				}
			} else if (!checkUserExists(requesterUsername) && !checkUserExists(accepterUsername)){
				isSuccess = false;
				System.out.println("requesterUsername AND accepterUsername both not found1");
			} else if (!checkUserExists(requesterUsername)){
				isSuccess = false;
				System.out.println("requesterUsername not found!");
			} else if (!checkUserExists(accepterUsername)) {
				isSuccess = false;
				System.out.println("accepterUsername not found!");
			}
		
		} else {
			System.out.println("The contact relationship already exists, doing nothing...");
		}
		
		return isSuccess;
	}

	@Override
	public boolean deleteContact(User requester, User accepter) {
		
		boolean isSuccess = false;
		
		UUID requesterUuid = requester.getUuid();
		UUID accepterUuid = accepter.getUuid();
		
		String sql = "DELETE FROM contacts WHERE requester_id = ? AND accepter_id = ?";
		
		// First check if request exists (should always if contact exists) and delete it if so
		// if request deleted, then delete contact relationship as well
		// else do nothing if no request exists
		if(deleteContactRequest(requester, accepter)) {
			try {
				template.update(sql, new Object[]{requesterUuid, accepterUuid});
				isSuccess = true;
			} catch (DataAccessException e) {
				e.printStackTrace();
				isSuccess = false;
			}
		} else {
			isSuccess = false;
		}
		
		return isSuccess;
	}
	
	// Tied to the deleteContact method for now... potential use from other methods later...
	public boolean deleteContactRequest(User sender, User recipient) {
		
		boolean isSuccess = false;
		
		UUID senderUuid = sender.getUuid();
		UUID recipientUuid = recipient.getUuid();
		
		String sql = "DELETE FROM contact_requests WHERE sender_id = ? AND recipient_id = ?";
		
		try {
			template.update(sql, new Object[]{senderUuid, recipientUuid});
			isSuccess = true;
		} catch (DataAccessException e) {
			e.printStackTrace();
			isSuccess = false;
		}
		
		return isSuccess;
	}
	
	/*
	 * DAO Support query-methods
	 */
	
	@Override
	public boolean checkUserExists(String username) {
		
		String sql = "SELECT username FROM users WHERE username = ?";
		String results = null;
		
		try {
			results = template.query(sql, new Object[]{username}, rse);
		} catch (DataAccessException e) {
			e.printStackTrace();
			return false;
		}
		
		if(results == null || results.isEmpty()) {
			System.out.println("Username not found!");
			return false;
		} else {
			System.out.println("Username already exists!");
			return true;
		}
	}
	
	@Override
	public String getUuid(String username) {
		
		String uuid = "";
		String sql = "SELECT uuid FROM users WHERE username = ?";
		
		try {
			uuid = template.query(sql, new Object[]{username}, rse);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		
		System.out.println("getUuid: " + uuid);
		
		if(uuid == null || uuid.isEmpty()) {
			return null;
		} else {
			return uuid;
		}
	}

	@Override
	public String getUsername(UUID userUuid) {

		String username = "";
		String sql = "SELECT username FROM users WHERE uuid = ?";
		
		System.out.println("DEBUG: userUuid.toString(): " + userUuid.toString());

		try {
			username = template.query(sql, new Object[]{userUuid}, rse);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}

		System.out.println("getUsername: " + username);

		if (username == null || username.isEmpty()) {
			return null;
		} else {
			return username;
		}
	}
	
	@Override
	public int getContactRequestStatus(UUID userUuid1, UUID userUuid2) {
		
		String sql = "SELECT status FROM contact_requests WHERE sender_id = ? AND recipient_id = ?";
		int status = -1; // Default (should never return this)
		
		try {
			status = template.query(sql,
					new Object[] { userUuid1, userUuid2 },
					new ResultSetExtractor<Integer>() {
						@Override
						public Integer extractData(ResultSet rs) // custom because Integer return type
								throws SQLException, DataAccessException {
							return (rs.next() ? rs.getInt(1) : -99); // -99 = Request doesn't exist
						}
					});
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		
		System.out.println("DEBUG: status: " + status);
		
		return status;
	}
	
	@Override
	public boolean checkContactRequestExists(UUID uuid1, UUID uuid2) {
		
		/* 
		 * NOTE: "duplicate relationships" (requests) are found when a request exists AND its
		 * inverse-relationship of the uuid's exist.
		 * 
		 * i.e. a row that has {sender_id:sam, recipient_id:tom}
		 * and another row that has {sender_id:tom, recipient_id:sam}
		 * 
		 * It doesn't make sense to have a request come from one person and the other person able to
		 * send another request back -- recipient should only be allowed to reject or accept
		 */
		
		String sql = "SELECT * FROM contact_requests WHERE sender_id = ? AND recipient_id = ?";
		
		String results1 = null, results2 = null;
		
		try {
			results1 = template.query(sql, new Object[]{uuid1, uuid2}, rse);
			results2 = template.query(sql, new Object[]{uuid2, uuid1}, rse);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		
		System.out.println("results1: " + results1);
		System.out.println("results2: " + results2);
		
		// If two NOT NULLs are caught, then a duplicate relationship was found
		if(null != results1 && null != results2) {
			System.out.println("!!! WARNING !!!: Duplicate records found!  Check table integrity...");
			// technically, however, the request(s) exist, so this is implicitly true
		}
		
		// If two NULLs are found, then no request exists
		if((null == results1 && null == results2) || (results1.isEmpty() && results2.isEmpty())) {
			System.out.println("Query results are empty or null!  No request exists!");
			return false;
		} else {
			// else a standard use-case will always have one NOT NULL and one NULL
			System.out.println("Request found!");
			return true;
		}
		
	}
	
	@Override
	public boolean checkContactExists(UUID uuid1, UUID uuid2) {
		
		/* 
		 * NOTE: "duplicate relationships" (requests) are found when a contact exists AND its
		 * inverse-relationship of the uuid's exist.
		 * 
		 * i.e. a row that has {requester_id:sam, accepter_id:tom}
		 * and another row that has {requester_id:tom, accepter_id:sam}
		 * 
		 * It doesn't make sense to have two contacts entries both saying the same thing
		 */
		
		String sql = "SELECT * FROM contacts WHERE requester_id = ? AND accepter_id = ?";
		
		String results1 = null, results2 = null;
		
		try {
			results1 = template.query(sql, new Object[]{uuid1, uuid2}, rse);
			results2 = template.query(sql, new Object[]{uuid2, uuid1}, rse);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		
		// If two NOT NULLs are caught, then a duplicate relationship was found
		if(null != results1 && null != results2) {
			System.out.println("!!! WARNING !!!: Duplicate records found!  Check table integrity...");
			// technically, however, the contacts(s) exist, so this is implicitly true
		}
		
		// If two NULLs are found, then no contact relationship exists
		if((null == results1 && null == results2) || (results1.isEmpty() && results2.isEmpty())) {
			System.out.println("Query results are empty or null!  No contact relationship exists!");
			return false;
		} else {
			// else a standard use-case will always have one NOT NULL and one NULL
			System.out.println("Contact relationship found!");
			return true;
		}
		
	}
	
	/*public User getUser(User user) {
		// If we can identify any user information, run a query based on that
		// and return the entire user data object
		String email = user.getEmail();
		String username = user.getUsername();
		UUID uuid = user.getUuid();
		
		System.out.println("DaoImpl: uuid: " + uuid);
		System.out.println("DaoImpl: username: " + username);
		System.out.println("DaoImpl: email: " + email);
		
		String sql = "";
		List<Map<String, String>> results = null;
		
		int field = 0;
		
		if(uuid != null) {
			field = 1;
			System.out.println("uuid not null");
			sql = "SELECT  * FROM users WHERE uuid = ?";
		} else if (username != null) {
			field = 2;
			System.out.println("username not null");
			sql = "SELECT * FROM users WHERE username = ?";
		} else if (email != null) {
			field = 3;
			System.out.println("email not null");
			sql = "SELECT * FROM users WHERE email = ?";
		} else {
			System.out.println("No identifying information for the user, returning null...");
			return null;
		}
		// FIXME: Need to check for null returns
		results = template.query(sql, new Object[]{(
					field == 1 ? uuid :
						(field == 2 ? username : email)
					)}, String.class);
		
		for(Map<String, String> i : results) {
			// map results to user fields
		}
		
		return user;
	}*/

}
