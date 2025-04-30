package com.unithub.controller;

import com.unithub.exception.ImageUploadException;
import com.unithub.dto.request.event.AtualizarEventoDTO;
import com.unithub.dto.request.event.CadastrarEventoDTO;
import com.unithub.dto.respose.feed.FeedItemDTO;
import com.unithub.dto.respose.subscribe.InscricaoResponseDTO;
import com.unithub.dto.respose.event.EventDetailsDTO;
import com.unithub.dto.respose.feed.FeedDTO;
import com.unithub.service.EventService;
import com.unithub.service.FeedService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/events")
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventDetailsDTO> cadastrarEvento(@RequestParam String title,
                                                           @RequestParam String description,
                                                           @RequestParam LocalDateTime dateTime,
                                                           @RequestParam String location,
                                                           @RequestParam Set<Long> categoriaIds,
                                                           @RequestParam int maxParticipants,
                                                           @RequestParam(required = false) MultipartFile imagem1,
                                                           @RequestParam(required = false) MultipartFile imagem2,
                                                           @RequestParam(required = false) MultipartFile imagem3,
                                                           @RequestParam(required = false) MultipartFile imagem4,
                                                           JwtAuthenticationToken authentication,
                                                           UriComponentsBuilder uriBuilder) throws ImageUploadException {

        CadastrarEventoDTO dto = new CadastrarEventoDTO(title, description, dateTime, location, categoriaIds, maxParticipants);

        List<MultipartFile> imagens = new ArrayList<>();
        if (imagem1 != null) imagens.add(imagem1);
        if (imagem2 != null) imagens.add(imagem2);
        if (imagem3 != null) imagens.add(imagem3);
        if (imagem4 != null) imagens.add(imagem4);

        EventDetailsDTO eventDetails = eventService.cadastrarEvento(dto, imagens, authentication);

        URI uri = uriBuilder.path("/event/{id}").buildAndExpand(eventDetails.eventId()).toUri();
        return ResponseEntity.created(uri).body(eventDetails);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventDetailsDTO> atualizarEvento(@PathVariable UUID eventId,
                                                           @RequestBody AtualizarEventoDTO dados,
                                                           JwtAuthenticationToken authentication) {
        EventDetailsDTO updatedEvent = eventService.atualizarEvento(eventId, dados, authentication);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId, JwtAuthenticationToken authentication) {
        eventService.deletarEvento(eventId, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDTO> feed(
            @RequestParam(value = "pages", defaultValue = "1") int pages,
            @RequestParam(value = "per_page", defaultValue = "10") int per_page,
            @RequestParam(value = "isActive", defaultValue = "true") boolean isActive) {
        FeedDTO feed = feedService.getFeed(pages, per_page, isActive);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/feed-by-course")
    public ResponseEntity<FeedDTO> feedByCourse(
            @RequestParam(value = "pages", defaultValue = "1") int pages,
            @RequestParam(value = "per_page", defaultValue = "10") int per_page,
            @RequestParam(value = "isActive", defaultValue = "true") boolean isActive,
            JwtAuthenticationToken authentication) {
        FeedDTO feed = feedService.getFeedByUserCourse(pages, per_page, isActive, authentication);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/feed-by-course-creator")
    public ResponseEntity<FeedDTO> feedByCourseCreator(
            @RequestParam(value = "pages", defaultValue = "1") int pages,
            @RequestParam(value = "per_page", defaultValue = "10") int per_page,
            @RequestParam(value = "isActive", defaultValue = "true") boolean isActive,
            JwtAuthenticationToken authentication) {
        FeedDTO feed = feedService.getFeedByUserCreatorCourse(pages, per_page, isActive, authentication);
        return ResponseEntity.ok(feed);
    }

    // Inscrição em eventos
    @PostMapping("/subscribe/{eventId}")
    public ResponseEntity<InscricaoResponseDTO> subscribeEvent(@PathVariable UUID eventId, JwtAuthenticationToken authentication) {
        InscricaoResponseDTO response = eventService.subscribeEvent(eventId, authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/unsubscribe/{eventId}")
    public ResponseEntity<Void> unsubscribeEvent(@PathVariable UUID eventId, JwtAuthenticationToken authentication) {
        eventService.unsubscribeEvent(eventId, authentication);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subscribed")
    public ResponseEntity<List<FeedItemDTO>> getSubscribedEvents(JwtAuthenticationToken authentication) {
        List<FeedItemDTO> subscribedEvents = feedService.getSubscribedEvents(authentication);
        return ResponseEntity.ok(subscribedEvents);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailsDTO> getEventById(@PathVariable UUID eventId) {
        EventDetailsDTO eventDetails = eventService.getEventById(eventId);
        return ResponseEntity.ok(eventDetails);
    }
}
