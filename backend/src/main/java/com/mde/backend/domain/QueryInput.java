package com.mde.backend.domain;

public class QueryInput {
	private final String dataquality;
	private final String[] keywords;
	//private final TimeFrame timeframe;
	private final String timeframeStart;
	private final String timeframeEnd;
	
	
	public QueryInput(String dataquality, String[] keywords, String timeframeStart, String timeframeEnd) {
		super();
		this.dataquality = dataquality;
		this.keywords = keywords;
		this.timeframeStart = timeframeStart;
		this.timeframeEnd = timeframeEnd;
	}
	public String getDataquality() {
		return dataquality;
	}
	public String[] getKeywords() {
		return keywords;
	}
	public String getTimeframeStart() {
		return timeframeStart;
	}
	public String getTimeframeEnd() {
		return timeframeEnd;
	}
	
	

	

}
