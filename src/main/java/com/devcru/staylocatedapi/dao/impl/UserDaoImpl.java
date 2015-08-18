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
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
		String hashedPassword = passwordEncoder.encode(password);
		
		String message = "", sql = null;
		
		sql = "INSERT INTO users (username, password)"
				+ "VALUES ('" + username + "', '" + hashedPassword + "')";
		
		if(checkUserExists(username)) {
			message = "Username exists! Doing nothing!";
		} else {
			message = "Username not found! Creating account!";
			try {
				template.update(sql);
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
		List<String> encodedPassword = null;
		
		try {
			// Retrieve encodedPassword from storage
			encodedPassword = template.query(sql,
					new Object[] {username},
					new RowMapper<String>() {
						public String mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getString(1);
						}
					});
		} catch (DataAccessException e) {
			e.printStackTrace();
			return false;
		}

		// Check for null value returned from the SQL query execution
		if (encodedPassword.isEmpty()) {
			System.out.println("BAD: encodedPassword was empty! No results found for username: " + username);
			return false;
		} else if (encodedPassword.size() == 1) {
			System.out.println("GOOD: encodedPassword contains 1 element! Processing");
			
			// This takes the raw pass and encodes it and then checks if it matches the encoded pass in storage
			boolean passwordMatches = passwordEncoder.matches(password, encodedPassword.get(0));

			if (passwordMatches) { return true; }
			else { return false; }
		} else {
			return false;
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
	
	/*
	 * DAO Support query-methods
	 */
	
	public boolean checkUserExists(String username) {
		
		String sql = "SELECT username FROM users WHERE username = ?";
		
		List<String> results = null;
		try {
			results = template.query(sql,
			new Object[]{username},
			new BeanPropertyRowMapper<String>(String.class));
		} catch (DataAccessException e) {
			e.printStackTrace();
			return false;
		}
		
		if(results.isEmpty()) {
			System.out.println("Username not found!");
			return false;
		} else {
			System.out.println("Username already exists!");
			return true;
		}
	}
	
	public String getUuid(String username) {
		
		String uuid = "";
		String sql = "SELECT uuid FROM users WHERE username = ?";
		
		try {
			uuid = (String) template.queryForObject(sql,
					new Object[]{username},
					String.class);
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
	
	public String getSalt(String username) {
		
		String salt = "";
		
		String sql = "SELECT password_salt FROM users WHERE username = ?";
		
		salt = (String) template.queryForObject(sql,
				new Object[]{username},
				String.class);
		
		System.out.println("getSalt: " + salt);
		
		if(salt == null || salt.isEmpty()) {
			return null;
		} else {
			return salt;
		}
		
	}

}
