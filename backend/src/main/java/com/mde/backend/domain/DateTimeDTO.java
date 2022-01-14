package com.mde.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DateTimeDTO {
	
	private final boolean success;
	private final DateTimeData data; // how to represent time objects?
	
	public DateTimeDTO(boolean success, DateTimeData data) {
		super();
		this.success = success;
		this.data = data;
	}
	public boolean isSuccess() {
		return success;
	}
	public DateTimeData getData() {
		return data;
	}
	
	public static class DateTimeData{ 
		private final String end_date;
		private final String start_date;
		public DateTimeData(String end_date, String start_date) {
			super();
			this.start_date = start_date;
			this.end_date = end_date;
		}
		public String getEnd_date() {
			return end_date;
		}
		public String getStart_date() {
			return start_date;
		}
		

}
}
