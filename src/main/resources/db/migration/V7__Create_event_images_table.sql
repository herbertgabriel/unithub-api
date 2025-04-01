CREATE TABLE tb_event_images (
                                 event_id UUID NOT NULL,
                                 image_url TEXT NOT NULL,
                                 PRIMARY KEY (event_id, image_url),
                                 FOREIGN KEY (event_id) REFERENCES tb_events(event_id) ON DELETE CASCADE
);