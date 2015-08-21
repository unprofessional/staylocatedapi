package com.devcru.staylocatedapi.objects;

public class JsonResponse {

	private String status = "";
	private String message = "";

	public JsonResponse(String status, String message) {
		this.setStatus(status);
		this.setMessage(message);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}