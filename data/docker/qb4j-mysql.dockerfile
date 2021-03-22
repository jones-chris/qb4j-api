# This is a dockerfile for the qb4j-mysql image that is the HR database in the qb4j DEV environment.

FROM mysql:8.0.23

COPY ./.docker-compose/database/hr_database/hr_database_seeder.sql /docker-entrypoint-initdb.d

EXPOSE 3306