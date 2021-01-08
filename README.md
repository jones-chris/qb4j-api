[![Build Status](https://travis-ci.com/jones-chris/qb4j-api.svg?branch=master)](https://travis-ci.com/jones-chris/qb4j-api)

# qb4j-api (QueryBuilder4Java-API)

## About ![Notebook](https://github.githubassets.com/images/icons/emoji/unicode/1f4d3.png)
qb4j-api stands for QueryBuilder4Java-API.

Catchy acronyms for `qb4j` might be "Cube Forge" or "Cubey" if we further abbreviate `qb4j` to `qb`.  Being that this is 
a software project, this project gets marketing points if it's name or logo pays homage to the cube/hexagon...

qb4j-api is the back-end REST API of a larger project that seeks to provide a query builder GUI (graphical user interface)
"web plug-in" to allow users to create, run, and share SQL SELECT queries. The GUI is the [qb4j-ui](https://github.com/jones-chris/qb-react-ui) 
project.

qb4j-api's purpose is to read a target database(s) metadata and data and serve it up through a REST API.  In addition, it
can consume, validate, and run a JSON-serialized `SQL SELECT` statement and pass the query results back
to the GUI.  Furthermore, the GUI is intended to be an `iframe` embedded in an application.  When the GUI receives query results
from qb4j-api, it will post a message to the encapsulating application, which allows the encapsulating application to 
consume, analyze, and visualize the data to its users as it sees fit.

## Use ![Hammer and Wrench](https://github.githubassets.com/images/icons/emoji/unicode/1f6e0.png)
Pull the latest `qb4j-api` image with:

```shell script
docker pull joneschris/qb4j-api:latest
```

You'll need at least 1 SQL database running either locally or remotely.  

Currently, qb4j-api has passed tests to read and query the following databases:

- PostgreSQL
- MySQL
- SQLite

The following databases are planned to be supported in the future:

- Oracle
- SQL Server 

To test qb4j-api, let's pull a PostgreSQL docker image and run a container with it:

```shell script
docker pull postgres

docker run --name some-postgres -e POSTGRES_PASSWORD=mysecretpassword -d postgres
```

qb4j-api relies on a `YAML` file to give it context (ie: to make it aware) of
 
1) The databases you want it to read and query (known as `target databases` or `target data sources`).
2) The location of the cache of database metadata (the cache serves database metadata to qb4j-api so that qb4j-api does not 
have to query the databases constantly for the metadata).
3) The location of the database that it can read and write JSON-serialized SQL SELECT queries in order for users 1) to use
or 2) run other users' queries as sub queries in their own queries (Optional Feature).

The `YAML` file looks like this:
```yaml
targetDataSources:
  - name: my_database
    url: jdbc:postgresql://localhost:5432/postgres
    driverClassName: org.postgresql.Driver
    databaseType: PostgreSQL
    username: postgres
    password: mysecretpassword
    excludeObjects:
      schemas: []
      tables: []
      columns: []

databaseMetadataCacheSource:
  cacheType: IN_MEMORY

queryTemplateDataSource:
  url: jdbc:mysql://127.0.0.1:3306/qb4j
  driverClassName: com.mysql.cj.jdbc.Driver
  username: root
  password: password
```

Create a `YAML` file called `qb4j.yml` by doing the following:  

```shell script
touch qb4j.yml
```

Copy and paste the `YAML` above into the file you created and save it.

You pass this YAML into the qb4j-api docker container at runtime when starting the container, like so:

```shell script
QB4J_CONFIG=$(cat ./qb4j.yml)

docker container run --publish 8080:8080 --detach --env qb4j_config="$QB4J_CONFIG" joneschris/qb4j-api:latest
```

You can run `curl http://localhost:8080/metadata/database` to get a response from the API showing the target database.

Alternatively, you can go to `http://querybuilder4j.net/?baseApiUrl=http://localhost:8080` to explore the database metadata,
build queries, and run them in the GUI.  

***NOTE:  If you run a query, open your browser's developer tools and look at the console to see the query results.

## Database Metadata Caching
qb4j-api uses target database metadata such as schema names, table and view names, and column data types to serve this data
to clients, validate JSON-serialized SQL SELECT statements, and build a SQL SELECT statement from the JSON-serialized SQL 
SELECT statement that can be run against the target database. 

If qb4j-api queried the target database for this metadata every time a client requested metadata or it needed to validate a
JSON-serialized SQL SELECT statement, or build a SQL SELECT statement from a JSON-serialized SQL SELECT statement, it could 
affect the target database's performance.  To avoid this, when qb4j-api starts, it will query the target database for metadata
and write the metadata to either an in-memory `HashMap` or a Redis cache.  Read below to understand each option.

### In memory Cache
An in-memory cache is best suited for development, small target databases, or when running only 1 Docker container in
a container orchestration platform.

To enable in-memory caching, change the `databaseMetadataCacheSource.cacheType` value to `IN_MEMORY` (it is case-sensitive):

```yaml
targetDataSources:
  - name: my_database
    url: jdbc:postgresql://localhost:5432/postgres
    driverClassName: org.postgresql.Driver
    databaseType: PostgreSQL
    username: postgres
    password: mysecretpassword
    excludeObjects:
      schemas: []
      tables: []
      columns: []

databaseMetadataCacheSource:
  cacheType: IN_MEMORY # <---- This is an in-memory cache.

queryTemplateDataSource:
  url: jdbc:mysql://127.0.0.1:3306/qb4j
  driverClassName: com.mysql.cj.jdbc.Driver
  username: root
  password: password
```

qb4j-api will refresh the in-memory cache every 24 hours after it starts.

***NOTE:  There are plans to add an API endpoint that will refresh the cache, so that this can be done on-demand.

### Redis Cache
A Redis cache is best suited for production, large target databases, or when running more than 1 Docker container in
a container orchestration platform.

To enable Redis caching, change the `databaseMetadataCacheSource.cacheType` value to `REDIS` (it is case-sensitive):

```yaml
targetDataSources:
  - name: my_database
    url: jdbc:postgresql://localhost:5432/postgres
    driverClassName: org.postgresql.Driver
    databaseType: PostgreSQL
    username: postgres
    password: mysecretpassword
    excludeObjects:
      schemas: []
      tables: []
      columns: []

databaseMetadataCacheSource:
  cacheType: REDIS # <---- This is a Redis cache.
  host: localhost  # <---- The Redis cache host.
  port: 6379       # <---- The Redis cache port.

queryTemplateDataSource:
  url: jdbc:mysql://127.0.0.1:3306/qb4j
  driverClassName: com.mysql.cj.jdbc.Driver
  username: root
  password: password
```

#### Running qb4j-api to Update a Redis Cache
Because a Redis cache could be used by multiple qb4j-api instances or other applications, qb4j-api does **NOT** populate 
the Redis cache with database metadata on start up, unlike an in-memory cache.  

To populate the Redis cache, you can run qb4j-api in `Update Cache` mode by doing the following:
```shell script
docker run --env qb4j_config="$QB4J_CONFIG" --env update_cache=true joneschris/qb4j-api:latest
```

When the `update_cache` environment variable is set to `true` (the default is `false` if not specified), the qb4j-api container
will read target database metadata and write it to the Redis cache specified in the `qb4j.yml` file and then exit.  This 
allows you to set up a scheduled task to initally populate and refresh the Redis cache.

## Local Development

#### Compile and Run the API
To compile the API into a JAR and build a docker image with the JAR in it, run the following command from the project root 
directory:

```shell script
export PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

mvn clean install -Dmaven.javadoc.skip=true && sudo docker image build -t joneschris/qb4j-api:"$PROJECT_VERSION" --build-arg project_version="$PROJECT_VERSION" .
```

To run the API locally and debug it in IntelliJ, do the following:
1) Set environment variables that will be interpolated in the next step.
    ```shell script
    export QB4J_CONFIG=$(cat ./.docker-compose/compose-qb4j.yml)
    
    export UPDATE_CACHE=false
    ```
   ***NOTE:  If you want to see the output of `QB4J_CONFIG` in the terminal, wrap the variable in double quotes 
   like:  `echo "$QB4J_CONFIG"`, not `echo $QB4J_CONFIG`.
2) Run `sudo -E docker-compose -f ./.docker-compose/docker-compose.yml up  && sudo -E docker-compose -f ./.docker-compose/docker-compose.yml down`

3) Set up an IntelliJ Remote Debugger Configuration like so (these should be the default Remote settings): ![IntelliJ Configuration](./readme-images/intellij_remote_debugger_configuration.png)

4) Run the IntelliJ Remote Debugger Configuration by clicking on the green Debug icon:  ![IntelliJ Configuration](./readme-images/intellij_debugger.png)
