package com.unithub.controller;

import com.unithub.dto.eventsDTOs.EnrollDTOs.InscricaoDTO;
import com.unithub.dto.eventsDTOs.EnrollDTOs.InscricaoResponseDTO;
import com.unithub.service.EnrolledService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class EnrolledController {

    private final EnrolledService enrolledService;

    public EnrolledController(EnrolledService enrolledService) {
        this.enrolledService = enrolledService;
    }

    @PostMapping("/subscribe")
    @Transactional
    public ResponseEntity<InscricaoResponseDTO> subscribeEvent(@RequestBody @Valid InscricaoDTO inscricaoDTO) {
        InscricaoResponseDTO response = enrolledService.subscribeEvent(inscricaoDTO);
        return ResponseEntity.ok(response);
    }

}
