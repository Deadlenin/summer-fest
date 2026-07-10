CREATE TABLE events (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_date DATE,
    company_name VARCHAR(255),
    location VARCHAR(255),
    registration_enabled BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
