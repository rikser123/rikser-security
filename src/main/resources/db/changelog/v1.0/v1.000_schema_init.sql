CREATE TABLE users
(
    id          UUID PRIMARY KEY,
    login       VARCHAR(50) UNIQUE  NOT NULL,
    password    VARCHAR             NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    first_name  VARCHAR(100)        NOT NULL,
    middle_name VARCHAR(100),
    last_name   VARCHAR(100)        NOT NULL,
    birth_date  DATE,
    status      VARCHAR             NOT NULL,
    created     TIMESTAMP WITH TIME ZONE,
    updated     TIMESTAMP WITH TIME ZONE
);

CREATE TABLE user_privilege
(
    id        UUID PRIMARY KEY,
    user_id   UUID,
    privilege VARCHAR(100),
    created   TIMESTAMP WITH TIME ZONE,
    updated   TIMESTAMP WITH TIME ZONE
);

CREATE TABLE black_list_tokens
(
    id      UUID PRIMARY KEY,
    user_id UUID    NOT NULL,
    token   VARCHAR NOT NULL,
    created TIMESTAMP WITH TIME ZONE
);


CREATE TABLE refresh_token
(
    id         UUID PRIMARY KEY,
    token_hash VARCHAR                  NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id    UUID,
    revoked    BOOLEAN,
    created    TIMESTAMP WITH TIME ZONE,
    updated    TIMESTAMP WITH TIME ZONE
);

INSERT INTO users (id,
                   login,
                   password,
                   email,
                   first_name,
                   middle_name,
                   last_name,
                   birth_date,
                   status,
                   created,
                   updated)
VALUES ('458d0244-98ff-4c8a-b2ba-5e6f8440e37b',
        'Kafka123',
        '$2a$10$wy1Hsyd2Ld7ZaMfSz46stexFPT5XkFa6Z0GKbOOPpfqyQdrhlsDKi',
        'kafka@rar.ru',
        'kafka',
        'kafka',
        'kafka',
        '1990-02-08',
        'REGISTERED',
        NOW(),
        NOW());