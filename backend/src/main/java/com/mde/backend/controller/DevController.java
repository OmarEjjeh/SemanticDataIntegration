package com.mde.backend.controller;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.mde.backend.model.FileInfo;
import com.mde.backend.resources.FusekiProvider;
import com.mde.backend.domain.AdminInput;
import com.mde.backend.domain.DataQualityDTO.DQData;
import com.mde.backend.domain.FileDataDTO.FileData;
import com.mde.backend.domain.KnowledgeGraphResponse;
import com.mde.backend.domain.LanguageDTO;
import com.mde.backend.message.ResponseMessage;
import com.mde.backend.services.DevService;
import com.mde.backend.services.ExtractorService;
import com.mde.backend.services.QueryService;

@RestController
@CrossOrigin("http://localhost:3000")
public class DevController {

	@Autowired
	DevService storageService;

	@Autowired
	QueryService queryService;
	
	@Autowired
	ExtractorService extractorService;
	
	@Autowired 
	FusekiProvider fuseki;

	@PostMapping("/upload") // doesnt work right
	public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
		String message = "";
		try {
			storageService.save(file);

			message = "Uploaded the file successfully: " + file.getOriginalFilename();
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
		} catch (Exception e) {
			message = "Could not upload the file: " + file.getOriginalFilename() + "!";
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
		}
	}

	@PostMapping("/testextractorapi")
	public LanguageDTO testapi(@RequestBody AdminInput input) {
		return storageService.testingAPI(input);
	}
	
	@PostMapping("/testrdf")
	public void testrdf() {
		extractorService.castToModel("en", Arrays.asList(new String[]{"foo", "bar"}), "title", "desc", "www.example.com", 
				"2020-02-15T00:00:00.000+02:00", "2021-05-15T00:00:00.000+02:00", new FileData("file.csv", 
						"csv", "01-02-2020", 923823), new DQData("excellent", 1, 0, 0), 1);
	}
	
	@PostMapping("/testdump")
	public KnowledgeGraphResponse testdump() {
		return fuseki.returnAll();
	}

	/*
	 * @GetMapping("/testquery") public KnowledgeGraphResponse testquery() { return
	 * queryService.demoQuery(); }
	 */

	@GetMapping("/sparqltest")
	public KnowledgeGraphResponse sparqltest() {
		return storageService.testQuery();
	}

	@GetMapping("/stringformatter")
	public String formatter() {
		String s = String.format("Hello Folks, welcome to %s !", "Baeldung");

		return s;
	}

	// this might be useful in the future?
	/*
	 * @GetMapping("/files") public ResponseEntity<List<FileInfo>> getListFiles() {
	 * List<FileInfo> fileInfos = storageService.loadAll().map(path -> { String
	 * filename = path.getFileName().toString(); String url =
	 * MvcUriComponentsBuilder .fromMethodName(FilesController.class, "getFile",
	 * path.getFileName().toString()).build().toString();
	 * 
	 * return new FileInfo(filename, url); }).collect(Collectors.toList());
	 * 
	 * return ResponseEntity.status(HttpStatus.OK).body(fileInfos); }
	 * 
	 * @GetMapping("/files/{filename:.+}")
	 * 
	 * @ResponseBody public ResponseEntity<Resource> getFile(@PathVariable String
	 * filename) { Resource file = storageService.load(filename); return
	 * ResponseEntity.ok() .header(HttpHeaders.CONTENT_DISPOSITION,
	 * "attachment; filename=\"" + file.getFilename() + "\"").body(file); }
	 */
}
