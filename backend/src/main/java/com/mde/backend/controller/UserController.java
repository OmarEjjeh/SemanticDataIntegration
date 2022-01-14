package com.mde.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mde.backend.domain.KnowledgeGraphResponse;
import com.mde.backend.services.QueryService;

@RestController
@CrossOrigin("http://localhost:3000")
public class UserController {
	
	@Autowired
	private final QueryService service;
	private final Logger logger;

	public UserController(QueryService service) {
		super();
		this.service = service;
		logger = LoggerFactory.getLogger(UserController.class);

	}


	
	@GetMapping("/query")
	KnowledgeGraphResponse response(@RequestParam(required = false) String keywords, 
			@RequestParam(required = false) String dataquality, @RequestParam(required = false) String timeframeStart, 
			@RequestParam(required = false) String timeframeEnd) {

		
		String[] keywordsList = null;
		if(keywords != null)  keywordsList = keywords.split(" ");
		
		return service.returnQuery(keywordsList, dataquality, timeframeStart, timeframeEnd);
	}
	
	
}
