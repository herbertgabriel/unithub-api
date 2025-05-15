package com.unithub.service;

import com.unithub.dto.request.email.EmailDTO;
import com.unithub.model.Event;
import com.unithub.model.User;
import com.unithub.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

@Test
void testProcessEvents_Success() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();
    Event event1 = new Event();
    event1.setEventId(UUID.randomUUID());
    event1.setTitle("Evento 1");
    event1.setDateTime(now.plusHours(12));
    event1.setLocation("Local 1");
    User user1 = new User();
    user1.setEmail("herbertgac@gmail.com");
    user1.setName("User 1");
    event1.setEnrolledUserList(List.of(user1));

    Event event2 = new Event();
    event2.setEventId(UUID.randomUUID());
    event2.setTitle("Evento 2");
    event2.setDateTime(now.plusHours(20));
    event2.setLocation("Local 2");
    User user2 = new User();
    user2.setEmail("herbertgacruz@gmail.com");
    user2.setName("User 2");
    event2.setEnrolledUserList(List.of(user2));

    when(eventRepository.findEventsWithinNext24Hours(any(), any()))
            .thenReturn(List.of(event1, event2));

    // Mock do envio de e-mail
    doNothing().when(emailService).sendEmail(any(EmailDTO.class));

    // Act
    notificationService.processEvents();

    // Assert
    verify(emailService, times(2)).sendEmail(any(EmailDTO.class)); // Verifica se 2 e-mails foram enviados
    verify(eventRepository, times(1)).findEventsWithinNext24Hours(any(), any());
}

@Test
void testCheckAndQueueEvent_Success() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();
    Event event = new Event();
    event.setEventId(UUID.randomUUID());
    event.setTitle("Evento Teste");
    event.setDateTime(now.plusHours(10));

    // Act
    notificationService.checkAndQueueEvent(event);

    // Assert
    // Não há exceções e o evento é adicionado à fila
    // Não é possível verificar diretamente a fila, mas o teste garante que o método não falha
}

@Test
void testNotifyUsersForQueuedEvents_Success() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();
    Event event = new Event();
    event.setEventId(UUID.randomUUID());
    event.setTitle("Evento Teste");
    event.setDateTime(now.plusHours(10));
    event.setLocation("Local Teste");
    User user = new User();
    user.setEmail("herbertgacruz@gmail.com");
    user.setName("User Teste");
    event.setEnrolledUserList(List.of(user));

    notificationService.checkAndQueueEvent(event);

    // Mock do envio de e-mail
    doNothing().when(emailService).sendEmail(any(EmailDTO.class));

    // Act
    notificationService.notifyUsersForQueuedEvents();

    // Assert
    verify(emailService, times(1)).sendEmail(any(EmailDTO.class)); // Verifica se 1 e-mail foi enviado
}


}