package com.devcru.staylocatedapi.dao;

import com.devcru.staylocatedapi.objects.User;

public interface UserDao {

	boolean insertUser(User user);

	boolean updateUser(User user);

	boolean deleteUser(User user);

	/*
	 * XXX: Is this even necessary if using OAuth2 to authenticate exclusively
	 * with no intended statefulness? Maybe on an initial client login, but I
	 * feel as if abstracting even that away from OAuth2 via setting a
	 * dataSource may nullify the need for a login endpoint.
	 */
	//boolean verifyUserCreds(User user);

}
