package com.unithub.service;

import com.unithub.dto.CadastrarEventoDTO;
import com.unithub.dto.EventDetailsDTO;
import com.unithub.model.Event;
import com.unithub.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

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