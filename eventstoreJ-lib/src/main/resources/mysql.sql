CREATE TABLE IF NOT EXISTS ${namespace}_events (
      position BIGINT NOT NULL AUTO_INCREMENT,
      aggregateId CHAR(36) NOT NULL,
      revision INT NOT NULL,
      event JSON NOT NULL,
      published TINYINT NOT NULL,
  CONSTRAINT ${namespace}_events_pk PRIMARY KEY(position),
  CONSTRAINT ${namespace}_aggregateId_revision UNIQUE (aggregateId, revision)
);


CREATE TABLE IF NOT EXISTS ${namespace}_snapshots (
  aggregateId uuid NOT NULL,
  revision integer NOT NULL,
  state jsonb NOT NULL,
  CONSTRAINT ${namespace}_snapshots_pk PRIMARY KEY(aggregateId, revision)
);