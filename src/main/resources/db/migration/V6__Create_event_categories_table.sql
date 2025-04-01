CREATE TABLE tb_event_categories (
                                     event_id UUID NOT NULL,
                                     category VARCHAR(255) NOT NULL,
                                     PRIMARY KEY (event_id, category),
                                     FOREIGN KEY (event_id) REFERENCES tb_events(event_id) ON DELETE CASCADE
);