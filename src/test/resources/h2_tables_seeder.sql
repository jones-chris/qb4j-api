DROP TABLE IF EXISTS public.customers;

CREATE TABLE public.customers (
    id INTEGER PRIMARY KEY,
    first_name VARCHAR(25),
    last_name VARCHAR(25),
    address VARCHAR(30),
    city VARCHAR(20),
    state VARCHAR(20),
    country VARCHAR(20)
);

DROP TABLE IF EXISTS public.suppliers;

CREATE TABLE public.suppliers (
    id INTEGER PRIMARY KEY,
    name VARCHAR(50)
);