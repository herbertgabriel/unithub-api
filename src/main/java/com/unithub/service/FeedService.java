package com.unithub.service;

import com.unithub.dto.eventsDTOs.Feed.FeedDTO;
import com.unithub.dto.eventsDTOs.Feed.FeedItemDTO;
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
        var eventsPage = eventRepository.findAllByActive(true, PageRequest.of(page - 1, pageSize, Sort.Direction.DESC, "creationTimeStamp"));

        var feedItems = eventsPage.getContent().stream()
                .map(event -> new FeedItemDTO(
                        event.getEventId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDateTime(),
                        event.getLocation(),
                        event.getCategory(),
                        event.isActive()
                ))
                .collect(Collectors.toList());

        return new FeedDTO(feedItems, page, pageSize, eventsPage.getTotalPages(), eventsPage.getTotalElements());
    }

    public FeedDTO getFeedDesactivate(int page, int pageSize) {
        var eventsPage = eventRepository.findAllByActive(false, PageRequest.of(page - 1, pageSize, Sort.Direction.DESC, "creationTimeStamp"));

        var feedItems = eventsPage.getContent().stream()
                .map(event -> new FeedItemDTO(
                        event.getEventId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDateTime(),
                        event.getLocation(),
                        event.getCategory(),
                        event.isActive()
                ))
                .collect(Collectors.toList());

        return new FeedDTO(feedItems, page, pageSize, eventsPage.getTotalPages(), eventsPage.getTotalElements());
    }

    // Funcionalidade para user ver todos seus proprios posts
    public List<FeedItemDTO> getSelfPostFeed(JwtAuthenticationToken authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return eventRepository.findByCreatorUserOrderByCreationTimeStampDesc(user)
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