ALTER TABLE member
    ADD CONSTRAINT uk_email_provider UNIQUE (email, provider);
