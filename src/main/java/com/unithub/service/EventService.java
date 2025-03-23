package com.unithub.service;

import com.unithub.dto.EmailDTO;
import com.unithub.dto.eventsDTOs.CadastrarEventoDTO;
import com.unithub.dto.eventsDTOs.Inscricao.InscricaoDTO;
import com.unithub.dto.eventsDTOs.Inscricao.InscricaoResponseDTO;
import com.unithub.dto.eventsDTOs.EventDetailsDTO;
import com.unithub.dto.eventsDTOs.Inscricao.InscricoesListDTO;
import com.unithub.model.Event;
import com.unithub.model.Role;
import com.unithub.repository.EventRepository;
import com.unithub.repository.ImageRepository;
import com.unithub.repository.UserRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ImageRepository imageRepository;
    private final EmailService emailService;

    public EventService(UserRepository userRepository, EventRepository eventRepository, ImageRepository imageRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.imageRepository = imageRepository;
        this.emailService = emailService;
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

        if(isOrganizador || post.getCreatorUser().getUserId().equals(UUID.fromString(authentication.getName()))){
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
        var email = post.getCreatorUser().getEmail();
        EmailDTO emailDTO = new EmailDTO(email, "UnitHub - Seu evento aprovado!", "Parabéns seu evento foi aprovado com sucesso! ");
        emailService.sendEmail(emailDTO);

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

        var email = post.getCreatorUser().getEmail();
        EmailDTO emailDTO = new EmailDTO(email, "UnitHub - Seu evento foi recusado", motivo);

        emailService.sendEmail(emailDTO);

        eventRepository.delete(post);
    }

    // Funcionalidade de Inscrição em evento e desinscrição
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

        if (evento.getEnrolledUserList().size() >= evento.getMaxParticipants()) {
            throw new RuntimeException("Maximum participants exceeded");
        }

        evento.addUser(usuario);
        eventRepository.save(evento);

        return new InscricaoResponseDTO(evento.getDateTime());
    }

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

        evento.removeUser(usuario);
        eventRepository.save(evento);
    }

    public List<InscricoesListDTO> getSubscribers(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<InscricoesListDTO> subscribers = event.getEnrolledUserList().stream()
                .map(user -> new InscricoesListDTO(user.getUserId(), user.getName(), user.getTelephone(), user.getEmail()))
                .collect(Collectors.toList());

        if (subscribers.isEmpty()) {
            throw new RuntimeException("No subscribers found for this event");
        }

        return subscribers;
    }

}