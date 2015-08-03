package com.devcru.staylocatedapi.dao.impl;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.devcru.staylocatedapi.dao.UserDao;
import com.devcru.staylocatedapi.objects.User;
import com.devcru.staylocatedapi.utils.PasswordHash;

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
		System.out.println("[!!!!!!] " + " username: " + username + " | password: " + password);
		
		String message = "", salt = null, sql = null;
		
		try {
			password = PasswordHash.createHash(password);
			String[] passwordData = password.split(":"); // 0 = iterations, 1 = salt, 2 = finished hash
			salt = passwordData[1];
			password = passwordData[2];
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		System.out.println("username: " + username + " | password: " + password + " | salt: " + salt);
		
		sql = "INSERT INTO users (username, password_hash, password_salt)"
				+ "VALUES ('" + username + "', '" + password + "', '" + salt + "')";
		
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
		
		boolean isSuccess = false;
		
		String username = user.getUsername();
		String password = user.getPassword();
		System.out.println("[!!!!!!] " + " username: " + username + " | password: " + password);
		
		String message = "", salt = null, sql = null;
		
		salt = getSalt(username);
		System.out.println("salt: " + salt);
		
		try {
			// Pass in salt-hex to custom method in PasswordHash
			password = PasswordHash.createHashWithSalt(password, salt);
			String[] passwordData = password.split(":"); // 0 = iterations, 1 = salt, 2 = finished hash
			//salt = passwordData[1];
			password = passwordData[2];
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		System.out.println("username: " + username + " | password: " + password + " | salt: " + salt);
		
		sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
		
		List<String> results = null;
		try {
			results = template.query(sql,
					new Object[]{username, password},
					new BeanPropertyRowMapper<String>(String.class));
			isSuccess = true;
		} catch (DataAccessException e) {
			e.printStackTrace();
			isSuccess = false;
		}
		
		if(results.isEmpty()) {
			message = "Username or password not recognized!";
		} else {
			message = "Logged in successfully!";
		}
		
		System.out.println("message: " + message);
		
		return isSuccess;
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
		
		//boolean userExists = false;
		
		String sql = "SELECT * FROM users WHERE username = ?";
		
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
		
		uuid = (String) template.queryForObject(sql,
				new Object[]{username},
				String.class);
		
		System.out.println("getUuid: " + uuid);
		
		return uuid; // XXX: Anything that calls this will need to check for the possibility of a null return value
		
	}
	
	public String getSalt(String username) {
		
		String salt = "";
		
		String sql = "SELECT password_salt FROM users WHERE username = ?";
		
		salt = (String) template.queryForObject(sql,
				new Object[]{username},
				String.class);
		
		System.out.println("getSalt: " + salt);
		
		return salt; // XXX: Anything that calls this will need to check for the possibility of a null return value
		
	}

}
