package com.unithub.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_course")
@Data
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "course_id")
    private long cursoId;

    @Column(nullable = false, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category categoria; // Agora usa a enumeração separada
}