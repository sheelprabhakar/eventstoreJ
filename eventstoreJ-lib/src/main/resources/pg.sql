CREATE TABLE IF NOT EXISTS ${namespace}_events (
  position bigserial NOT NULL,
  aggregateId uuid NOT NULL,
  revision integer NOT NULL,
  event jsonb NOT NULL,
  hasBeenPublished boolean NOT NULL,
  CONSTRAINT ${namespace}_events_pk PRIMARY KEY(position),
  CONSTRAINT ${namespace}_aggregateId_revision UNIQUE (aggregateId, revision)
);


CREATE TABLE IF NOT EXISTS ${namespace}_snapshots (
  aggregateId uuid NOT NULL,
  revision integer NOT NULL,
  state jsonb NOT NULL,
  CONSTRAINT ${namespace}_snapshots_pk PRIMARY KEY(aggregateId, revision)
);