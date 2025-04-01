CREATE TABLE tb_user (
                         user_id UUID PRIMARY KEY,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         password VARCHAR(255) NOT NULL,
                         telephone VARCHAR(20),
                         name VARCHAR(255) NOT NULL,
                         curso_id BIGINT,
                         FOREIGN KEY (curso_id) REFERENCES tb_course(course_id) ON DELETE SET NULL
);