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

    public FeedDTO getFeed(int page, int pageSize) {
        var events = eventRepository.findAll(PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimeStamp"))
                .map(event -> new FeedItemDTO(
                        event.getEventId()
                    //  Adicionar atributos a serem exibidos no frontend
                ));
        return new FeedDTO(events.getContent(), page, pageSize, events.getTotalPages(), events.getTotalElements());
    }

}
