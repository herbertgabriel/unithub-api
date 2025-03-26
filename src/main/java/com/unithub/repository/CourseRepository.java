package com.unithub.repository;

import com.unithub.model.Categorys;
import com.unithub.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByNome(String nome);
    List<Course> findAllByCategoria(Categorys categoria);

}