package com.unithub.service;

import com.unithub.dto.request.event.CadastrarEventoDTO;
import com.unithub.dto.respose.event.EventDetailsDTO;
import com.unithub.exception.EventNotFoundException;
import com.unithub.exception.ImageUploadException;
import com.unithub.model.Event;
import com.unithub.model.Role;
import com.unithub.model.User;
import com.unithub.repository.EventRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCadastrarEvento_Success() throws ImageUploadException {
        // Arrange
        User usuario = mock(User.class);
        Role roleOrganizador = new Role();
        roleOrganizador.setName(Role.Values.ORGANIZADOR.name());

        when(usuario.getRoles()).thenReturn(Set.of(roleOrganizador));

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);
        when(authService.getAuthenticatedUser(authentication)).thenReturn(usuario);

        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);
        CadastrarEventoDTO dados = new CadastrarEventoDTO(
                "Evento Teste",
                "Descrição do evento",
                futureDateTime,
                "Local do evento",
                Set.of(1L, 2L),
                100
        );

        Event event = new Event();
        event.setEventId(UUID.randomUUID());
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        List<MultipartFile> imagens = List.of();

        // Act
        EventDetailsDTO result = eventService.cadastrarEvento(dados, imagens, authentication);

        // Assert
        assertNotNull(result);
        assertEquals("Evento Teste", result.title());
        verify(eventRepository, times(2)).save(any(Event.class)); // Atualizado para refletir o comportamento esperado
    }

    @Test
    void testSubscribeEvent_Success() {
        // Arrange
        UUID eventId = UUID.randomUUID();

        User usuario = mock(User.class);
        Event event = new Event();
        event.setEventId(eventId);
        event.setMaxParticipants(10);
        event.setActive(true);
        event.setEnrolledUserList(new ArrayList<>());

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);
        when(authService.getAuthenticatedUser(authentication)).thenReturn(usuario);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act
        eventService.subscribeEvent(eventId, authentication);

        // Assert
        assertTrue(event.getEnrolledUserList().contains(usuario));
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testSubscribeEvent_EventNotFound() {
        // Arrange
        UUID eventId = UUID.randomUUID();

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EventNotFoundException.class, () -> eventService.subscribeEvent(eventId, authentication));
        verify(eventRepository, times(1)).findById(eventId);
    }
}