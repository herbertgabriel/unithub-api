CREATE TABLE tb_events (
                           event_id UUID PRIMARY KEY,
                           creation_time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           user_id UUID NOT NULL,
                           title VARCHAR(255) NOT NULL,
                           description TEXT NOT NULL,
                           date_time TIMESTAMP NOT NULL,
                           location VARCHAR(255),
                           active BOOLEAN DEFAULT FALSE,
                           max_participants INT DEFAULT 0,
                           is_official BOOLEAN DEFAULT FALSE,
                           FOREIGN KEY (user_id) REFERENCES tb_user(user_id) ON DELETE CASCADE
);