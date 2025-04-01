CREATE TABLE tb_course (
                           course_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           nome VARCHAR(255) NOT NULL UNIQUE,
                           categoria VARCHAR(255) NOT NULL
);