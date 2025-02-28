package com.unithub.service;

import com.unithub.dto.eventsDTOs.CadastrarEventoDTO;
import com.unithub.dto.eventsDTOs.InscricaoDTOs.InscricaoDTO;
import com.unithub.dto.eventsDTOs.InscricaoDTOs.InscricaoResponseDTO;
import com.unithub.dto.eventsDTOs.EventDetailsDTO;
import com.unithub.model.Event;
import com.unithub.model.Role;
import com.unithub.repository.EventRepository;
import com.unithub.repository.ImageRepository;
import com.unithub.repository.UserRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    // Funcionalidade de cadastro de eventos
    // Falta implementar as imagens do S3
    public EventDetailsDTO cadastrarEvento(CadastrarEventoDTO dados) {
        var usuario = userRepository.findById(UUID.fromString(dados.authentication().getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = new Event();

        if(usuario.getRoles().stream().noneMatch(role -> role.getName().equals("organizador"))){
            event.setActive(true);
        } else{
            event.setActive(false);
        }

        if(dados.maxParticipants() <= 0){
            event.setMaxParticipants(0);
        } else{
            event.setMaxParticipants(dados.maxParticipants());
        }

        event.setTitle(dados.title());
        event.setDescription(dados.description());
        event.setCategory(dados.category());
        event.setLocation(dados.location());
        event.setDateTime(dados.dateTime());
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
            event.getExternalSubscriptionLink(),
            event.getMaxParticipants()
        );
    }

    public void deletarEvento(UUID eventId, JwtAuthenticationToken authentication) {
        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));
        var post = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        var isOrganizador = usuario.getRoles().stream().anyMatch((role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name())));

        if(isOrganizador || post.getUser().getUserId().equals(UUID.fromString(authentication.getName()))){
            eventRepository.delete(post);
        }
    }

    // Funcionalidade de Aceitar e Recusar eventos criados por alunos
    // Necessario Integrar API com e-mail informando motivo do recuso
    public void aceitarEvento(UUID eventId, JwtAuthenticationToken authentication) {
        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        var post = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        var isOrganizador = usuario.getRoles().stream().anyMatch((role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name())));

        if(isOrganizador){
            throw new RuntimeException("You don't have permission");
        }
        post.setActive(true);
        eventRepository.save(post);
    }
    public void recusarEvento(UUID eventId, JwtAuthenticationToken authentication, String motivo) {
        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        var post = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        var isOrganizador = usuario.getRoles().stream().anyMatch((role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name())));

        if(isOrganizador){
            throw new RuntimeException("You don't have permission");
        }
        eventRepository.delete(post);
    }

    // Funcionalidade de Inscrição em evento e desinscrição
    @Transactional
    public InscricaoResponseDTO subscribeEvent(InscricaoDTO inscricaoDTO) {
        var usuario = userRepository.findById(UUID.fromString(inscricaoDTO.authentication().getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        var evento = eventRepository.findById(inscricaoDTO.eventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (evento.getMaxParticipants() == 0){
            throw new RuntimeException("Isn't possible to subscribe event");
        }

        if (evento.getEnrolledUserList().contains(usuario)) {
            throw new RuntimeException("User is already enrolled in the event");
        }

        if (evento.getNumberOfSubscribers() >= evento.getMaxParticipants()) {
            throw new RuntimeException("Maximum participants exceeded");
        }

        evento.setNumberOfSubscribers(evento.getNumberOfSubscribers() + 1);
        evento.addUser(usuario);
        eventRepository.save(evento);

        return new InscricaoResponseDTO(evento.getDateTime());
    }

    @Transactional
    public void unsubscribeEvent(InscricaoDTO inscricaoDTO) {
        var usuario = userRepository.findById(UUID.fromString(inscricaoDTO.authentication().getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        var evento = eventRepository.findById(inscricaoDTO.eventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (evento.getMaxParticipants() == 0){
            throw new RuntimeException("Isn't possible to unsubscribe event");
        }

        if (!evento.getEnrolledUserList().contains(usuario)) {
            throw new RuntimeException("User isn't enrolled in the event");
        }

        evento.setNumberOfSubscribers(evento.getNumberOfSubscribers() - 1);
        evento.removeUser(usuario);
        eventRepository.save(evento);
    }
}