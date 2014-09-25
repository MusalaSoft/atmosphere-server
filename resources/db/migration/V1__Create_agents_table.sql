CREATE TABLE agents (
    id BIGINT AUTO_INCREMENT,
    agent_id VARCHAR(255) UNIQUE,
    rmi_registry_id VARCHAR(255) UNIQUE,
    PRIMARY KEY (id)
    );

CREATE INDEX agent_id_idx ON agents (agent_id)