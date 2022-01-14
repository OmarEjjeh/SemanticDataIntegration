package com.mde.backend.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mde.backend.domain.AdminInput;
import com.mde.backend.domain.DataQualityDTO;
import com.mde.backend.domain.DataQualityDTO.DQData;
import com.mde.backend.domain.DateTimeDTO;
import com.mde.backend.domain.FileDataDTO;
import com.mde.backend.domain.FileDataDTO.FileData;
import com.mde.backend.domain.KeywordsDTO;
import com.mde.backend.domain.LanguageDTO;
import com.mde.backend.domain.LinkDTO;
import com.mde.backend.resources.FusekiProvider;



@Service
public class ExtractorService {

	private final Logger logger;

	private final String extractorLocation;
	private final String DataQualityAPI;
	private final String LangAPI;
	private final String KeywordAPI;
	private final String DateTimeAPI;
	private final String FileDataAPI;

	@Autowired
	private final FusekiProvider fuseki;

	@Autowired
	private final RestTemplate restTemplate;

	public ExtractorService(FusekiProvider fuseki, RestTemplate restTemplate) {
		super();
		this.fuseki = fuseki;
		this.restTemplate = restTemplate;
		this.extractorLocation = "http://127.0.0.1:5000";
		this.DataQualityAPI = "/data_quality";
		this.LangAPI = "/language";
		this.KeywordAPI = "/key_word";
		this.DateTimeAPI = "/date_time";
		this.FileDataAPI = "/file_meta_data";
		this.logger = LoggerFactory.getLogger(ExtractorService.class);
	}

	// These could all be one generic function
	private DataQualityDTO retrieveDQ(String url) {

		DataQualityDTO dq = restTemplate.postForObject(extractorLocation + DataQualityAPI, new LinkDTO(url),
				DataQualityDTO.class);
		return dq;
	}

	private FileDataDTO retrieveFD(String url) {
		FileDataDTO data = restTemplate.postForObject(extractorLocation + FileDataAPI, new LinkDTO(url),
				FileDataDTO.class);
		return data;
	}

	private LanguageDTO retrieveLang(String url) {
		LanguageDTO lang = restTemplate.postForObject(extractorLocation + LangAPI, new LinkDTO(url), LanguageDTO.class);
		return lang;
	}

	private KeywordsDTO retrieveKW(String url) {
		KeywordsDTO kw = restTemplate.postForObject(extractorLocation + KeywordAPI, new LinkDTO(url),
				KeywordsDTO.class);
		return kw;
	}

	private DateTimeDTO retrieveDT(String url) {
		DateTimeDTO dt = restTemplate.postForObject(extractorLocation + DateTimeAPI, new LinkDTO(url),
				DateTimeDTO.class);
		return dt;
	}

	public boolean submit(AdminInput input) throws Exception {
		try {
			URL url = new URL(input.getUrl());
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();

			int responseCode = huc.getResponseCode();
			logger.info(String.valueOf(responseCode));

			if (HttpURLConnection.HTTP_OK != responseCode)
				return false;
		} catch (Exception e) {
			return false;

		}

		Model model = createKnowledgeGraph(input);
		
		logger.info("baout to insert");

		fuseki.insert(model);
		return true;
	}

	public Model createKnowledgeGraph(AdminInput input) throws Exception {

		DataQualityDTO dq = retrieveDQ(input.getUrl());
		DateTimeDTO dt = retrieveDT(input.getUrl());
		LanguageDTO lang = retrieveLang(input.getUrl());
		KeywordsDTO keywords = retrieveKW(input.getUrl());
		FileDataDTO filedata = retrieveFD(input.getUrl());
		

		Model model = castToModel(lang.data.getLanguage(), keywords.getData(), input.getTitle(),
				input.getDescription(), input.getUrl(), dt.getData().getStart_date(), 
				dt.getData().getEnd_date(), filedata.getData(), dq.getData(), (int)(Math.random() * 999999999 + 1));
		
		OutputStream testOut = new ByteArrayOutputStream();
		model.write(testOut, "TTL");
		return model;
	}

	public Model castToModel(String lang, List<String> keywords, String title, String description,
			String url, String begin, String end, FileData filedata, DQData dataq, int id) {
		
		Model model = ModelFactory.createDefaultModel();
		String resourceNameLocal = "data"+String.valueOf(id);
		
		String dataPrefix = "https://www.mde.com/datasets/";
		String mdePrefix = "https://www.mde.com/customproperties/";
		String dataFull = dataPrefix + resourceNameLocal + "/";
		String idsPrefix = "https://w3id.org/idsa/core/";
		String idscPrefix = "https://w3id.org/idsa/code/";
		String rdfPrefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		String xsdPrefix = "http://www.w3.org/2001/XMLSchema#";
		String dqvPrefix = "http://www.w3.org/ns/dqv#";
		
		model.setNsPrefix( "ids", idsPrefix );
		model.setNsPrefix( "idsc", idscPrefix );
		model.setNsPrefix( "rdf", rdfPrefix );
		model.setNsPrefix( "xsd", xsdPrefix );
		model.setNsPrefix( resourceNameLocal, dataFull);
		model.setNsPrefix("dqv", dqvPrefix);
		model.setNsPrefix("mdeprops", mdePrefix);


		
		Resource data = model.createResource(dataFull);
		Resource temporalCoverage = model.createResource();
		Resource beginRes = model.createResource();
		Resource endRes = model.createResource();
		Resource representation = model.createResource();
		Resource resourceEndpoint = model.createResource();
		Resource materialization = model.createResource(dataFull+"materialization_csv");
		Resource dataqualityConcept = model.createResource(mdePrefix+ "dataquality");
		Resource dataqualityBlank = model.createResource();
		Resource dataqualityBlankNa = model.createResource();
		Resource dataqualityBlankMissing = model.createResource();
		Resource dataqualityBlankScore = model.createResource();

		
		Resource percentnaConcept = model.createResource(mdePrefix+"percentNA");
		Resource percentMissing = model.createResource(mdePrefix+"percentMissing");
		Resource qualityScore = model.createResource(mdePrefix+"qualityScore");
		
		
		Property type = model.createProperty(rdfPrefix, "type");
		Property titleProp = model.createProperty(idsPrefix, "title");
		Property keywordProp = model.createProperty(idsPrefix, "keyword");
		Property descriptionProp = model.createProperty(idsPrefix, "description");
		Property languageProp = model.createProperty(idsPrefix, "language");
		Property temporalCoverageProp = model.createProperty(idsPrefix, "temporalCoverage");
		Property beginProp = model.createProperty(idsPrefix, "begin");
		Property endProp = model.createProperty(idsPrefix, "end");
		Property dateTimeProp = model.createProperty(idsPrefix, "dateTime");
		Property representationProp = model.createProperty(idsPrefix, "representation");
		Property mediaTypeProp = model.createProperty(idsPrefix, "mediaType");
		Property instanceProp = model.createProperty(idsPrefix, "instance");
		Property endpointProp = model.createProperty(idsPrefix, "resourceEndpoint");
		Property endpointArtifact = model.createProperty(idsPrefix, "endpointArtifact");
		Property accessURL = model.createProperty(idsPrefix, "accessURL");
		Property size = model.createProperty(idsPrefix, "byteSize");
		Property filename = model.createProperty(idsPrefix, "fileName");
		Property creationdate = model.createProperty(idsPrefix, "creationDate");
		Property hasqualitymeasurement = model.createProperty(dqvPrefix, "hasQualityMeasurement");
		Property isMeasurementOf = model.createProperty(dqvPrefix, "isMeasurementOf");
		Property value = model.createProperty(dqvPrefix, "value");
		
		
		data.addProperty(RDF.type, model.createResource(idsPrefix+"TextResource"));
		data.addProperty(titleProp, title);
		for(String s: keywords) {
			data.addProperty(keywordProp, s);
		}
		data.addProperty(descriptionProp, description);
		data.addProperty(languageProp, lang);
		data.addProperty(temporalCoverageProp, temporalCoverage);
		temporalCoverage.addProperty(type, model.createResource(idsPrefix+"Instant"));
		beginRes.addProperty(type, model.createResource(idsPrefix+"Instant"));
		endRes.addProperty(type, model.createResource(idsPrefix+"Instant"));
		beginRes.addProperty(dateTimeProp, model.createTypedLiteral(String.valueOf(begin), xsdPrefix + "dateTimeStamp"));
		endRes.addProperty(dateTimeProp, model.createTypedLiteral(String.valueOf(end), xsdPrefix + "dateTimeStamp"));
		temporalCoverage.addProperty(beginProp, beginRes);
		temporalCoverage.addProperty(endProp, endRes);
		
		data.addProperty(representationProp, representation);
		representation.addProperty(type, model.createResource(idsPrefix+"TextRepresentation"));
		representation.addProperty(mediaTypeProp, model.createResource("https://www.iana.org/assignments/media-types/text/csv"));
		representation.addProperty(instanceProp, model.createResource(dataFull+"materialization_csv"));
		
		data.addProperty(endpointProp, resourceEndpoint);
		resourceEndpoint.addProperty(endpointArtifact, materialization);
		resourceEndpoint.addProperty(accessURL, url);
		resourceEndpoint.addProperty(type, model.createResource(idsPrefix+"ConnectorEndpoint"));
		
		materialization.addProperty(type, model.createResource(idsPrefix+"Artifact"));
		materialization.addProperty(size, model.createTypedLiteral(filedata.getFile_size()));
		materialization.addProperty(filename, String.valueOf(filedata.getFile_name()));
		materialization.addProperty(creationdate, model.createTypedLiteral(String.valueOf(filedata.getCreation_date()), xsdPrefix + "dateTimeStamp"));
		
		data.addProperty(hasqualitymeasurement, dataqualityBlank);
		data.addProperty(hasqualitymeasurement, dataqualityBlankNa);
		data.addProperty(hasqualitymeasurement, dataqualityBlankMissing);
		data.addProperty(hasqualitymeasurement, dataqualityBlankScore);

		
		
		dataqualityBlank.addProperty(isMeasurementOf, dataqualityConcept);
		dataqualityBlankNa.addProperty(isMeasurementOf, percentnaConcept);
		dataqualityBlankMissing.addProperty(isMeasurementOf, percentMissing);
		dataqualityBlankScore.addProperty(isMeasurementOf, qualityScore);

		dataqualityBlank.addProperty(value, dataq.getFile_quality());
		dataqualityBlankNa.addProperty(value, String.valueOf(dataq.getPercentNA()));
		dataqualityBlankMissing.addProperty(value, String.valueOf(dataq.getPercentage_missing()));
		dataqualityBlankScore.addProperty(value, String.valueOf(dataq.getFile_quality_score()));

		
		OutputStream testOut = new ByteArrayOutputStream();
		model.write(testOut, "TTL");
		logger.info(testOut.toString());
		
		return model;
	}

	

}
