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