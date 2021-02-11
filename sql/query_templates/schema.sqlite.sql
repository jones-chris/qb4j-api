CREATE TABLE query_templates(
   id INTEGER PRIMARY KEY AUTOINCREMENT,

   name TEXT NOT NULL,
   version INTEGER NOT NULL,

   query_json TEXT NOT NULL,

   discoverable BOOLEAN NOT NULL CHECK(discoverable in (0, 1)) DEFAULT 0,

   created_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   created_by TEXT NOT NULL,
   last_updated_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   last_updated_by TEXT NOT NULL,

   UNIQUE (name, version)
);

-- todo:  Add this in the future to tell if a query template is unused or build a dependency tree?
--CREATE TABLE query_template_dependency(
--    query_id INTEGER NOT NULL,
--    dependency_id INTEGER NOT NULL,
--
--    created_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--   created_by TEXT NOT NULL,
--   last_updated_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--   last_updated_by TEXT NOT NULL,
--
--    FOREIGN KEY (query_id) REFERENCES query_templates(id),
--    FOREIGN KEY (dependency_id) REFERENCES query_templates(id),
--    UNIQUE (query_id, dependency_id)
--);