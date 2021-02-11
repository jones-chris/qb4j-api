CREATE TABLE query_templates(
   id INT PRIMARY KEY AUTO_INCREMENT,

   name VARCHAR(50) NOT NULL,
   version INT NOT NULL,

   query_json JSON NOT NULL,

   discoverable BOOLEAN NOT NULL DEFAULT FALSE,

   created_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
   created_by VARCHAR(50) NOT NULL,
   last_updated_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
   last_updated_by VARCHAR(50) NOT NULL,

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