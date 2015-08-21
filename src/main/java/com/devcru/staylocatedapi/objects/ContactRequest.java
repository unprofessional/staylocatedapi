package com.devcru.staylocatedapi.objects;

import java.util.Date;
import java.util.UUID;

public class ContactRequest {
	
	private UUID sender_id = null;
	private UUID recipient_id = null;
	private int status = -1;
	private Date time_sent = null;
	
	public UUID getSender_id() {
		return sender_id;
	}
	public void setSender_id(UUID sender_id) {
		this.sender_id = sender_id;
	}
	public UUID getRecipient_id() {
		return recipient_id;
	}
	public void setRecipient_id(UUID recipient_id) {
		this.recipient_id = recipient_id;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getTime_sent() {
		return time_sent;
	}
	public void setTime_sent(Date timestamp) {
		this.time_sent = timestamp;
	}
	
}
