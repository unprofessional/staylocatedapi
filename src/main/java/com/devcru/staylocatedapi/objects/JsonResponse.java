package com.devcru.staylocatedapi.objects;

public class JsonResponse {

	private String status = "";
	private String errorMessage = "";
	private String codeDescription = "";
	private int code = -99999;

	public JsonResponse(String status, String errorMessage) {
		this.setStatus(status);
		this.setErrorMessage(errorMessage);
	}
	
	public JsonResponse(String codeDescription, int code) {
		this.setCodeDescription(codeDescription);
		this.setCode(code);
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

	public String getCodeDescription() {
		return codeDescription;
	}

	public void setCodeDescription(String codeDescription) {
		this.codeDescription = codeDescription;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
}