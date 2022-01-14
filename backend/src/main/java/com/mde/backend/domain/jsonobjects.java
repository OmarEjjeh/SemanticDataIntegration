package com.mde.backend.domain;

public class jsonobjects{
	String id;
	String title;
	String description;
	String quality;
	public jsonobjects(String id, String title, String description, String quality) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.quality = quality;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getQuality() {
		return quality;
	}
	public void setQuality(String quality) {
		this.quality = quality;
	}
	
}