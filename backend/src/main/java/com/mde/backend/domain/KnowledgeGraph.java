package com.mde.backend.domain;

import java.util.List;

import com.mde.backend.domain.DataQualityDTO.DQData;
import com.mde.backend.domain.FileDataDTO.FileData;

public class KnowledgeGraph {
	
		private final List<String> keywords;
		private final String dataQuality;
		private final String title;
		private final String description;
		private final String timeframeStart;
		private final String timeframeEnd;
		private final String url;
		private final String language;
		private final FileData filemetadata;
		private final DQData dataQualityDetailed;
		private final int id;


		public KnowledgeGraph(List<String> keywords, String dataqualityString, String title, String description,
				String timeframeStart, String timeframeEnd, String url, String language, FileData filemetadata, DQData dataQualityDetailed,
				int id) {
			this.keywords = keywords;
			this.title = title;
			this.description = description;
			this.timeframeStart = timeframeStart;
			this.timeframeEnd = timeframeEnd;
			this.url = url;
			this.filemetadata = filemetadata;
			this.dataQuality = dataqualityString;
			this.dataQualityDetailed = dataQualityDetailed;
			this.language = language;
			this.id = id;
		}
		public List<String> getKeywords() {
			return keywords;
		}
		public String getTitle() {
			return title;
		}
		public String getDescription() {
			return description;
		}
		public String getTimeframeStart() {
			return timeframeStart;
		}
		public String getTimeframeEnd() {
			return timeframeEnd;
		}
		public String getUrl() {
			return url;
		}
		public FileData getFilemetadata() {
			return filemetadata;
		}
		public String getDataQuality() {
			return dataQuality;
		}
		public DQData getDataQualityDetailed() {
			return dataQualityDetailed;
		}
		public String getLanguage() {
			return language;
		}
		public int getId() {
			return id;
		}		
			
}
