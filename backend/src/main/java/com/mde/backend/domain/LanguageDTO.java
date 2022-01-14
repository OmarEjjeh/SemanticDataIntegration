package com.mde.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LanguageDTO {
	
	private final boolean success;
	public final LangData data;
	public LanguageDTO(boolean success, LangData data) {
		super();
		this.success = success;
		this.data = data;
	}
	public boolean isSuccess() {
		return success;
	}
	public LangData getData() {
		return data;
	}

	public static class LangData{ 
		private final String language;
		private final float language_probability;
		public LangData(String language, float language_probability) {
			super();
			this.language = language;
			this.language_probability = language_probability;
		}
		public String getLanguage() {
			return language;
		}
		public float getLanguage_probability() {
			return language_probability;
		}
	}
	


}




