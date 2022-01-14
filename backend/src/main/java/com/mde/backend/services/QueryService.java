package com.mde.backend.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.query.ResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mde.backend.domain.DataQualityDTO.DQData;
import com.mde.backend.domain.FileDataDTO.FileData;
import com.mde.backend.domain.KnowledgeGraph;
import com.mde.backend.domain.KnowledgeGraphResponse;
import com.mde.backend.resources.FusekiProvider;

@Service
public class QueryService {
	
	@Autowired
	private FusekiProvider fuseki;
	
	public KnowledgeGraphResponse returnQuery(String[] keywords, String dataquality, 
			String timeframeStart, String timeframeEnd) {
		KnowledgeGraphResponse response = fuseki.query(keywords, dataquality, timeframeStart, timeframeEnd);
		return response;
	}


	
	public KnowledgeGraphResponse demoQuery() {
		return fuseki.returnAll();
	}
	
	

	
}
