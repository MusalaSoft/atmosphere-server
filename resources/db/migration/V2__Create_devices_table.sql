CREATE TABLE devices (
    id BIGINT AUTO_INCREMENT,
    serial_number VARCHAR(255),
    is_emulator TINYINT(1) DEFAULT false,
    is_tablet TINYINT(1) DEFAULT false,
    resolution_height INTEGER,
    resolution_width INTEGER,
    os VARCHAR(255),
    model VARCHAR(255),
    dpi INTEGER,
    ram INTEGER,
    cpu VARCHAR(255),
    api_level INTEGER,
    manifacturer VARCHAR(255),
    has_camera TINYINT(1),
    is_allocated TINYINT(1) DEFAULT false,
    agent_id BIGINT NOT NULL,
    rmi_registry_id VARCHAR(255) UNIQUE,
    PRIMARY KEY (id),
    FOREIGN KEY (agent_id) REFERENCES agents (id)
    );

CREATE INDEX serial_number_idx ON devices (serial_number)