package com.unithub.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "tb_roles")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private long id;
    private String name;

    public enum Values {
        ADMIN(1L),
        ORGANIZADOR(2L),
        ALUNO(3L);

        long roleId;
        Values(long roleId) {
            this.roleId = roleId;
        }

        public long getRoleId() {
            return roleId;
        }
    }
}