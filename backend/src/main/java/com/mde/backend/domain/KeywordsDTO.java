package com.mde.backend.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeywordsDTO { // DTO for consuming the keywords API
	
	private final boolean success;
	private final List<String> data;
	public KeywordsDTO(boolean success, List<String> data) {
		super();
		this.success = success;
		this.data = data;
	}
	public boolean isSuccess() {
		return success;
	}
	public List<String> getData() {
		return data;
	}

}
