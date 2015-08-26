package com.devcru.staylocatedapi.objects;

import java.util.UUID;

public class User {
	
	// FIXME: The current user in the DB either needs to be updated to reflect this,
	// or this needs to reflet what's in the DB.
	private UUID uuid = null;
	private String username = "";
	private String password = "";
	private String email = "";
	private String firstName = "";
	private String lastName = "";
	
	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

}
