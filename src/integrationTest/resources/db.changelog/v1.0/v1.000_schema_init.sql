CREATE TABLE users (
    id UUID PRIMARY KEY,
    login VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR NOT NULL,
    email VARCHAR(100) UNIQUE  NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    status VARCHAR NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP
);

CREATE TABLE user_privilege (
    id UUID PRIMARY KEY,
    user_id UUID,
    privilege VARCHAR(100),
    created TIMESTAMP,
    updated TIMESTAMP
);