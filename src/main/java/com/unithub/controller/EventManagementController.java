package com.unithub.controller;

import com.unithub.dto.eventsDTOs.Feed.FeedDTO;
import com.unithub.dto.eventsDTOs.Inscricao.InscricoesListDTO;
import com.unithub.dto.eventsDTOs.RecusarEventoDTO;
import com.unithub.service.EventService;
import com.unithub.service.FeedService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/management-event")
public class EventManagementController {
    private final EventService eventService;
    private final FeedService feedService;

    public EventManagementController(EventService eventService, FeedService feedService) {
        this.eventService = eventService;
        this.feedService = feedService;
    }

    // Subscribers List
    @GetMapping("/{eventId}/subscribers")
    public ResponseEntity<List<InscricoesListDTO>> getSubscribers(@PathVariable UUID eventId) {
        List<InscricoesListDTO> response = eventService.getSubscribers(eventId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDTO> feed(@RequestParam(value = "pages", defaultValue = "1") int pages,
                                        @RequestParam(value = "per_page", defaultValue = "10") int per_page) {
        FeedDTO feed = feedService.getFeed(pages, per_page, false);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/feed/representante")
    public ResponseEntity<FeedDTO> feedAlunoRepresentante(@RequestParam(value = "pages", defaultValue = "1") int pages,
                                                          @RequestParam(value = "per_page", defaultValue = "10") int per_page, JwtAuthenticationToken authentication) {
        FeedDTO feed = feedService.getFeedByUserCourse(pages, per_page,false, authentication);
        return ResponseEntity.ok(feed);
    }

    @PatchMapping("/{eventId}/accept")
    public ResponseEntity<FeedDTO> acceptEvent(@PathVariable UUID eventId, JwtAuthenticationToken authentication) {
        eventService.aceitarEvento(eventId, authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/recuse")
    public ResponseEntity<Void> recuseEvent(@PathVariable UUID eventId, @RequestBody RecusarEventoDTO dto, JwtAuthenticationToken authentication) {
        eventService.recusarEvento(eventId, dto, authentication);
        return ResponseEntity.ok().build();
    }
}
