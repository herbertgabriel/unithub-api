package com.unithub.service;

import com.unithub.repository.EnrolledRepository;
import com.unithub.repository.EventRepository;
import com.unithub.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CheckinService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EnrolledRepository enrolledRepository;

    public CheckinService(EventRepository eventRepository, UserRepository userRepository, EnrolledRepository enrolledRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.enrolledRepository = enrolledRepository;
    }


}