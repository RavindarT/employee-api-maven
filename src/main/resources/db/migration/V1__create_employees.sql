CREATE TABLE employees (
    empid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL CHECK (length(trim(name)) > 0),
    years_of_experience INTEGER NOT NULL CHECK (years_of_experience BETWEEN 0 AND 80),
    city VARCHAR(100) NOT NULL CHECK (length(trim(city)) > 0)
);
