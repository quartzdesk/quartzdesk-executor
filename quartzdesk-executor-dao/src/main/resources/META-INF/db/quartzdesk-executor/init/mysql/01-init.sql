CREATE TABLE qd_schema_update (
    schema_update_id BIGINT  NOT NULL AUTO_INCREMENT UNIQUE,
    major            INTEGER NOT NULL,
    minor            INTEGER NOT NULL,
    maintenance      INTEGER NOT NULL,
    applied_at       DATETIME (3) NOT NULL
);

ALTER TABLE qd_schema_update ADD CONSTRAINT pk_qd_schema_update PRIMARY KEY (
    schema_update_id
);

COMMIT;
