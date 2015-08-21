package com.devcru.staylocatedapi.dao.impl;

/*
 * XXX: Theory question(s):
 * Q1) We are assigning single values back to List<String> types on some queries while using String types on others -- which is better?
 * A1)   List<String> can absorb multiple values, while String can only do so for a single value
 * 
 * Q2) Should we look into finding a way to return message strings to place into the JsonResponse object?
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
			encodedPassword = (String) template.queryForObject(sql,
					new Object[] {username},
					String.class
					);
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
	public boolean updateContactRequest(User sender, User recipient) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteContact(User sender, User recipient) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/*
	 * DAO Support query-methods
	 */
	
	@Override
	public boolean checkUserExists(String username) {
		
		String sql = "SELECT username FROM users WHERE username = ?";
		String results = null;
		
		try {
			results = (String) template.query(sql,
					new Object[]{username},
					new ResultSetExtractor<String>() {
				@Override
				public String extractData(ResultSet rs) throws SQLException,
						DataAccessException {
					return rs.next() ? rs.getString(1) : null;
				}
				});
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
			uuid = (String) template.query(sql,
					new Object[]{username},
					new ResultSetExtractor<String>() {
				@Override
				public String extractData(ResultSet rs) throws SQLException,
						DataAccessException {
					return rs.next() ? rs.getString(1) : null;
				}
				});
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
			username = (String) template.query(sql,
					new Object[]{userUuid},
					new ResultSetExtractor<String>() {
				@Override
				public String extractData(ResultSet rs) throws SQLException,
						DataAccessException {
					return rs.next() ? rs.getString(1) : null;
				}
				});
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
		int status = -1; // Request doesn't exist
		
		System.out.println("UUID1: " + userUuid1);
		System.out.println("UUID2: " + userUuid2);
		
		try {
			status = template.queryForObject(sql,
					new Object[] { userUuid1, userUuid2 },
					Integer.class);
//					new ResultSetExtractor<Integer>() {
//						@Override
//						public Integer extractData(ResultSet rs)
//								throws SQLException, DataAccessException {
//							return (rs.next() ? rs.getInt(1) : -99);
//						}
//					});
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		
		System.out.println("DEBUG: status: " + status);
		
		return status;
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
