package com.devcru.staylocatedapi.objects;

public class JsonResponse {

	private String status = "";
	private String message = "";

	public JsonResponse(String status, String message) {
		this.setStatus(status);
		this.setErrorMessage(message);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorMessage() {
		return message;
	}

	public void setErrorMessage(String message) {
		this.message = message;
	}
	
}