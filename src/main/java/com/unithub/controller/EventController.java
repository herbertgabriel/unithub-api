package com.unithub.controller;

import com.unithub.dto.eventsDTOs.AtualizarEventoDTO;
import com.unithub.dto.eventsDTOs.CadastrarEventoDTO;
import com.unithub.dto.eventsDTOs.Feed.FeedItemDTO;
import com.unithub.dto.eventsDTOs.Inscricao.InscricaoResponseDTO;
import com.unithub.dto.eventsDTOs.EventDetailsDTO;
import com.unithub.dto.eventsDTOs.Feed.FeedDTO;
import com.unithub.service.EventService;
import com.unithub.service.FeedService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;
    private final FeedService feedService;

    public EventController(EventService eventService, FeedService feedService) {
        this.eventService = eventService;
        this.feedService = feedService;
    }

    // CRUD Evento

    @GetMapping("/self-posts")
    public ResponseEntity<List<FeedItemDTO>> getSelfPostFeed(JwtAuthenticationToken authentication) {
        List<FeedItemDTO> feedItems = feedService.getSelfPostFeed(authentication);
        return ResponseEntity.ok(feedItems);
    }

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<EventDetailsDTO> cadastrarEvento(@RequestBody @Valid CadastrarEventoDTO dto, JwtAuthenticationToken authentication, UriComponentsBuilder uriBuilder) {
        EventDetailsDTO eventDetails = eventService.cadastrarEvento(dto, authentication);

        URI uri = uriBuilder.path("/event/{id}").buildAndExpand(eventDetails.eventId()).toUri();
        return ResponseEntity.created(uri).body(eventDetails);
    }

    @PatchMapping("/{eventId}/update")
    public ResponseEntity<EventDetailsDTO> atualizarEvento(@PathVariable UUID eventId,
                                                           @RequestBody AtualizarEventoDTO dados,
                                                           JwtAuthenticationToken authentication) {
        EventDetailsDTO updatedEvent = eventService.atualizarEvento(eventId, dados, authentication);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{eventId}/delete")
    @Transactional
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId, JwtAuthenticationToken authentication) {
        eventService.deletarEvento(eventId, authentication);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDTO> feed(@RequestParam(value = "pages", defaultValue = "1") int pages,
                                        @RequestParam(value = "per_page", defaultValue = "10") int per_page) {
        FeedDTO feed = feedService.getFeed(pages, per_page, true);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/feed/course")
    public ResponseEntity<FeedDTO> feed(@RequestParam(value = "pages", defaultValue = "1") int pages,
                                        @RequestParam(value = "per_page", defaultValue = "10") int per_page,JwtAuthenticationToken authentication) {
        FeedDTO feed = feedService.getFeedByUserCourse(pages, per_page,true, authentication);
        return ResponseEntity.ok(feed);
    }

    // Inscrição em eventos
    @PostMapping("/{eventId}/subscribe")
    @Transactional
    public ResponseEntity<InscricaoResponseDTO> subscribeEvent(@PathVariable UUID eventId, JwtAuthenticationToken authentication) {
        InscricaoResponseDTO response = eventService.subscribeEvent(eventId, authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{eventId}/unsubscribe")
    @Transactional
    public ResponseEntity<Void> unsubscribeEvent(@PathVariable UUID eventId, JwtAuthenticationToken authentication) {
        eventService.unsubscribeEvent(eventId, authentication);
        return ResponseEntity.ok().build();
    }

}
