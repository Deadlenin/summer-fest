CREATE TABLE participants (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    company VARCHAR(255) NOT NULL,
    project_role VARCHAR(255) NOT NULL,
    stack VARCHAR(255) NOT NULL,
    grade VARCHAR(255),
    telegram VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
