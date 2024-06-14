CREATE TABLE qd_schema_update (
    schema_update_id BIGINT AUTO_INCREMENT NOT NULL,
    major            INTEGER   NOT NULL,
    minor            INTEGER   NOT NULL,
    maintenance      INTEGER   NOT NULL,
    applied_at       TIMESTAMP NOT NULL
);

ALTER TABLE qd_schema_update ADD PRIMARY KEY (
    schema_update_id
);

COMMIT;