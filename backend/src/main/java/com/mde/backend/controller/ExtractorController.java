package com.mde.backend.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mde.backend.domain.AdminInput;
import com.mde.backend.domain.SuccessDTO;
import com.mde.backend.services.ExtractorService;


@RestController
@CrossOrigin("http://localhost:3000")
public class ExtractorController {
	
	@Autowired
	private final ExtractorService service;
	
	public ExtractorController(ExtractorService service) {
		super();
		this.service = service;
	}

	@CrossOrigin(origins = "http://localhost:3000" )
	@PostMapping("/submit")
	SuccessDTO submit(@RequestBody AdminInput input) throws Exception{
		return new SuccessDTO(service.submit(input));
	}

}

