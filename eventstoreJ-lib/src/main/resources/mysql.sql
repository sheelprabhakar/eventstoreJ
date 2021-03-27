
CREATE TABLE IF NOT EXISTS ${namespace}_snapshots (
  aggregateId CHAR(36) NOT NULL,
  revision INT NOT NULL,
  state VARCHAR(2048) NOT NULL,
  CONSTRAINT ${namespace}_snapshots_pk PRIMARY KEY(aggregateId, revision)
);

CREATE TABLE IF NOT EXISTS ${namespace}_events (
      position BIGINT NOT NULL AUTO_INCREMENT,
      aggregateId CHAR(36) NOT NULL,
      revision INT NOT NULL,
      event VARCHAR(2048) NOT NULL,
      published TINYINT NOT NULL,
  CONSTRAINT ${namespace}_events_pk PRIMARY KEY(position),
  CONSTRAINT ${namespace}_aggregateId_revision UNIQUE (aggregateId, revision)
);

