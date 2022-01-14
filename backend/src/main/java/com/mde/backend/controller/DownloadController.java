package com.mde.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mde.backend.domain.LinkDTO;
import com.mde.backend.services.DownloadService;

@RestController
@CrossOrigin("http://localhost:3000")
public class DownloadController {

	@Autowired
	private final DownloadService service;

	public DownloadController(DownloadService service) {
		super();
		this.service = service;
	}

	@GetMapping("/download")
	ResponseEntity<byte[]> response(@RequestParam String link, @RequestParam String type) {
		
		try {
		String dataset = service.retrieveDataset(link, type); 
		HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE); // (3) Content-Type: application/octet-stream
        httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(String.format("metadata.", type)).build().toString()); // (4) Content-Disposition: attachment; filename="demo-file.txt"

        return ResponseEntity.ok().headers(httpHeaders).body(dataset.getBytes()); // (5) Return Response

		}
		catch(Exception e){
			String dataset = null; 

			HttpHeaders httpHeaders = new HttpHeaders();
	        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE); // (3) Content-Type: application/octet-stream
	        httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(String.format("metadata.", type)).build().toString()); // (4) Content-Disposition: attachment; filename="demo-file.txt"


	        return ResponseEntity.ok().headers(httpHeaders).body(dataset.getBytes()); // (5) Return Response
		}
			}

}


