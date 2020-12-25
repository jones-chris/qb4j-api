FROM mysql:latest

ENV MYSQL_DATABASE="qb4j"
ENV MYSQL_ROOT_PASSWORD="qb4j"

COPY hr_database_seeder.sql /docker-entrypoint-initdb.d

EXPOSE 3306
