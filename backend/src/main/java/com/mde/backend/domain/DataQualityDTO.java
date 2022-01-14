package com.mde.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataQualityDTO {
	public final DQData data;
	private final boolean success;
	public DataQualityDTO(DQData data, boolean success) {
		super();
		this.data = data;
		this.success = success;
	}
	public DQData getData() {
		return data;
	}
	public boolean isSuccess() {
		return success;
	}
	
	public static class DQData{ 
		private final String file_quality;
		private final double file_quality_score;
		private final double percentNA;
		private final double percentage_missing;
		public DQData(String file_quality, double file_quality_score, double percentNA, double percentage_missing) {
			super();
			this.file_quality_score = file_quality_score;
			this.percentNA = percentNA;
			this.percentage_missing = percentage_missing;
			this.file_quality = file_quality;
		}
		public double getFile_quality_score() {
			return file_quality_score;
		}
		public double getPercentNA() {
			return percentNA;
		}
		public double getPercentage_missing() {
			return percentage_missing;
		}
		public String getFile_quality() {
			return file_quality;
		}	
	}

}


