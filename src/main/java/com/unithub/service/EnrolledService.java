package com.unithub.service;

import com.unithub.dto.eventsDTOs.EnrollDTOs.InscricaoDTO;
import com.unithub.dto.eventsDTOs.EnrollDTOs.InscricaoResponseDTO;
import com.unithub.model.Enrolled;
import com.unithub.repository.EnrolledRepository;
import com.unithub.repository.EventRepository;
import com.unithub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrolledService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EnrolledRepository enrolledRepository;

    public EnrolledService(EventRepository eventRepository, UserRepository userRepository, EnrolledRepository enrolledRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.enrolledRepository = enrolledRepository;
    }

    @Transactional
    public InscricaoResponseDTO subscribeEvent(InscricaoDTO inscricaoDTO) {
        var usuario = userRepository.getReferenceById(inscricaoDTO.userId());
        var evento = eventRepository.getReferenceById(inscricaoDTO.eventId());

        var dateEvent = evento.getDateTime();

        var inscricao = new Enrolled(null, usuario, evento, false);
        enrolledRepository.save(inscricao);

        return new InscricaoResponseDTO(dateEvent);
    }
}