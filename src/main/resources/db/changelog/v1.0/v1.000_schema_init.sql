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