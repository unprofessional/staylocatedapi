package com.devcru.staylocatedapi.objects;

public class JsonResponse {

	private String status = "";
	private String errorMessage = "";

	public JsonResponse(String status, String errorMessage) {
		this.setStatus(status);
		this.setErrorMessage(errorMessage);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
}