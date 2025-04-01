-- Criação da tabela de roles
CREATE TABLE IF NOT EXISTS tb_roles (
                                        role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        name VARCHAR(255) NOT NULL UNIQUE
    );

-- Inserção de dados iniciais na tabela de roles
INSERT INTO tb_roles (role_id, name) VALUES (1, 'admin')
    ON DUPLICATE KEY UPDATE name = VALUES(name);
INSERT INTO tb_roles (role_id, name) VALUES (2, 'organizador')
    ON DUPLICATE KEY UPDATE name = VALUES(name);
INSERT INTO tb_roles (role_id, name) VALUES (3, 'aluno')
    ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Criação da tabela de categorias
CREATE TABLE IF NOT EXISTS tb_categories (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                             descricao VARCHAR(255) NOT NULL UNIQUE
    );

-- Inserção de dados iniciais na tabela de categorias
INSERT INTO tb_categories (id, descricao) VALUES (1, 'Tecnologia')
    ON DUPLICATE KEY UPDATE descricao = VALUES(descricao);
INSERT INTO tb_categories (id, descricao) VALUES (2, 'Saúde')
    ON DUPLICATE KEY UPDATE descricao = VALUES(descricao);
INSERT INTO tb_categories (id, descricao) VALUES (3, 'Engenharia')
    ON DUPLICATE KEY UPDATE descricao = VALUES(descricao);
INSERT INTO tb_categories (id, descricao) VALUES (4, 'Humanas')
    ON DUPLICATE KEY UPDATE descricao = VALUES(descricao);
INSERT INTO tb_categories (id, descricao) VALUES (5, 'Exatas')
    ON DUPLICATE KEY UPDATE descricao = VALUES(descricao);
INSERT INTO tb_categories (id, descricao) VALUES (6, 'Evento Não Oficial')
    ON DUPLICATE KEY UPDATE descricao = VALUES(descricao);
INSERT INTO tb_categories (id, descricao) VALUES (7, 'Evento Oficial')
    ON DUPLICATE KEY UPDATE descricao = VALUES(descricao);