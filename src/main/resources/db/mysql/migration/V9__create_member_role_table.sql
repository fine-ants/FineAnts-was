CREATE TABLE IF NOT EXISTS member_role
(
    member_id BIGINT NOT NULL,
    role_id   BIGINT NOT NULL,
    PRIMARY KEY (member_id, role_id),
    CONSTRAINT fk_member_role_member
        FOREIGN KEY (member_id)
            REFERENCES fineAnts.member (id)
            ON DELETE CASCADE
);
