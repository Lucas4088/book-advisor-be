ALTER TABLE crawler_configs
 ADD COLUMN proxy_session_enabled BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE crawler_configs
    ADD COLUMN forwarding_proxy_enabled BOOLEAN NOT NULL DEFAULT false;