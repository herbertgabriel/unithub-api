package com.unithub.controller;

import com.unithub.dto.eventsDTOs.CadastrarEventoDTO;
import com.unithub.dto.eventsDTOs.Inscricao.InscricaoDTO;
import com.unithub.dto.eventsDTOs.Inscricao.InscricaoResponseDTO;
import com.unithub.dto.eventsDTOs.EventDetailsDTO;
import com.unithub.dto.eventsDTOs.Feed.FeedDTO;
import com.unithub.dto.eventsDTOs.Inscricao.InscricoesListDTO;
import com.unithub.service.EventService;
import com.unithub.service.FeedService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<EventDetailsDTO> cadastrarEvento(@RequestBody @Valid CadastrarEventoDTO dto, UriComponentsBuilder uriBuilder) {
        EventDetailsDTO eventDetails = eventService.cadastrarEvento(dto);

        URI uri = uriBuilder.path("/event/{id}").buildAndExpand(eventDetails.eventId()).toUri();
        return ResponseEntity.created(uri).body(eventDetails);
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDTO> feed(@RequestParam(value = "pages", defaultValue = "1") int pages,
                                        @RequestParam(value = "per_page", defaultValue = "10") int per_page) {
        FeedDTO feed = feedService.getFeedActivate(pages, per_page);
        return ResponseEntity.ok(feed);
    }

    // FEED DESACTIVATED
    @PostMapping("/subscribe")
    @Transactional
    public ResponseEntity<InscricaoResponseDTO> subscribeEvent(@RequestBody @Valid InscricaoDTO inscricaoDTO) {
        InscricaoResponseDTO response = eventService.subscribeEvent(inscricaoDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/unsubscribe")
    @Transactional
    public ResponseEntity<Void> unsubscribeEvent(@RequestBody @Valid InscricaoDTO inscricaoDTO) {
        eventService.unsubscribeEvent(inscricaoDTO);
        return ResponseEntity.ok().build();
    }

    // Subscribers List
    @GetMapping("/{eventId}/subscribers")
    @Transactional
    public ResponseEntity<List<InscricoesListDTO>> getSubscribers(@PathVariable UUID eventId) {
        List<InscricoesListDTO> response = eventService.getSubscribers(eventId);
        return ResponseEntity.ok(response);
    }
}
