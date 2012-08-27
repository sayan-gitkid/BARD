
-- Standard Alter Table SQL

ALTER TABLE ASSAY MODIFY(READY_FOR_EXTRACTION  DEFAULT 'Pending')
;

-- Drop Referencing Constraint SQL

ALTER TABLE MEASURE DROP CONSTRAINT FK_MEASURE_M_CONTEXT
;
DROP INDEX FK_MEASURE_M_CONTEXT
;
ALTER TABLE MEASURE RENAME COLUMN MEASURE_CONTEXT_ID TO ASSAY_CONTEXT_ID
;
ALTER TABLE MEASURE_CONTEXT_ITEM DROP CONSTRAINT FK_M_CONTEXT_ITEM_M_CONTEXT
;

-- Drop Constraint, Rename and Create Table SQL

--DROP INDEX FK_MEASURE_CONTEXT_ASSAY
--/
ALTER TABLE MEASURE_CONTEXT DROP PRIMARY KEY DROP INDEX
;
ALTER TABLE MEASURE_CONTEXT DROP CONSTRAINT FK_MEASURE_CONTEXT_ASSAY
;
ALTER TABLE MEASURE_CONTEXT RENAME TO ASSAY_CONTEXT
;
ALTER TABLE ASSAY_CONTEXT RENAME COLUMN MEASURE_CONTEXT_ID TO ASSAY_CONTEXT_ID
;
COMMENT ON COLUMN ASSAY_CONTEXT.CONTEXT_NAME IS
'used as a title for the cards in the UI'
;


-- MEASURE_CONTEXT_ITEM --------------------------------------------------------------
ALTER TABLE MEASURE_CONTEXT_ITEM DROP CONSTRAINT FK_MEASURE_CONTEXT_ITEM_GROUP
;
ALTER TABLE MEASURE_CONTEXT_ITEM DROP CONSTRAINT FK_M_CONTEXT_ITEM_ASSAY
;
ALTER TABLE MEASURE_CONTEXT_ITEM DROP CONSTRAINT FK_M_CONTEXT_ITEM_ATTRIBUTE
;
ALTER TABLE MEASURE_CONTEXT_ITEM DROP CONSTRAINT FK_M_CONTEXT_ITEM_VALUE
;
ALTER TABLE MEASURE_CONTEXT_ITEM DROP PRIMARY KEY DROP INDEX
;
ALTER TABLE MEASURE_CONTEXT_ITEM DROP CONSTRAINT CK_ATTRIBUTE_TYPE
;
ALTER TABLE MEASURE_CONTEXT_ITEM DROP CONSTRAINT CK_MEASURE_CONTEXT_ITEM_QALFR
;
DROP INDEX FK_M_CONTEXT_ITEM_M_CONTEXT
;
DROP INDEX FK_M_CONTEXT_ITEM_ATTRIBUTE
;
DROP INDEX FK_M_CONTEXT_ITEM_VALUE
;
DROP INDEX FK_M_CONTEXT_ITEM_ASSAY
;
DROP INDEX FK_M_CONTEXT_ITEM_QUALIFIER
;
DROP INDEX FK_MEASURE_CONTEXT_ITEM_GROUP
;
DROP INDEX AK_MEASURE_CONTEXT_ITEM
;
ALTER TABLE MEASURE_CONTEXT_ITEM RENAME TO MEASURE_CO_08202012212129000
;
CREATE TABLE ASSAY_CONTEXT_ITEM
(
    ASSAY_CONTEXT_ITEM_ID NUMBER(19)    NOT NULL,
    ASSAY_CONTEXT_ID      NUMBER(19)    NOT NULL,
    DISPLAY_ORDER         NUMBER(38)    DEFAULT 0 NOT NULL,
    ATTRIBUTE_TYPE        VARCHAR2(20)  NOT NULL,
    ATTRIBUTE_ID          NUMBER(19)    NOT NULL,
    QUALIFIER             CHAR(2)           NULL,
    VALUE_ID              NUMBER(19)        NULL,
    EXT_VALUE_ID          VARCHAR2(60)      NULL,
    VALUE_DISPLAY         VARCHAR2(500)     NULL,
    VALUE_NUM             FLOAT(126)        NULL,
    VALUE_MIN             FLOAT(126)        NULL,
    VALUE_MAX             FLOAT(126)        NULL,
    VERSION               NUMBER(38)    DEFAULT 0 NOT NULL,
    DATE_CREATED          TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED          TIMESTAMP(6)      NULL,
    MODIFIED_BY           VARCHAR2(40)      NULL
)
;
COMMENT ON COLUMN ASSAY_CONTEXT_ITEM.VALUE_DISPLAY IS
'This is not a general text entry field, rather it is an easily displayable text version of the other value columns'
;

-- Insert Data SQL

ALTER TABLE MEASURE LOGGING
;
-- the old data is already there, we need to add items for the MEASURE_CONTEXT_ITEMS
INSERT INTO ASSAY_CONTEXT
    (ASSAY_CONTEXT_ID,
    ASSAY_ID,
    CONTEXT_NAME
    )
SELECT
    MEASURE_CONTEXT_ITEM_ID,
    ASSAY_ID,
    substr(VALUE_DISPLAY, 1, 128)
FROM MEASURE_CO_08202012212129000
WHERE measure_context_id is null
  and MEASURE_CONTEXT_ITEM_ID IN
    (SELECT GROUP_MEASURE_CONTEXT_ITEM_ID
     FROM MEASURE_CO_08202012212129000)
  and measure_context_item_id not in
        (select assay_context_id
         from assay_context)
ORDER BY 1,2
;
ALTER TABLE ASSAY_CONTEXT LOGGING
;
ALTER SESSION ENABLE PARALLEL DML
;
INSERT INTO ASSAY_CONTEXT_ITEM(
                       ASSAY_CONTEXT_ITEM_ID,
                       ASSAY_CONTEXT_ID,
                       DISPLAY_ORDER,
                       ATTRIBUTE_TYPE,
                       ATTRIBUTE_ID,
                       QUALIFIER,
                       VALUE_ID,
                       EXT_VALUE_ID,
                       VALUE_DISPLAY,
                       VALUE_NUM,
                       VALUE_MIN,
                       VALUE_MAX,
                       VERSION,
                       DATE_CREATED,
                       LAST_UPDATED,
                       MODIFIED_BY
                      )
                SELECT
                       MEASURE_CONTEXT_ITEM_ID,
                       MEASURE_CONTEXT_ID,
                       0,
                       ATTRIBUTE_TYPE,
                       ATTRIBUTE_ID,
                       QUALIFIER,
                       VALUE_ID,
                       EXT_VALUE_ID,
                       VALUE_DISPLAY,
                       VALUE_NUM,
                       VALUE_MIN,
                       VALUE_MAX,
                       VERSION,
                       DATE_CREATED,
                       LAST_UPDATED,
                       MODIFIED_BY
                  FROM MEASURE_CO_08202012212129000
                  where MEASURE_CONTEXT_ID IS NOT NULL
;
-- KEEP MEASURE_CO_08202012212129000as some of the rows will not make it into the new table
INSERT INTO ASSAY_CONTEXT_ITEM(
                       ASSAY_CONTEXT_ITEM_ID,
                       ASSAY_CONTEXT_ID,
                       DISPLAY_ORDER,
                       ATTRIBUTE_TYPE,
                       ATTRIBUTE_ID,
                       QUALIFIER,
                       VALUE_ID,
                       EXT_VALUE_ID,
                       VALUE_DISPLAY,
                       VALUE_NUM,
                       VALUE_MIN,
                       VALUE_MAX,
                       VERSION,
                       DATE_CREATED,
                       LAST_UPDATED,
                       MODIFIED_BY
                      )
                SELECT
                       MEASURE_CONTEXT_ITEM_ID,
                       GROUP_MEASURE_CONTEXT_ITEM_ID,
                       0,
                       ATTRIBUTE_TYPE,
                       ATTRIBUTE_ID,
                       QUALIFIER,
                       VALUE_ID,
                       EXT_VALUE_ID,
                       VALUE_DISPLAY,
                       VALUE_NUM,
                       VALUE_MIN,
                       VALUE_MAX,
                       VERSION,
                       DATE_CREATED,
                       LAST_UPDATED,
                       MODIFIED_BY
                  FROM MEASURE_CO_08202012212129000
                  where GROUP_MEASURE_CONTEXT_ITEM_ID IN
                        (SELECT ASSAY_CONTEXT_ID
                         FROM ASSAY_CONTEXT)
                    and MEASURE_CONTEXT_ID IS NULL
                    and GROUP_MEASURE_CONTEXT_ITEM_ID NOT IN
                        (SELECT ASSAY_CONTEXT_ITEM_ID
                         FROM ASSAY_CONTEXT_ITEM)
;
ALTER TABLE ASSAY_CONTEXT_ITEM LOGGING
;

-- Add Indexes SQL

-- Add Constraint SQL

ALTER TABLE ASSAY_CONTEXT_ITEM ADD CONSTRAINT PK_ASSAY_CONTEXT_ITEM
PRIMARY KEY (ASSAY_CONTEXT_ITEM_ID)
;
ALTER TABLE ASSAY_CONTEXT ADD CONSTRAINT PK_ASSAY_CONTEXT
PRIMARY KEY (ASSAY_CONTEXT_ID)
;
ALTER TABLE ASSAY_CONTEXT_ITEM ADD CONSTRAINT CK_ATTRIBUTE_TYPE
CHECK (Attribute_Type in ('Fixed', 'List', 'Range', 'Free'))
;
ALTER TABLE ASSAY_CONTEXT_ITEM ADD CONSTRAINT CK_ASSAY_CONTEXT_ITEM_QALFR
CHECK (QUALIFIER IN ('= ', '< ', '<=', '> ', '>=', '<<', '>>', '~ '))
;

-- Alter Index SQL

CREATE INDEX FK_MEASURE_A_CONTEXT
    ON MEASURE(ASSAY_CONTEXT_ID)
;
CREATE INDEX FK_A_CONTEXT_ITEM_QUALIFIER
    ON ASSAY_CONTEXT_ITEM(QUALIFIER)
;
CREATE INDEX FK_A_CONTEXT_ITEM_ATTRIBUTE
    ON ASSAY_CONTEXT_ITEM(ATTRIBUTE_ID)
;
CREATE UNIQUE INDEX AK_ASSAY_CONTEXT_ITEM
    ON ASSAY_CONTEXT_ITEM(ASSAY_CONTEXT_ID,DISPLAY_ORDER,ATTRIBUTE_ID,ATTRIBUTE_TYPE,VALUE_DISPLAY)
;
CREATE INDEX FK_A_CONTEXT_ITEM_A_CONTEXT
    ON ASSAY_CONTEXT_ITEM(ASSAY_CONTEXT_ID)
;

-- Add Referencing Foreign Keys SQL

ALTER TABLE MEASURE ADD CONSTRAINT FK_MEASURE_A_CONTEXT
FOREIGN KEY (ASSAY_CONTEXT_ID)
REFERENCES ASSAY_CONTEXT (ASSAY_CONTEXT_ID)
ENABLE VALIDATE
;
ALTER TABLE ASSAY_CONTEXT ADD CONSTRAINT FK_ASSAY_CONTEXT_ASSAY
FOREIGN KEY (ASSAY_ID)
REFERENCES ASSAY (ASSAY_ID)
ENABLE VALIDATE
;
ALTER TABLE ASSAY_CONTEXT_ITEM ADD CONSTRAINT FK_A_CONTEXT_ITEM_ATTRIBUTE
FOREIGN KEY (ATTRIBUTE_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ENABLE VALIDATE
;
ALTER TABLE ASSAY_CONTEXT_ITEM ADD CONSTRAINT FK_A_CONTEXT_ITEM_A_CONTEXT
FOREIGN KEY (ASSAY_CONTEXT_ID)
REFERENCES ASSAY_CONTEXT (ASSAY_CONTEXT_ID)
ENABLE VALIDATE
;
ALTER TABLE ASSAY_CONTEXT_ITEM ADD CONSTRAINT FK_A_CONTEXT_ITEM_VALUE
FOREIGN KEY (VALUE_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ENABLE VALIDATE
;

-- sequence renaming ----------------------------------------------------

RENAME MEASURE_CONTEXT_ID_SEQ TO ASSAY_CONTEXT_ID_SEQ
;
RENAME MEASURE_CONTEXT_ITEM_ID_SEQ TO ASSAY_CONTEXT_ITEM_ID_SEQ
;
