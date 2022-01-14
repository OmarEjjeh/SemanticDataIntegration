package com.mde.backend.domain;

import java.util.List;

public class dummyFrontend {
	
	public dummyFrontend(List<jsonobjects> objects) {
		super();
		this.objects = objects;
	}

	public List<jsonobjects> getObjects() {
		return objects;
	}

	public void setObjects(List<jsonobjects> objects) {
		this.objects = objects;
	}

	List<jsonobjects> objects;
	

}


