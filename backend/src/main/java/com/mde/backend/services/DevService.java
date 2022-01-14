package com.mde.backend.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.mde.backend.domain.AdminInput;
import com.mde.backend.domain.KnowledgeGraph;
import com.mde.backend.domain.KnowledgeGraphResponse;
import com.mde.backend.domain.LanguageDTO;
import com.mde.backend.domain.LinkDTO;
import com.mde.backend.resources.FusekiProvider;

@Service
public class DevService {
	
	@Autowired
	private final FusekiProvider fuseki;
	
	@Autowired
	private final RestTemplate restTemplate;

	
	private final Logger logger;

    
		
		

	public DevService(FusekiProvider fuseki, RestTemplate restTemplate) {
		super();
		this.fuseki = fuseki;
		this.restTemplate = restTemplate;
		logger = LoggerFactory.getLogger(DevService.class);
	}

	public void save(MultipartFile file) {
		
			logger.info("entered save service");
			InputStream in = null;

		
			try {
				in = file.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			logger.info("content type is" + file.getContentType());

			Model newModel = ModelFactory.createDefaultModel();
			newModel.read(in, "TTL");
			logger.info("Sucessfully loaded model!");



			fuseki.insert(newModel);
			}
	
	public LanguageDTO testingAPI(AdminInput input) {
		logger.info("entered devservice");

		LanguageDTO lang = restTemplate.postForObject(
				"http://localhost:5000/language", new LinkDTO(input.getUrl()), LanguageDTO.class);
		logger.info(lang.getData().getLanguage());
		return lang;
	}
	
	public KnowledgeGraphResponse testQuery() {
		String[] keywords= {"London", "lebanese"};
		String timeframeStart = "2020-02-15T00:00:00+02:00^^http://www.w3.org/2001/XMLSchema#dateTime";
		String timeframeEnd = "2021-05-15T00:00:00+02:00^^http://www.w3.org/2001/XMLSchema#dateTime";
		String dataquality = "Excellent";
		logger.info("entered testQuery function");
		
		KnowledgeGraphResponse response = fuseki.query(keywords, dataquality,  timeframeStart, timeframeEnd);
		
	
		return response;
	}

	
}
