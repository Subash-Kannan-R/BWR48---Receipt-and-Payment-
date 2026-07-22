package com.kfurban.dhanam.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kfurban.dhanam.dto.ConsolidationRequest;
import com.kfurban.dhanam.dto.ConsolidationResponse;
import com.kfurban.dhanam.service.ConsolidationService;

@RestController
@RequestMapping("/api/dhanam/consolidation")
@CrossOrigin // tighten to your actual frontend origin in production
public class ConsolidationController {

    private final ConsolidationService service;

    public ConsolidationController(ConsolidationService service) {
        this.service = service;
    }

    /**
     * Runs the consolidation report (was the whole JSP page render).
     * Read-only: nothing is written back to the database.
     */
    @PostMapping("/generate")
    public ResponseEntity<ConsolidationResponse> generate(@Valid @RequestBody ConsolidationRequest request) {
        return ResponseEntity.ok(service.generate(request));
    }
}