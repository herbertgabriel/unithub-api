CREATE TABLE tb_users_roles (
                                user_id UUID NOT NULL,
                                role_id BIGINT NOT NULL,
                                PRIMARY KEY (user_id, role_id),
                                FOREIGN KEY (user_id) REFERENCES tb_user(user_id) ON DELETE CASCADE,
                                FOREIGN KEY (role_id) REFERENCES tb_roles(role_id) ON DELETE CASCADE
);