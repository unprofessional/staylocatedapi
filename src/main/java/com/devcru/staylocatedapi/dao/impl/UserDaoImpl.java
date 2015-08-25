package com.devcru.staylocatedapi.dao.impl;

/*
 * XXX: Theory question(s):
 * Q1) We are assigning single values back to List<String> types on some queries while using String types
 * 		on others -- which is better?
 * A1) List<String> can absorb multiple values, while String can only do so for a single value
 * 
 * Q2) Should we look into finding a way to return message strings to place into the JsonResponse object?
 * A2) For now, we can put this aside... the boolean isSuccess messages are sufficient enough.  Anything
 * 		beyond this should probably be a Log4J thing... so find out how to make it work in Heroku!
 * 
 * Q3) Should we create our own custom ResultSetExtractor for the number of times we use it?
 * A3) Yes, since ResultSetExtractor is necessary when using the template's query method which is allowed
 * 		to return no results, as opposed to the queryFor* method which MUST return at least one result
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.devcru.staylocatedapi.dao.UserDao;
import com.devcru.staylocatedapi.objects.User;

public class UserDaoImpl implements UserDao {
	
	protected JdbcTemplate template;
	@Autowired
	@Qualifier("dataSource")
	public void setDataSource(DataSource ds) { this.template = new JdbcTemplate(ds); }
	
	// For all of the queries
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean viewContacts(User self) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createContactRequest(User user1, User user2) {
		
		// FIXME: Check if request exists, if so, do nothing
		
		String username1 = user1.getUsername();
		String username2 = user2.getUsername();
		
		UUID senderUuid = user1.getUuid();
		UUID recipientUuid = user2.getUuid();
		
		System.out.println("DEBUG: senderUuid.toString(): " + senderUuid.toString());
		System.out.println("DEBUG: recipientUuid.toString(): " + recipientUuid.toString());
		
		boolean isSuccess = false;
		String sql = "INSERT INTO contact_requests (sender_id, recipient_id, status)"
				+ "VALUES(?, ?, ?)";
		
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
			isSuccess = false;
		}
		
		return isSuccess;
	}
	
	@Override
	public boolean createContact(User requester, User accepter) {
		
		boolean isSuccess = false;
		
		UUID requesterUuid = requester.getUuid();
		UUID accepterUuid = accepter.getUuid();
		
		String sql = "INSERT INTO contacts (requester_id, accepter_id) VALUES(?, ?)";
		
		try {
			template.update(sql, new Object[]{requesterUuid, accepterUuid});
			isSuccess = true;
		} catch (DataAccessException e) {
			e.printStackTrace();
			isSuccess = false;
		}
		
		return isSuccess;
	}

	@Override
	public boolean deleteContact(User requester, User accepter) {
		
		boolean isSuccess = false;
		
		UUID requesterUuid = requester.getUuid();
		UUID accepterUuid = accepter.getUuid();
		
		String sql = "DELETE FROM contacts WHERE requester_id = ? AND accepter_id = ?";
		
		// First check for a request (there should always be one if a contact relationship exists) and delete it
		// Then delete the contact relationship if this was successful
		// Else do nothing if no request exists, thus enforcing atomicity
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
			results = (String) template.query(sql, new Object[]{username}, rse);
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
			uuid = (String) template.query(sql, new Object[]{username}, rse);
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
			username = (String) template.query(sql, new Object[]{userUuid}, rse);
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
						public Integer extractData(ResultSet rs) // custom because we need an integer
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
		
		String sql = "SELECT * FROM contact_requests WHERE sender_id = ? AND recipient_id = ?";
		
		String results1 = null;
		String results2 = null;
		
		/*
		 * The relationship needs to be checked both ways to prevent
		 * inverse-relationship duplicates.
		 * 
		 * A standard use-case will always have one NOT NULL and one NULL.
		 * If two NOT NULLs are caught, then a duplicate was found.
		 * If two NULLs are found, then no request exists.
		 */
		try {
			results1 = template.query(sql, new Object[]{uuid1, uuid2}, rse);
			results2 = template.query(sql, new Object[]{uuid2, uuid1}, rse);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		
		// if(both results come back empty/null) { return false; }
		// this implies that if either result has a result, then the request and its inverse exists
		if((results1.isEmpty() && results2.isEmpty()) || (results1 == null && results2 == null)) {
			System.out.println("Query results are empty or null!");
			return false;
		} else {
			return true;
		}
		
	}
	
//	public User getUser(String userUuid) {
//		
//		User user = new User();
//		
//		String sql = "SELECT  * FROM users WHERE uuid = ?";
//		
//		String userString = template.query(sql,
//				new Object[]{userUuid},
//				new ResultSetExtractor<String>() {
//				@Override
//				public String extractData(ResultSet rs) throws SQLException,
//						DataAccessException {
//					return rs.next() ? rs.getString(1) : null;
//				}
//				});
//		
//		System.out.println("userString: " + userString);
//		
//		return user;
//	}

}
