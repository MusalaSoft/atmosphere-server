CREATE TABLE devices (
    id BIGINT AUTO_INCREMENT,
    serial_number VARCHAR(255) NOT NULL,
    is_emulator TINYINT(1) NOT NULL DEFAULT false,
    is_tablet TINYINT(1),
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
    is_allocated TINYINT(1) NOT NULL DEFAULT false,
    agent_id BIGINT NOT NULL,
    device_id VARCHAR(255) UNIQUE NOT NULL,
    passkey BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (agent_id) REFERENCES agents (id)
    );

CREATE INDEX serial_number_idx ON devices (serial_number)