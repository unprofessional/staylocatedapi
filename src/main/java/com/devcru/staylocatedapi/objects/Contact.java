package com.devcru.staylocatedapi.objects;

import java.sql.Timestamp;
import java.util.UUID;

public class Contact {
	
	UUID requester_id = null;
	UUID accepter_id = null;
	Timestamp time_added = null; // Change to proper datatype later
	
	public UUID getRequester_id() {
		return requester_id;
	}
	public void setRequester_id(UUID requester_id) {
		this.requester_id = requester_id;
	}
	
	public UUID getAccepter_id() {
		return accepter_id;
	}
	public void setAccepter_id(UUID accepter_id) {
		this.accepter_id = accepter_id;
	}
	
	public Timestamp getTime_added() {
		return time_added;
	}
	public void setTime_added(Timestamp time_added) {
		this.time_added = time_added;
	}

}
