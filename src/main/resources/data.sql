CREATE TABLE IF NOT EXISTS tb_roles (role_id INT PRIMARY KEY, name VARCHAR(255) NOT NULL);
INSERT IGNORE INTO tb_roles (role_id, name) VALUES (1, 'admin');
INSERT IGNORE INTO tb_roles (role_id, name) VALUES (2, 'organizador');
INSERT IGNORE INTO tb_roles (role_id, name) VALUES (3, 'aluno');

-- Criação da tabela de categorias
CREATE TABLE IF NOT EXISTS tb_categories (
                                             id BIGINT PRIMARY KEY,
                                             descricao VARCHAR(255) NOT NULL
);

-- Inserção das categorias
INSERT INTO tb_categories (id, descricao) VALUES
                                              (1, 'Tecnologia'),
                                              (2, 'Saúde'),
                                              (3, 'Engenharia'),
                                              (4, 'Humanas'),
                                              (5, 'Exatas'),
                                              (6, 'Evento Não Oficial'),
                                              (7, 'Evento Oficial');