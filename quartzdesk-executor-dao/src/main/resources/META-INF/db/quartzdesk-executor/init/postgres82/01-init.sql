CREATE SEQUENCE seq_qd_schema_update START WITH 1 INCREMENT BY 1;

CREATE TABLE qd_schema_update (
    schema_update_id BIGINT      NOT NULL DEFAULT nextval( 'seq_qd_schema_update' )
  , major            INTEGER     NOT NULL
  , minor            INTEGER     NOT NULL
  , maintenance      INTEGER     NOT NULL
  , applied_at       TIMESTAMPTZ NOT NULL
);

ALTER TABLE qd_schema_update ADD CONSTRAINT pk_qd_schema_update PRIMARY KEY (
  schema_update_id
);
