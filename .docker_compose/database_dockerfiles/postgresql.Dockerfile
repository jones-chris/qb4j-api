FROM postgres:latest

COPY ../database_seeders/finance_database_seeder.sql /docker-entrypoint-initdb.d

EXPOSE 5432
