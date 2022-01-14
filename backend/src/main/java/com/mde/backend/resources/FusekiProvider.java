package com.mde.backend.resources;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PreDestroy;

import org.apache.jena.fuseki.main.*;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.SplitIRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mde.backend.BackendApplication;
import com.mde.backend.domain.DataQualityDTO.DQData;
import com.mde.backend.domain.FileDataDTO.FileData;
import com.mde.backend.domain.KnowledgeGraph;
import com.mde.backend.domain.KnowledgeGraphResponse;

import java.util.stream.Collectors;

@Repository
public class FusekiProvider {

	@Value("${dataset.location:DefaultTDB}")
	private String datasetLocation = "TDB"; 
	@Value("${fuseki.url:http://localhost:3330}")
	private String fusekiURL = "http://localhost:3330";
	@Value("${fuseki.endpoint:/default}")
	private String endpoint = "/rdf";
	private Dataset ds;
	private FusekiServer server;
	private final RDFConnection conn;

	private final Logger logger;

	// query for retrieving all datasets
	private final String fullQuery = "prefix ids: <https://w3id.org/idsa/core/> \r\n"
			+ "prefix idsc: <https://w3id.org/idsa/code/> \r\n"
			+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \r\n"
			+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> \r\n"
			+ "prefix mdeprops: <https://www.mde.com/customproperties/> \r\n"
			+ "prefix dqv:      <http://www.w3.org/ns/dqv#> \r\n" + "\r\n" + "\r\n"
			+ "SELECT ?resource ?title ?description ?url ?language ?begin ?end ?filename ?filetype ?creationdate ?filesize ?quality ?score ?na ?missing\r\n"
			+ "WHERE {?resource a ids:TextResource .\r\n" + "	   ?resource ids:title ?title .\r\n"
			+ "	   ?resource ids:description ?description .	\r\n"
			+ "	   ?resource ids:temporalCoverage ?coverage .\r\n" + "	   ?coverage ids:begin ?beginObject .\r\n"
			+ "	   ?beginObject ids:dateTime ?begin .\r\n" + "	   ?coverage ids:end ?endObject .\r\n"
			+ "	   ?endObject ids:dateTime ?end .\r\n" + "	   ?resource ids:resourceEndpoint ?endpoint.\r\n"
			+ "	   ?endpoint ids:accessURL ?url .\r\n" + "	   ?resource ids:language ?language .\r\n" + "	   \r\n"
			+ "	   ?resource ids:representation ?repr .\r\n" + "	   ?repr ids:mediaType ?filetype .\r\n"
			+ "	   ?repr ids:instance ?instance .\r\n" + "	   ?instance ids:byteSize ?filesize .\r\n"
			+ "	   ?instance ids:fileName ?filename .\r\n" + "	   ?instance ids:creationDate ?creationdate .\r\n"
			+ "	   \r\n" + "	   ?resource dqv:hasQualityMeasurement ?qualityBlank .\r\n"
			+ "	   ?qualityBlank dqv:isMeasurementOf mdeprops:dataquality .\r\n"
			+ "	   ?qualityBlank dqv:value ?quality .\r\n" + "	   \r\n"
			+ "	   ?resource dqv:hasQualityMeasurement ?qualityNA .\r\n"
			+ "	   ?qualityNA dqv:isMeasurementOf mdeprops:percentNA .\r\n" + "	   ?qualityNA dqv:value ?na .\r\n"
			+ "	   \r\n" + "	   ?resource dqv:hasQualityMeasurement ?qualityScore .\r\n"
			+ "	   ?qualityScore dqv:isMeasurementOf mdeprops:qualityScore .\r\n"
			+ "	   ?qualityScore dqv:value ?score .\r\n" + "	   \r\n"
			+ "	   ?resource dqv:hasQualityMeasurement ?qualityMissing .\r\n"
			+ "	   ?qualityMissing dqv:isMeasurementOf mdeprops:percentMissing .\r\n"
			+ "	   ?qualityMissing dqv:value ?missing .\r\n" + "	   \r\n" + "}\r\n" + "\r\n" + "";

	public FusekiProvider() {
		ds = TDBFactory.createDataset(datasetLocation);
		this.server = FusekiServer.create().add(endpoint, ds).build();
		logger = LoggerFactory.getLogger(FusekiProvider.class);
		logger.info("Created Fuseki server");
		this.server.start();
		this.conn = RDFConnectionFactory.connect(fusekiURL + endpoint);
		logger.info("Connected to Fuseki server");
		logger.info("Initializing Fuseki server");
		init();
		logger.info("Init complete");
	}

	@Override
	public void finalize() { // graceful shutdown of the fuseki server
		stop();
		System.out.println("Stopped Fuseki in the finalizer");
	}

	public void start() {
		server.start();
	}

	public void stop() {
		server.stop();
	}

	public void insert(Model input) {
		conn.load(input);
		return;
	}

	@PreDestroy
	public void preDestroy() {
		conn.close();
		logger.info("Closed fuseki connection");
		server.stop();
		logger.info("Stopped fuseki");
	}

	public KnowledgeGraphResponse returnAll() {
		KnowledgeGraphResponse response = new KnowledgeGraphResponse();
		response.setSuccess(false);

		String queryString = fullQuery;

		Query query = QueryFactory.create(queryString);
		QueryExecution qExec = conn.query(query);
		ResultSet rs = qExec.execSelect();

		String resource = null;

		String titleRes = null;
		String descriptionRes = null;
		String urlRes = null;
		String languageRes = null;
		String dataqualityRes = null;
		String qualityScore = null;
		String qualityNA = null;
		String qualityMissing = null;
		String timeframeStartRes = null;
		String timeframeEndRes = null;
		String filename = null;
		String filetype = null;
		String creationdate = null;
		String filesize = null;

		// Retrieve all datasets
		while (rs.hasNext()) {
			QuerySolution sol = rs.next();
			Iterator<String> s = sol.varNames();
			while (s.hasNext()) {
				String var = s.next();
				RDFNode value = sol.get(var);
				switch (var) {
				case "resource":
					resource = value.toString();
					break;
				case "title":
					titleRes = value.toString();
					break;
				case "description":
					descriptionRes = value.toString();
					break;
				case "url":
					urlRes = value.toString();
					break;
				case "language":
					languageRes = value.toString();
					break;
				case "begin":
					timeframeStartRes = value.toString();
					break;
				case "end":
					timeframeEndRes = value.toString();
					break;
				case "filename":
					filename = value.toString();
					break;
				case "filetype":
					filetype = value.toString();
					break;
				case "creationdate":
					creationdate = value.toString();
					break;
				case "filesize":
					filesize = value.toString();
					break;
				case "quality":
					dataqualityRes = value.toString();
					break;
				case "score":
					qualityScore = value.toString();
					break;
				case "na":
					qualityNA = value.toString();
					break;
				case "missing":
					qualityMissing = value.toString();
					break;
				default:
					// code block
				}
			}

			
			// remove type annotations for output
			String[] parts = filesize.split("\\^\\^");
			int fs = Integer.parseInt(parts[0]);
			float scoref = Float.parseFloat(qualityScore);
			float naf = Float.parseFloat(qualityNA);
			float missingf = Float.parseFloat(qualityMissing);
			parts = creationdate.split("\\^\\^");
			creationdate = parts[0];
			parts = creationdate.split("T");
			creationdate = parts[0];
			parts = filetype.split("/");
			filetype = parts[parts.length - 1];
			parts = languageRes.split("@");
			languageRes = parts[0];
			parts = descriptionRes.split("@");
			descriptionRes = parts[0];
			parts = titleRes.split("@");
			titleRes = parts[0];
			
			String spl[] = resource.split("/");
			String resourceLocal = spl[spl.length - 1]; // need local resource name to query

			String[] part = resourceLocal.split("(?<=\\D)(?=\\d)");
		    String idString = part[1];

			int id = Integer.parseInt(idString);


			

			parts = timeframeStartRes.split("T");
			timeframeStartRes = parts[0];
			parts = timeframeEndRes.split("T");
			timeframeEndRes = parts[0];

			// Retrieve all the keywords for the current resource

			ArrayList<String> keywordsRes = new ArrayList<String>();

			// Construct query for the current resource
			String keywordQuery = String.format("prefix ids: <https://w3id.org/idsa/core/> \r\n"
					+ "prefix idsc: <https://w3id.org/idsa/code/> \r\n"
					+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \r\n"
					+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> \r\n" + "\r\n" + "prefix %s: <%s>\r\n" + "\r\n"
					+ "SELECT ?keyword\r\n" + "WHERE {%s: ids:keyword ?keyword .\r\n" + "	   \r\n" + "}",
					resourceLocal, resource, resourceLocal);

			Query queryKeyword = QueryFactory.create(keywordQuery);
			QueryExecution qExecKeyword = conn.query(queryKeyword);
			ResultSet rsKeyword = qExecKeyword.execSelect();

			while (rsKeyword.hasNext()) {
				QuerySolution solK = rsKeyword.next();
				keywordsRes.add(solK.get("keyword").toString()); // add keywords to keywordslist
			}
			qExecKeyword.close();

			KnowledgeGraph graph = new KnowledgeGraph(keywordsRes, dataqualityRes, titleRes, descriptionRes,
					timeframeStartRes, timeframeEndRes, urlRes, languageRes,
					new FileData(filename, filetype, creationdate, fs),
					new DQData(dataqualityRes, scoref, naf, missingf), id);
			response.data.add(graph);
			response.setSuccess(true);
		}

		qExec.close();
		return response;

	}

	public KnowledgeGraphResponse query(String[] keywords, String dataquality, String timeframeStart,
			String timeframeEnd) {

		KnowledgeGraphResponse fullData = returnAll();
		if (!fullData.isSuccess()) {
			return fullData;
		}

		if (dataquality != null) {
			if (!dataquality.equals("all")) {
				fullData.data.removeIf(d -> !d.getDataQuality().equals(dataquality));
			}
			if (fullData.data.isEmpty()) {
				return fullData;}
		}
		

		
		for (int i = 0; i < fullData.data.size(); i++) {
			if (fullData.data.isEmpty()) {
				fullData.setSuccess(false);
				return fullData;
			}
			if (timeframeStart != null && timeframeStart.compareTo(fullData.getData().get(i).getTimeframeStart()) > 0) {
				fullData.data.remove(i);
				i--;
			}

			if (timeframeEnd != null && timeframeEnd.compareTo(fullData.getData().get(i).getTimeframeEnd()) < 0) {
				fullData.data.remove(i);
				i--;
			}
		}

		if (fullData.data.isEmpty()) {
			return fullData;
		}

		if (keywords != null) {
			for (int i = 0; i < fullData.data.size(); i++) {
				if (fullData.data.isEmpty()) {
					return fullData;
				}

				boolean contains = false;
				for (String key : keywords) {
					if (!(fullData.getData().get(i).getTitle().toLowerCase().contains(key.toLowerCase())
							|| fullData.getData().get(i).getDescription().toLowerCase().contains(key.toLowerCase()))) {
						for (String dataKey : fullData.getData().get(i).getKeywords()) {
							if (dataKey.toLowerCase().contains(key.toLowerCase())) {
								contains = true;
							}

						}
					}
					else contains = true;
									
				}
				if (!contains) {
					fullData.getData().remove(i);
					i--;
				}

			}
		}
		
		
		if (fullData.data.isEmpty()) {
			return fullData;
		}
		return fullData;
	}
	
	public KnowledgeGraphResponse retrieveByLink(String url) {
		KnowledgeGraphResponse response = returnAll();
		response.data.removeIf(d -> !d.getUrl().equals(url));
		if (response.data.isEmpty()) response.setSuccess(false); 
		return response;
	}

	private void preloadDatasets() {
		String wd = System.getProperty("user.dir");
		System.out.println(wd);
		File folder = new File(wd + "/datasets");
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles == null)
			return;
		for (int i = 0; i < listOfFiles.length; i++) {
			Model model = ModelFactory.createDefaultModel();

			model.read(listOfFiles[i].toString());

			conn.load(model);

		}
	}

	private void deleteEverything() {
		String deleteQuery = "DELETE WHERE \r\n" + "{ ?s ?p ?o }\r\n" + "\r\n" + "";

		UpdateRequest req = UpdateFactory.create(deleteQuery);
		conn.update(req);

	}

	private void init() {
		//deleteEverything(); // only for dev purposes
		preloadDatasets();
	}
}
