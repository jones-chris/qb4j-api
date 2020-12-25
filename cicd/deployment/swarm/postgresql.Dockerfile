FROM postgres:latest

COPY finance_database_seeder.sql /docker-entrypoint-initdb.d

EXPOSE 5432
