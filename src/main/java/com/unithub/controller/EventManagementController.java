package com.unithub.controller;

import com.unithub.dto.eventsDTOs.Inscricao.InscricoesListDTO;
import com.unithub.dto.eventsDTOs.RecusarEventoDTO;
import com.unithub.service.EventService;
import com.unithub.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/management")
public class EventManagementController {
    private final EventService eventService;

    public EventManagementController(EventService eventService) {
        this.eventService = eventService;
    }

    // Subscribers List
    @GetMapping("/subscribers/{eventId}")
    public ResponseEntity<List<InscricoesListDTO>> getSubscribers(@PathVariable UUID eventId, JwtAuthenticationToken authentication) {
        List<InscricoesListDTO> response = eventService.getSubscribers(eventId, authentication);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<Void> acceptEvent(@PathVariable UUID eventId, JwtAuthenticationToken authentication) {
        eventService.aceitarEvento(eventId, authentication);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> recuseEvent(@PathVariable UUID eventId, @RequestBody RecusarEventoDTO dto, JwtAuthenticationToken authentication) {
        eventService.recusarEvento(eventId, dto, authentication);
        return ResponseEntity.noContent().build();
    }
}
