CREATE SCHEMA qb4j;

CREATE TABLE qb4j.query_templates(
   id SERIAL PRIMARY KEY,

   name VARCHAR(50) NOT NULL,
   version INTEGER NOT NULL,

   query_json TEXT NOT NULL,  -- todo:  consider changing this to JSON or JSONB.  Initial dev work for this didn't work :(.

   discoverable BOOLEAN NOT NULL DEFAULT FALSE,

   -- todo:  Consider adding these back as helpful performance data for DBAs and users considering using a query.
--   number_of_executions INTEGER NOT NULL DEFAULT 0,
--   avg_execution_time DECIMAL NOT NULL DEFAULT 0,

   -- todo:  Consider adding this field to hold the first 5-10 rows of the query to display in the UI as sample data.
   -- todo:  Consider changing from TEXT to JSON or JSONB.
--   sample_data TEXT

   created_ts TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
   created_by VARCHAR(50) NOT NULL,
   last_updated_ts TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
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