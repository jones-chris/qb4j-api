FROM openjdk:11

# Expose arguments to `docker build` so that the environment variables with the same names can be set dynamically.
ARG project_version
ARG qb4j_config
ARG update_cache=false

# Check that required arguments have values.
RUN if [ -z $project_version ]; then echo "project_version required"; exit 1; fi

# ***NOTE:  I have to put the project_version ARG in an ENV in order for it to be used in the CMD.
# - project_version is used to dynamically COPY the jar into the docker image and run the jar in the CMD.
# - qb4j_config could be empty when the docker image is built.  If so, it should be provided with `docker container run -e qb4j_config=$QB4J_CONFIG`.
# - update_cache defaults to false unless specified at build time as `true` or overriden at run time with `docker container run -e update_cache=$UPDATE_CACHE`.
ENV project_version=${project_version}
ENV qb4j_config=${qb4j_config}
ENV update_cache=${update_cache}

# Create a directory for the API jar and copy the jar into the directory.
RUN mkdir /qb4j
WORKDIR /qb4j
COPY /target/qb4j-api-${project_version}.jar .

# Create a directory inside /qb4j for the embedded databases.
RUN mkdir /qb4j/data
COPY /data /qb4j/data

# The port the API is running on.
EXPOSE 8080
# The port you can debug the JVM on.
EXPOSE 5005

# Execute the jar with the qb4j.yml contents passed into the execution as a named argument.
CMD java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Dqb4jConfig="$qb4j_config" -DupdateCache="$update_cache" -jar /qb4j/qb4j-api-${project_version}.jar
