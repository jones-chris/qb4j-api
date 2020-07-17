FROM openjdk:8

ARG environment
ARG project_version

ENV env=${environment}
# I have to put the project_version ARG in an ENV in order for it to be used in the CMD.
ENV project_version=${project_version}

RUN mkdir /qb4j
WORKDIR /qb4j
COPY /target/qb4j-api-${project_version}.jar .
RUN mkdir /qb4j/data
COPY /data /qb4j/data

EXPOSE 8080

CMD java -jar /qb4j/querybuilder4jmvc-${project_version}.jar
