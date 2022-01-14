package com.mde.backend.domain;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeGraphResponse {
	
	private  boolean success;
	public  List<KnowledgeGraph> data;
	
	public KnowledgeGraphResponse() {
		this.success = false;
		this.data = new ArrayList<KnowledgeGraph>();

	}
	
	public KnowledgeGraphResponse(List<KnowledgeGraph> data, boolean success) {
		super();
		this.data = data;
		this.success = success;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public List<KnowledgeGraph> getData() {
		return data;
	}

	public void setData(List<KnowledgeGraph> data) {
		this.data = data;
	}



	

}
