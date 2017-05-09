CREATE TABLE agents (
    id BIGINT AUTO_INCREMENT,
    agent_id VARCHAR(255) UNIQUE NOT NULL,
    PRIMARY KEY (id)
    );

CREATE INDEX agent_id_idx ON agents (agent_id)