FROM openjdk:8

ARG project_version
ARG qb4j_config
ARG update_cache

# I have to put the project_version ARG in an ENV in order for it to be used in the CMD.
ENV project_version=${project_version}
ENV qb4j_config=${qb4j_config}
ENV updateCache=${update_cache}

# Create a directory for the API jar.
RUN mkdir /qb4j
WORKDIR /qb4j

# Copy the jar into the directory.
COPY /target/qb4j-api-${project_version}.jar .

# Create a directory inside /qb4j for the embedded databases.
RUN mkdir /qb4j/data
COPY /data /qb4j/data

EXPOSE 8080

# Execute the jar with the qb4j.yml contents passed into the execution as a named argument.
CMD java -Dqb4jConfig="$qb4j_config" -DupdateCache="$update_cache" -jar /qb4j/qb4j-api-${project_version}.jar
