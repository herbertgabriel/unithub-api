package com.unithub.service;

import com.unithub.dto.eventsDTOs.Feed.FeedDTO;
import com.unithub.dto.eventsDTOs.Feed.FeedItemDTO;
import com.unithub.model.Course;
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

    public FeedDTO getFeed(int page, int pageSize, boolean isActive) {
        var eventsPage = eventRepository.findAllByActive(isActive, PageRequest.of(page - 1, pageSize, Sort.Direction.DESC, "creationTimeStamp"));

        var feedItems = eventsPage.getContent().stream()
                .map(event -> new FeedItemDTO(
                        event.getEventId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDateTime(),
                        event.getLocation(),
                        event.getCategorias().stream().map(Course.Categorys::getDescricao).collect(Collectors.toSet()),
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
                        event.getCategorias().stream().map(Course.Categorys::getDescricao).collect(Collectors.toSet()),
                        event.isActive()))
                .collect(Collectors.toList());
    }

    public FeedDTO getFeedByUserCourse(int page, int pageSize, boolean isActive, JwtAuthenticationToken authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var userCourse = user.getCourse();
        var userCategory = userCourse != null ? userCourse.getCategoria() : null;

        var eventsPage = eventRepository.findAllByActive(isActive, PageRequest.of(page - 1, pageSize, Sort.Direction.DESC, "creationTimeStamp"));

        var feedItems = eventsPage.getContent().stream()
                .sorted((event1, event2) -> {
                    boolean event1Matches = userCategory != null && event1.getCategorias().contains(userCategory);
                    boolean event2Matches = userCategory != null && event2.getCategorias().contains(userCategory);

                    if (event1Matches && !event2Matches) {
                        return -1;
                    } else if (!event1Matches && event2Matches) {
                        return 1;
                    } else {
                        return 0;
                    }
                })
                .map(event -> new FeedItemDTO(
                        event.getEventId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDateTime(),
                        event.getLocation(),
                        event.getCategorias().stream().map(Course.Categorys::getDescricao).collect(Collectors.toSet()), // Converte categorias para strings
                        event.isActive()
                ))
                .collect(Collectors.toList());

        return new FeedDTO(feedItems, page, pageSize, eventsPage.getTotalPages(), eventsPage.getTotalElements());
    }
}