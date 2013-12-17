
-- Sequence Alter SQL
RENAME RUN_CONTEXT_ITEM_ID_SEQ TO RSLT_CONTEXT_ITEM_ID_SEQ
;
ALTER SEQUENCE RSLT_CONTEXT_ITEM_ID_SEQ
CACHE 200
;
ALTER SEQUENCE RESULT_ID_SEQ
CACHE 1000
;
DROP SEQUENCE PROJECT_EXPERIMENT_ID_SEQ
;
CREATE SEQUENCE BARD_ONTOLOGY_SEQ
    START WITH 873
    INCREMENT BY 1
    NOMINVALUE
    NOMAXVALUE
    CACHE 20
    NOORDER
;
CREATE SEQUENCE PROJECT_CONTEXT_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE EXPRMT_CONTEXT_ITEM_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE EXPRMT_CONTEXT_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE STEP_CONTEXT_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE STEP_CONTEXT_ITEM_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE RESULT_HIERARCHY_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE UNIT_CONVERSION_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE PERSON_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE TEAM_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE ROLE_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE TEAM_MEMBER_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE PERSON_ROLE_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE FAVORITE_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 2
    NOORDER
;
CREATE SEQUENCE assay_context_measure_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 2147483648
    NOCYCLE
    CACHE 20
    NOORDER
;
