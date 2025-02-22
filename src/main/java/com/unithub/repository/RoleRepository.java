package com.unithub.repository;

import com.unithub.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}