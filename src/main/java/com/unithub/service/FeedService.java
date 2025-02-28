package com.unithub.service;

import com.unithub.dto.eventsDTOs.FeedDTOs.FeedDTO;
import com.unithub.dto.eventsDTOs.FeedDTOs.FeedItemDTO;
import com.unithub.repository.EventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class FeedService {
    private final EventRepository eventRepository;

    public FeedService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
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

    // Falta ver todos posts do proprio usuario
}