CREATE TABLE IF NOT EXISTS translations (
    id SERIAL PRIMARY KEY,
    ip_address VARCHAR(50) NOT NULL,
    input_text TEXT NOT NULL,
    translated_text TEXT NOT NULL
);