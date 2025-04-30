package com.unithub.service;

import com.unithub.model.Event;
import com.unithub.repository.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.unithub.dto.request.email.EmailDTO;

@Service
public class NotificationService {
    private final EmailService emailService;
    private final EventRepository eventRepository;
    private final Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();
    private final Set<UUID> processedEventIds = new HashSet<>(); // Armazena os IDs dos eventos já processados
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    public NotificationService(EmailService emailService, EventRepository eventRepository) {
        this.emailService = emailService;
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedDelay = 86400000) // Executa 24 minutos após o término da execução anterior
    public void processEvents() {
        processedEventIds.clear(); // Limpa os IDs processados a cada execução
        System.out.println("Iniciando o processamento de eventos...");
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findEventsWithinNext24Hours(now, now.plusHours(24));
        events.forEach(this::checkAndQueueEvent);
        notifyUsersForQueuedEvents();
    }

    public void checkAndQueueEvent(Event event) {
        System.out.println("Verificando evento: " + event.getTitle());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventDate = event.getDateTime();

        if (!eventQueue.contains(event) && 
            !processedEventIds.contains(event.getEventId()) && // Verifica se o evento já foi processado
            !eventDate.isBefore(now) && 
            !eventDate.isAfter(now.plusHours(24))) {
            eventQueue.add(event);
        }
    }

    public void notifyUsersForQueuedEvents() {
        System.out.println("Notificando usuários para eventos na fila...");
        if (eventQueue.isEmpty()) {
            System.out.println("Nenhum evento na fila para notificação.");
            return;
        }
        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();
            if (processedEventIds.contains(event.getEventId())) {
                continue; // Ignora eventos já processados
            }
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
            processedEventIds.add(event.getEventId()); // Marca o evento como processado
            System.out.println("Evento " + event.getTitle() + " enviado com sucesso.");

        }
    }
}