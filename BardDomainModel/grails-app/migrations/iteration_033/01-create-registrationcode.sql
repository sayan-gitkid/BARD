CREATE TABLE REGISTRATION_CODE
(
    REGISTRATION_CODE_ID NUMBER(19)    NOT NULL,
    USER_NAME             VARCHAR2(255)    NOT NULL,
    TOKEN             VARCHAR2(1000)    DEFAULT 0 NOT NULL,
    DATE_CREATED          TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED          TIMESTAMP(6)  DEFAULT sysdate NOT NULL
)
;
ALTER TABLE REGISTRATION_CODE ADD CONSTRAINT PK_REGISTRATION_CODE PRIMARY KEY (REGISTRATION_CODE_ID)
;
CREATE SEQUENCE REGISTRATION_CODE_ID_SEQ
    START WITH 873
    INCREMENT BY 1
    NOMINVALUE
    NOMAXVALUE
    CACHE 20
    NOORDER
;

