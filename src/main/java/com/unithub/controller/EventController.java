package com.unithub.controller;

import com.unithub.dto.CadastrarEventoDTO;
import com.unithub.dto.EventDetailsDTO;
import com.unithub.service.EventService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping("/event")
    @Transactional
    public ResponseEntity<EventDetailsDTO> cadastrarEvento(@RequestBody @Valid CadastrarEventoDTO dto, UriComponentsBuilder uriBuilder) {
        EventDetailsDTO eventDetails = eventService.cadastrarEvento(dto);

        URI uri = uriBuilder.path("/event/{id}").buildAndExpand(eventDetails.eventId()).toUri();
        return ResponseEntity.created(uri).body(eventDetails);
    }
}
