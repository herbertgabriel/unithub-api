package com.unithub.service;

import com.unithub.dto.eventsDTOs.CadastrarEventoDTO;
import com.unithub.dto.eventsDTOs.EventDetailsDTO;
import com.unithub.model.Event;
import com.unithub.repository.EventRepository;
import com.unithub.repository.ImageRepository;
import com.unithub.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ImageRepository imageRepository;

    public EventService(UserRepository userRepository, EventRepository eventRepository, ImageRepository imageRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.imageRepository = imageRepository;
    }

    public EventDetailsDTO cadastrarEvento(CadastrarEventoDTO dados) {
        Event event = new Event();
        event.setTitle(dados.title());
        event.setDescription(dados.description());
        event.setDateTime(dados.dateTime());
        event.setLocation(dados.location());
        event.setCategory(dados.category());
        event.setActive(dados.active());
        event.setHasCheckin(dados.hasCheckin());
        event.setHasCertificate(dados.hasCertificate());
        event.setExternalSubscriptionLink(dados.externalSubscriptionLink());
        event.setMaxParticipants(dados.maxParticipants());

        eventRepository.save(event);

        return new EventDetailsDTO(
            event.getEventId(),
            event.getTitle(),
            event.getDescription(),
            event.getDateTime(),
            event.getLocation(),
            event.getCategory(),
            event.isActive(),
            event.isHasCheckin(),
            event.isHasCertificate(),
            event.getExternalSubscriptionLink(),
            event.getMaxParticipants()
        );
    }

}