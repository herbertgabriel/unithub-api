package com.unithub.repository;

import com.unithub.model.Event;
import com.unithub.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findAllByActive(boolean active, Pageable pageable);
    List<Event> findByCreatorUserOrderByCreationTimeStampDesc(User user);
    List<Event> findByCreatorUser(User creatorUser);
    List<Event> findAllByEnrolledUserListContains(User user);

}