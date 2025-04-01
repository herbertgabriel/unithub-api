CREATE TABLE tb_event_users (
                                event_id UUID NOT NULL,
                                user_id UUID NOT NULL,
                                PRIMARY KEY (event_id, user_id),
                                FOREIGN KEY (event_id) REFERENCES tb_events(event_id) ON DELETE CASCADE,
                                FOREIGN KEY (user_id) REFERENCES tb_user(user_id) ON DELETE CASCADE
);