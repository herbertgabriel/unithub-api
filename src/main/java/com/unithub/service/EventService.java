package com.unithub.service;

import com.unithub.dto.EmailDTO;
import com.unithub.dto.eventsDTOs.AtualizarEventoDTO;
import com.unithub.dto.eventsDTOs.CadastrarEventoDTO;
import com.unithub.dto.eventsDTOs.Inscricao.InscricaoResponseDTO;
import com.unithub.dto.eventsDTOs.EventDetailsDTO;
import com.unithub.dto.eventsDTOs.Inscricao.InscricoesListDTO;
import com.unithub.dto.eventsDTOs.RecusarEventoDTO;
import com.unithub.model.Categorys;
import com.unithub.model.Event;
import com.unithub.model.Role;
import com.unithub.repository.EventRepository;
import com.unithub.repository.ImageRepository;
import com.unithub.repository.UserRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public EventDetailsDTO cadastrarEvento(CadastrarEventoDTO dados, JwtAuthenticationToken authentication) {
        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = new Event();

        event.setCreatorUser(usuario);

        boolean isOrganizador = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        event.setActive(isOrganizador);

        if (dados.maxParticipants() <= 0) {
            event.setMaxParticipants(0);
        } else {
            event.setMaxParticipants(dados.maxParticipants());
        }

        Set<Categorys> categorias = new HashSet<>();
        for (Long categoriaId : dados.categoriaIds()) {
            categorias.add(Categorys.fromId(categoriaId));
        }

        event.setTitle(dados.title());
        event.setDescription(dados.description());
        event.setCategorias(categorias);
        event.setLocation(dados.location());
        event.setDateTime(dados.dateTime());

        eventRepository.save(event);

        Set<String> categoriasAsStrings = categorias.stream()
                .map(Categorys::getDescricao)
                .collect(Collectors.toSet());

        return new EventDetailsDTO(
                event.getEventId(),
                event.getTitle(),
                event.getDescription(),
                event.getDateTime(),
                event.getLocation(),
                categoriasAsStrings,
                event.isActive(),
                event.getMaxParticipants()
        );
    }

    public EventDetailsDTO atualizarEvento(UUID eventId, AtualizarEventoDTO dados, JwtAuthenticationToken authentication) {
        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (event.isActive() && !isOrganizadorOrAdmin) {
            throw new RuntimeException("You don't have permission to update this active event");
        }

        if (dados.title() != null) {
            event.setTitle(dados.title());
        }
        if (dados.description() != null) {
            event.setDescription(dados.description());
        }
        if (dados.location() != null) {
            event.setLocation(dados.location());
        }
        if (dados.dateTime() != null) {
            event.setDateTime(dados.dateTime());
        }
        if (dados.maxParticipants() != null) {
            event.setMaxParticipants(dados.maxParticipants());
        }
        if (dados.categoriaIds() != null) {
            Set<Categorys> novasCategorias = new HashSet<>();
            for (Long categoriaId : dados.categoriaIds()) {
                novasCategorias.add(Categorys.fromId(categoriaId));
            }
            event.getCategorias().clear(); // Remove as categorias antigas
            event.getCategorias().addAll(novasCategorias); // Adiciona as novas categorias
        }

        eventRepository.save(event);

        Set<String> categoriasAsStrings = event.getCategorias().stream()
                .map(Categorys::getDescricao)
                .collect(Collectors.toSet());

        return new EventDetailsDTO(
                event.getEventId(),
                event.getTitle(),
                event.getDescription(),
                event.getDateTime(),
                event.getLocation(),
                categoriasAsStrings,
                event.isActive(),
                event.getMaxParticipants()
        );
    }

    public void deletarEvento(UUID eventId, JwtAuthenticationToken authentication) {
        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));
        var post = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if(isOrganizadorOrAdmin || post.getCreatorUser().getUserId().equals(UUID.fromString(authentication.getName()))){
            eventRepository.delete(post);
        } else {
            throw new RuntimeException("You don't have permission to delete this event");
        }
    }

    // Funcionalidade de Aceitar e Recusar eventos criados por alunos
    // Necessario Integrar API com e-mail informando motivo do recuso
    public void aceitarEvento(UUID eventId, JwtAuthenticationToken authentication) {
        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        var post = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if(!isOrganizadorOrAdmin){
            throw new RuntimeException("You don't have permission");
        }

        var email = post.getCreatorUser().getEmail();
        EmailDTO emailDTO = new EmailDTO(email, "UnitHub - Seu evento aprovado!", "Parabéns seu evento foi aprovado com sucesso! ");
        emailService.sendEmail(emailDTO);

        post.setActive(true);
        eventRepository.save(post);
    }

    public void recusarEvento(UUID eventId, RecusarEventoDTO recusar, JwtAuthenticationToken authentication) {
        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        var post = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if(!isOrganizadorOrAdmin){
            throw new RuntimeException("You don't have permission");
        }

        var email = post.getCreatorUser().getEmail();
        EmailDTO emailDTO = new EmailDTO(email, "UnitHub - Seu evento foi recusado", recusar.motivo());

        emailService.sendEmail(emailDTO);

        eventRepository.delete(post);
    }

    // Funcionalidade de Inscrição em evento e desinscrição
    public InscricaoResponseDTO subscribeEvent(UUID eventId, JwtAuthenticationToken authentication) {
        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        var evento = eventRepository.findById(eventId)
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

    public void unsubscribeEvent(UUID eventId, JwtAuthenticationToken authentication) {
        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        var evento = eventRepository.findById(eventId)
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