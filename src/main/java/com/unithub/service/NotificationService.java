package com.unithub.service;

import com.unithub.model.Event;
import com.unithub.repository.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.unithub.dto.request.email.EmailDTO;

@Service
public class NotificationService {
    private final EmailService emailService;
    private final EventRepository eventRepository;
    private final Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    public NotificationService(EmailService emailService, EventRepository eventRepository) {
        this.emailService = emailService;
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedRate = 3600000)
    public void processEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findEventsWithinNext24Hours(now, now.plusHours(24));
        events.forEach(this::checkAndQueueEvent);
        notifyUsersForQueuedEvents();
    }

    public void checkAndQueueEvent(Event event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventDate = event.getDateTime();

        if (!eventQueue.contains(event) && eventDate.isAfter(now) && eventDate.isBefore(now.plusHours(24))) {
            eventQueue.add(event);
        }
    }

    public void notifyUsersForQueuedEvents() {
        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();
            event.getEnrolledUserList().forEach(user -> {
                try {
                    String formattedDate = event.getDateTime().format(dateFormatter);
                    EmailDTO emailDTO = new EmailDTO(
                            user.getEmail(),
                            "Lembrete: Evento " + event.getTitle(),
                            "Olá " + user.getName() + ",\n\n" +
                                    "Este é um lembrete para o evento \"" + event.getTitle() + "\".\n" +
                                    "Data e hora: " + formattedDate + "\n" +
                                    "Local: " + event.getLocation() + "\n\n" +
                                    "Atenciosamente,\nEquipe UnitHub"
                    );
                    emailService.sendEmail(emailDTO);
                } catch (Exception e) {
                    System.err.println("Falha ao enviar e-mail para " + user.getEmail());
                }
            });
        }
    }
}