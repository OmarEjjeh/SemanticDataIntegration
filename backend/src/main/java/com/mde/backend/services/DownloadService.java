package com.mde.backend.services;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mde.backend.domain.KnowledgeGraph;
import com.mde.backend.domain.KnowledgeGraphResponse;
import com.mde.backend.resources.FusekiProvider;


@Service
public class DownloadService {
	
	@Autowired
	private final ExtractorService extractor;
	@Autowired
	private final FusekiProvider fuseki;
	
	public DownloadService(ExtractorService extractor, FusekiProvider fuseki) {
		super();
		this.extractor = extractor;
		this.fuseki = fuseki;
	}




	public String retrieveDataset(String link, String type) throws Exception{
		KnowledgeGraphResponse response = fuseki.retrieveByLink(link);
		if (!response.isSuccess()) return null;
		KnowledgeGraph data  = response.getData().get(0);
		
		Model model = extractor.castToModel(data.getLanguage(), data.getKeywords(), data.getTitle(),
				data.getDescription(), data.getUrl(), data.getTimeframeStart(), 
				data.getTimeframeEnd(), data.getFilemetadata(), data.getDataQualityDetailed(), data.getId());
		

		OutputStream out = new ByteArrayOutputStream();
		model.write(out, type);
		
		
		return out.toString();
	}

}
