package com.unithub.service;

import com.unithub.dto.eventsDTOs.FeedDTOs.FeedDTO;
import com.unithub.dto.eventsDTOs.FeedDTOs.FeedItemDTO;
import com.unithub.repository.EventRepository;
import com.unithub.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FeedService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public FeedService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public FeedDTO getFeedActivate(int page, int pageSize) {
        var events = eventRepository.findAllByActive(true, PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimeStamp"))
                .map(event -> new FeedItemDTO(
                        event.getEventId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDateTime(),
                        event.getLocation(),
                        event.getCategory(),
                        event.isActive()
                ));
        return new FeedDTO(events.getContent(), page, pageSize, events.getTotalPages(), events.getTotalElements());
    }

    public FeedDTO getFeedDesactivate(int page, int pageSize) {
        var events = eventRepository.findAllByActive(false, PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimeStamp"))
                .map(event -> new FeedItemDTO(
                        event.getEventId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDateTime(),
                        event.getLocation(),
                        event.getCategory(),
                        event.isActive()
                ));
        return new FeedDTO(events.getContent(), page, pageSize, events.getTotalPages(), events.getTotalElements());
    }

    // Funcionalidade para user ver todos seus proprios posts

    public List<FeedItemDTO> getSelfPostFeed(JwtAuthenticationToken authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return eventRepository.findByUserOrderByCreationTimestampDesc(user)
                .stream()
                .map(event -> new FeedItemDTO(
                        event.getEventId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDateTime(),
                        event.getLocation(),
                        event.getCategory(),
                        event.isActive()))
                .collect(Collectors.toList());
    }
}