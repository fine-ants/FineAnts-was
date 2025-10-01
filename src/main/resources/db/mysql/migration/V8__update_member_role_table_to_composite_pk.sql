-- 0. member_role 테이블의 중복 데이터 제거
WITH duplicates AS (SELECT member_role_id,
                           ROW_NUMBER() OVER (PARTITION BY member_id, role_role_id ORDER BY member_role_id) AS rn
                    FROM member_role)
DELETE
FROM member_role
WHERE member_role_id IN (SELECT member_role_id
                         FROM duplicates
                         WHERE rn > 1);

-- 1. 임시 테이블 생성 (기존 데이터 백업용)
CREATE TABLE member_role_backup AS
SELECT member_id AS member_id, role_role_id AS role_id
FROM member_role;

-- 2. 기존 테이블 삭제
DROP TABLE member_role;

-- 3. 새 테이블 생성 (복합 PK 구조)
CREATE TABLE member_role
(
    member_id BIGINT NOT NULL,
    role_id   BIGINT NOT NULL,
    PRIMARY KEY (member_id, role_id),
    CONSTRAINT fk_member_role_member FOREIGN KEY (member_id)
        REFERENCES member (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_member_role_role FOREIGN KEY (role_id)
        REFERENCES role (role_id)
        ON DELETE CASCADE
);

-- 4. 기존 데이터 복사
INSERT INTO member_role (member_id, role_id)
SELECT member_id, role_id
FROM member_role_backup;

-- 5. 백업 테이블 삭제
DROP TABLE member_role_backup;
