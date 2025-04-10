package com.unithub.service;

import com.unithub.exception.EventNotFoundException;
import com.unithub.exception.ImageUploadException;
import com.unithub.dto.EmailDTO;
import com.unithub.dto.eventsDTOs.AtualizarEventoDTO;
import com.unithub.dto.eventsDTOs.CadastrarEventoDTO;
import com.unithub.dto.eventsDTOs.Inscricao.InscricaoResponseDTO;
import com.unithub.dto.eventsDTOs.EventDetailsDTO;
import com.unithub.dto.eventsDTOs.Inscricao.InscricoesListDTO;
import com.unithub.dto.eventsDTOs.RecusarEventoDTO;
import com.unithub.model.Category;
import com.unithub.model.Event;
import com.unithub.model.Role;
import com.unithub.repository.EventRepository;
import com.unithub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;
    private final ImageService imageService;
    private final AuthService authService;


    public EventService(UserRepository userRepository, EventRepository eventRepository, EmailService emailService, ImageService imageService, AuthService authService) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.emailService = emailService;
        this.imageService = imageService;
        this.authService = authService;
    }

    // Funcionalidade de cadastro de eventos
    @Transactional
    public EventDetailsDTO cadastrarEvento(CadastrarEventoDTO dados, List<MultipartFile> imagens, JwtAuthenticationToken authentication) throws ImageUploadException {

        var usuario = authService.getAuthenticatedUser(authentication);

        Event event = new Event();

        event.setCreatorUser(usuario);

        boolean isOrganizador = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        event.setActive(isOrganizador);
        event.setOfficial(isOrganizador);

        if (dados.maxParticipants() <= 0) {
            event.setMaxParticipants(0);
        } else {
            event.setMaxParticipants(dados.maxParticipants());
        }

        Set<Category> categorias = new HashSet<>();
        for (Long categoriaId : dados.categoriaIds()) {
            categorias.add(Category.fromId(categoriaId));
        }

        event.setTitle(dados.title());
        event.setDescription(dados.description());
        event.setCategorias(categorias);
        event.setLocation(dados.location());
        event.setDateTime(dados.dateTime());

        eventRepository.save(event);

        if (imagens != null && !imagens.isEmpty()) {
            if (imagens.size() > 4) {
                throw new ImageUploadException("O evento pode ter no máximo 4 imagens.");
            }

            for (MultipartFile imagem : imagens) {
                String imageUrl = imageService.uploadImage(imagem, event);
                event.getImages().add(imageUrl); // Adiciona a URL da imagem ao evento
            }
        }

        eventRepository.save(event);  // Salvar novamente para atualizar as URLs das imagens

        Set<String> categoriasAsStrings = categorias.stream()
                .map(Category::getDescricao)
                .collect(Collectors.toSet());

        return new EventDetailsDTO(
                event.getEventId(),
                event.getTitle(),
                event.getDescription(),
                event.getDateTime(),
                event.getLocation(),
                categoriasAsStrings,
                event.isActive(),
                event.getMaxParticipants(),
                event.getImages()
        );
    }

    @Transactional
    public EventDetailsDTO atualizarEvento(UUID eventId, AtualizarEventoDTO dados, JwtAuthenticationToken authentication) {
        var usuario = authService.getAuthenticatedUser(authentication);

        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (event.isActive() && !isOrganizadorOrAdmin) {
            throw new AccessDeniedException("You don't have permission to update this active event");
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
            Set<Category> novasCategorias = new HashSet<>();
            for (Long categoriaId : dados.categoriaIds()) {
                novasCategorias.add(Category.fromId(categoriaId));
            }
            event.getCategorias().clear(); // Remove as categorias antigas
            event.getCategorias().addAll(novasCategorias); // Adiciona as novas categorias
        }

        eventRepository.save(event);

        Set<String> categoriasAsStrings = event.getCategorias().stream()
                .map(Category::getDescricao)
                .collect(Collectors.toSet());

        return new EventDetailsDTO(
                event.getEventId(),
                event.getTitle(),
                event.getDescription(),
                event.getDateTime(),
                event.getLocation(),
                categoriasAsStrings,
                event.isActive(),
                event.getMaxParticipants(),
                event.getImages()
        );
    }

    @Transactional
    public void deletarEvento(UUID eventId, JwtAuthenticationToken authentication) {
        var usuario = authService.getAuthenticatedUser(authentication);

        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (isOrganizadorOrAdmin || event.getCreatorUser().getUserId().equals(UUID.fromString(authentication.getName()))) {
            // Remove as imagens do S3
            imageService.deleteAllEventImages(event);

            eventRepository.delete(event);
        } else {
            throw new AccessDeniedException("You don't have permission to delete this event");
        }
    }

    // Funcionalidade de Aceitar e Recusar eventos criados por alunos
    // Necessario Integrar API com e-mail informando motivo do recuso
    @Transactional
    public void aceitarEvento(UUID eventId, JwtAuthenticationToken authentication) {
        var usuario = authService.getAuthenticatedUser(authentication);

        var evento = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
    
        boolean isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));
    
        boolean isAlunoRepresentante = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ALUNO_REPRESENTANTE.name()));
    
        if (isAlunoRepresentante) {
            if (usuario.getCourse() == null || evento.getCreatorUser().getCourse() == null ||
                    !usuario.getCourse().equals(evento.getCreatorUser().getCourse())) {
                throw new AccessDeniedException("You don't have permission to accept events outside your course");
            }
        }
    
        if (!isOrganizadorOrAdmin && !isAlunoRepresentante) {
            throw new AccessDeniedException("You don't have permission to accept this event");
        }
    
        var email = evento.getCreatorUser().getEmail();
        EmailDTO emailDTO = new EmailDTO(email, "UnitHub - Seu evento foi aprovado!", "Parabéns, seu evento foi aprovado com sucesso!");
        emailService.sendEmail(emailDTO);
    
        evento.setActive(true);
        eventRepository.save(evento);
    }

    @Transactional
    public void recusarEvento(UUID eventId, RecusarEventoDTO recusar, JwtAuthenticationToken authentication) {
        var usuario = authService.getAuthenticatedUser(authentication);
    
        var evento = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
    
        boolean isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));
    
        boolean isAlunoRepresentante = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ALUNO_REPRESENTANTE.name()));
    
        if (isAlunoRepresentante) {
            if (usuario.getCourse() == null || evento.getCreatorUser().getCourse() == null ||
                    !usuario.getCourse().equals(evento.getCreatorUser().getCourse())) {
                throw new AccessDeniedException("You don't have permission to reject events outside your course");
            }
        }
    
        if (!isOrganizadorOrAdmin && !isAlunoRepresentante) {
            throw new AccessDeniedException("You don't have permission to reject this event");
        }
    
        var email = evento.getCreatorUser().getEmail();
        EmailDTO emailDTO = new EmailDTO(email, "UnitHub - Seu evento foi recusado", recusar.motivo());
        emailService.sendEmail(emailDTO);
    
        eventRepository.delete(evento);
    }

    // Funcionalidade de Inscrição em evento e desinscrição
    @Transactional
    public InscricaoResponseDTO subscribeEvent(UUID eventId, JwtAuthenticationToken authentication) {
        var usuario = authService.getAuthenticatedUser(authentication);

        var evento = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (evento.getMaxParticipants() == 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Isn't possible to subscribe event");
        }

        if (evento.getEnrolledUserList().contains(usuario)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"User is already enrolled in the event");
        }

        if (evento.getEnrolledUserList().size() >= evento.getMaxParticipants()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Maximum participants exceeded");
        }

        evento.addUser(usuario);
        eventRepository.save(evento);

        return new InscricaoResponseDTO(evento.getDateTime());
    }

    @Transactional
    public void unsubscribeEvent(UUID eventId, JwtAuthenticationToken authentication) {
        var usuario = authService.getAuthenticatedUser(authentication);

        var evento = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (evento.getMaxParticipants() == 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Isn't possible to unsubscribe event");
        }

        if (!evento.getEnrolledUserList().contains(usuario)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User isn't enrolled in the event");
        }

        evento.removeUser(usuario);
        eventRepository.save(evento);
    }

    public List<InscricoesListDTO> getSubscribers(UUID eventId, JwtAuthenticationToken authentication) {
        var usuario = authService.getAuthenticatedUser(authentication);

        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));


        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));
        if (!isOrganizadorOrAdmin) {
            throw new AccessDeniedException("You don't have permission");
        }

        List<InscricoesListDTO> subscribers = event.getEnrolledUserList().stream()
                .map(user -> new InscricoesListDTO(user.getUserId(), user.getName(), user.getTelephone(), user.getEmail()))
                .collect(Collectors.toList());

        if (subscribers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No subscribers found for this event");
        }

        return subscribers;
    }


}