CREATE TABLE job_run_policy
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(64) NOT NULL,
    enabled    BOOLEAN,
    updated_at BIGINT
);