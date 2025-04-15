package com.unithub.service;

import com.unithub.dto.respose.feed.FeedDTO;
import com.unithub.dto.respose.feed.FeedItemDTO;
import com.unithub.model.Category;
import com.unithub.repository.EventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedService {
    private final EventRepository eventRepository;
    private final AuthService authService;

    public FeedService(EventRepository eventRepository, AuthService authService) {
        this.eventRepository = eventRepository;
        this.authService = authService;
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
                        event.getCategorias().stream().map(Category::getDescricao).collect(Collectors.toSet()),
                        event.isActive(),
                        event.isOfficial(),
                        event.getImages()
                ))
                .collect(Collectors.toList());

        return new FeedDTO(feedItems, page, pageSize, eventsPage.getTotalPages(), eventsPage.getTotalElements());
    }

    // Funcionalidade para user ver todos seus proprios posts
    public List<FeedItemDTO> getSelfPostFeed(JwtAuthenticationToken authentication) {
        var usuario = authService.getAuthenticatedUser(authentication);

        return eventRepository.findByCreatorUserOrderByCreationTimeStampDesc(usuario)
                .stream()
                .map(event -> new FeedItemDTO(
                        event.getEventId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDateTime(),
                        event.getLocation(),
                        event.getCategorias().stream().map(Category::getDescricao).collect(Collectors.toSet()),
                        event.isActive(),
                        event.isOfficial(),
                        event.getImages()))
                .collect(Collectors.toList());
    }

    public FeedDTO getFeedByUserCourse(int page, int pageSize, boolean isActive, JwtAuthenticationToken authentication) {

        var usuario = authService.getAuthenticatedUser(authentication);

        var userCourse = usuario.getCourse();
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
                        event.getCategorias().stream().map(Category::getDescricao).collect(Collectors.toSet()), // Converte categorias para strings
                        event.isActive(),
                        event.isOfficial(),
                        event.getImages()
                ))
                .collect(Collectors.toList());
        return new FeedDTO(feedItems, page, pageSize, eventsPage.getTotalPages(), eventsPage.getTotalElements());
    }

    public List<FeedItemDTO> getSubscribedEvents(JwtAuthenticationToken authentication) {
        var usuario = authService.getAuthenticatedUser(authentication);
    
        var subscribedEvents = eventRepository.findAllByEnrolledUserListContains(usuario);
    
        return subscribedEvents.stream()
                .map(event -> new FeedItemDTO(
                        event.getEventId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDateTime(),
                        event.getLocation(),
                        event.getCategorias().stream().map(Category::getDescricao).collect(Collectors.toSet()), // Converte categorias para strings
                        event.isActive(),
                        event.isOfficial(),
                        event.getImages()
                ))
                .collect(Collectors.toList());
    }

    public FeedDTO getFeedByUserCreatorCourse(int page, int pageSize, boolean isActive, JwtAuthenticationToken authentication) {

        var usuario = authService.getAuthenticatedUser(authentication);

        var userCourse = usuario.getCourse();
        var userCategory = userCourse != null ? userCourse.getCategoria() : null;

        var eventsPage = eventRepository.findAllByActive(isActive, PageRequest.of(page - 1, pageSize, Sort.Direction.DESC, "creationTimeStamp"));

        var feedItems = eventsPage.getContent().stream()
                .filter(event -> {
                    var creatorCourse = event.getCreatorUser().getCourse();
                    return creatorCourse != null && creatorCourse.getCategoria().equals(userCategory);
                })
                .map(event -> new FeedItemDTO(
                        event.getEventId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDateTime(),
                        event.getLocation(),
                        event.getCategorias().stream().map(Category::getDescricao).collect(Collectors.toSet()), // Converte categorias para strings
                        event.isActive(),
                        event.isOfficial(),
                        event.getImages()
                ))
                .collect(Collectors.toList());

        return new FeedDTO(feedItems, page, pageSize, eventsPage.getTotalPages(), eventsPage.getTotalElements());
    }

}