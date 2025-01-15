CREATE TABLE if not exists server
(
    id             BIGSERIAL PRIMARY KEY,
    created_by     VARCHAR(200)                NOT NULL,
    updated_by     VARCHAR(200),
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE,
    server_id      VARCHAR(100),
    server_name    varchar(200),
    server_ip      varchar(200),
    config         TEXT,
    status         varchar(200),
    count_used     INTEGER,
    protocol       varchar(200),
    last_used_time TIMESTAMP WITHOUT TIME ZONE
);


CREATE TABLE if not exists server_history
(
    id         BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    server_id  BIGINT                      NOT NULL REFERENCES server,
    ip         VARCHAR(200),
    device_id  VARCHAR(100)
);


CREATE TABLE if not exists request_type
(
    id         BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    name       VARCHAR(100)                NOT NULL UNIQUE,
    fa_name    VARCHAR(100)                NOT NULL,
    display    INTEGER
    );

CREATE TABLE if not exists channel
(
    id                   BIGSERIAL PRIMARY KEY,
    created_by           VARCHAR(200)                NOT NULL,
    updated_by           VARCHAR(200),
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITHOUT TIME ZONE,
    first_name           VARCHAR(100)                NOT NULL,
    last_name            VARCHAR(100)                NOT NULL,
    username             VARCHAR(100)                NOT NULL UNIQUE,
    password             VARCHAR(500)                NOT NULL,
    email                VARCHAR(100)                NOT NULL,
    mobile               VARCHAR(100)                NOT NULL,
    access_token         VARCHAR(400),
    refresh_token        VARCHAR(400),
    token_expire_time    TIMESTAMP WITHOUT TIME ZONE,
    expire_time_duration BIGINT        DEFAULT 31536000,
    status               INTEGER,
    trust                INTEGER       DEFAULT 0,
    sign                 INTEGER       DEFAULT 0,
    public_key           VARCHAR(1000) DEFAULT NULL::CHARACTER VARYING,
    ip                   VARCHAR(500)  DEFAULT NULL::CHARACTER VARYING,
    end_time             TIMESTAMP WITHOUT TIME ZONE
    );

CREATE TABLE if not exists channel_request_type
(
    id              BIGSERIAL PRIMARY KEY,
    created_by      VARCHAR(200)                NOT NULL,
    updated_by      VARCHAR(200),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NULL,
    channel_id      BIGINT                      NOT NULL REFERENCES channel,
    request_type_id BIGINT                      NOT NULL REFERENCES request_type
    );

INSERT INTO channel (created_by, updated_by, created_at, updated_at, first_name, last_name, username, password, email,
                     access_token, refresh_token, token_expire_time, expire_time_duration, trust, sign, public_key, ip,
                     status, mobile, end_time)
VALUES ('System', NULL, now(), NULL, 'admin', 'admin', 'masoud',
        '$2a$10$OKNikztLNUEN1mmGl2E6dOHWDCldyBfHWjJokwaN3kfMArt14TvYy', 'admin@admin.com', NULL, NULL, NULL, 36000000,
        1, 0, '', '0.0.0.0/0;0:0:0:0:0:0:0:0/0', 1, '09124162337', NULL);