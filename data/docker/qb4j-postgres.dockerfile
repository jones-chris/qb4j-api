# This is a dockerfile for the qb4j-postgres image that is the finance database in the qb4j DEV environment.

FROM postgres:13-alpine

COPY ./.docker-compose/database/finance_database/finance_database_seeder.sql /docker-entrypoint-initdb.d

EXPOSE 5432