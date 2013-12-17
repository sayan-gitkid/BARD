CREATE TABLE BARD_TREE
(
  NODE_ID NUMBER(19, 0) NOT NULL
, PARENT_NODE_ID NUMBER(19, 0)
, ELEMENT_ID NUMBER(19, 0) NOT NULL
, ELEMENT_STATUS VARCHAR2(20 BYTE) NOT NULL
, LABEL VARCHAR2(128 BYTE) NOT NULL
, IS_LEAF CHAR(1 BYTE) NOT NULL
, FULL_PATH VARCHAR2(3000 BYTE)
, DESCRIPTION VARCHAR2(1000 BYTE)
, ABBREVIATION VARCHAR2(20 BYTE)
, SYNONYMS VARCHAR2(1000 BYTE)
, EXTERNAL_URL VARCHAR2(1000 BYTE)
, UNIT_ID NUMBER(19, 0)
, CONSTRAINT PK_BARD_TREE PRIMARY KEY
  (
    NODE_ID
  )
  ENABLE
);

CREATE INDEX IDX_BARD_TREE_ELEMENT_ID ON BARD_TREE (ELEMENT_ID ASC)
;
