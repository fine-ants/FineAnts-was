ALTER TABLE fineAnts.member
    DROP CONSTRAINT UK_hh9kg6jti4n1eoiertn2k6qsc;

ALTER TABLE fineAnts.member
    MODIFY COLUMN nickname VARCHAR(100) NOT NULL UNIQUE;
