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
    private Categorys categoria;

    public enum Categorys {
        TECNOLOGIA(1L, "Tecnologia"),
        SAUDE(2L, "Saúde"),
        ENGENHARIA(3L, "Engenharia"),
        HUMANAS(4L, "Humanas"),
        EXATAS(5L, "Exatas"),
        NAO_OFICIAL(6L, "Evento Não Oficial"),
        OFICIAL(7L, "Evento Oficial");

        private final long id;
        private final String descricao;

        Categorys(long id, String descricao) {
            this.id = id;
            this.descricao = descricao;
        }

        public long getId() {
            return id;
        }

        public String getDescricao() {
            return descricao;
        }

        public static Categorys fromId(long id) {
            for (Categorys categoria : values()) {
                if (categoria.getId() == id) {
                    return categoria;
                }
            }
            throw new IllegalArgumentException("Categoria inválida para o ID: " + id);
        }
    }
}