package com.unithub.repository;

import com.unithub.model.Enrolled;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrolledRepository extends JpaRepository<Enrolled, Long> {
}
