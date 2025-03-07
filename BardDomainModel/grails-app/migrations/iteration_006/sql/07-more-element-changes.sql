
-- Drop Referencing Constraint SQL

ALTER TABLE ASSAY_CONTEXT_ITEM DROP CONSTRAINT FK_A_CONTEXT_ITEM_ATTRIBUTE
;
ALTER TABLE ASSAY_CONTEXT_ITEM DROP CONSTRAINT FK_A_CONTEXT_ITEM_VALUE
;
ALTER TABLE ELEMENT_HIERARCHY DROP CONSTRAINT FK_E_HIERARCHY_CHILD_ELEM_ID
;
ALTER TABLE ELEMENT_HIERARCHY DROP CONSTRAINT FK_E_HIERARCHY_PARENT_ELEM_ID
;
ALTER TABLE MEASURE DROP CONSTRAINT FK_MEASURE_ELEMENT_UNIT
;
ALTER TABLE MEASURE DROP CONSTRAINT FK_MEASURE_RESULT_TYPE
;
ALTER TABLE ONTOLOGY_ITEM DROP CONSTRAINT FK_ONTOLOGY_ITEM_ELEMENT
;
ALTER TABLE PROJECT_CONTEXT_ITEM DROP CONSTRAINT FK_PROJ_CONTEXT_ITEM_ATTR
;
ALTER TABLE PROJECT_CONTEXT_ITEM DROP CONSTRAINT FK_PROJ_CONTEXT_ITEM_VALUE
;
ALTER TABLE RESULT DROP CONSTRAINT FK_RESULT_RESULT_TYPE
;
ALTER TABLE RUN_CONTEXT_ITEM DROP CONSTRAINT FK_R_CONTEXT_ITEM_ATTRIBUTE
;
ALTER TABLE RUN_CONTEXT_ITEM DROP CONSTRAINT FK_R_CONTEXT_ITEM_VALUE
;
ALTER TABLE TREE_ROOT DROP CONSTRAINT FK_TREE_ROOT_ELEMENT
;
ALTER TABLE UNIT_CONVERSION DROP CONSTRAINT FK_UNIT_CONVERSN_FRM_UNT_ELMNT
;
ALTER TABLE UNIT_CONVERSION DROP CONSTRAINT FK_UNIT_CONVERSN_TO_UNT_ELMNT
;

-- Drop Constraint, Rename and Create Table SQL

ALTER TABLE ELEMENT DROP CONSTRAINT FK_ELEMENT_UNIT
;
ALTER TABLE ELEMENT DROP PRIMARY KEY DROP INDEX
;
ALTER TABLE ELEMENT DROP UNIQUE (LABEL) DROP INDEX
;
ALTER TABLE ELEMENT DROP CONSTRAINT CK_ELEMENT_EXTRACTION
;
ALTER TABLE ELEMENT DROP CONSTRAINT CK_ELEMENT_STATUS
;
DROP INDEX FK_ELEMENT_UNIT
;
DROP INDEX IDX_ELEMENT_LOWER_LABEL
;
ALTER TABLE ELEMENT RENAME TO ELEMENT_08232012142820000
;
CREATE TABLE ELEMENT
(
    ELEMENT_ID           NUMBER(19)     NOT NULL,
    ELEMENT_STATUS       VARCHAR2(20)   DEFAULT 'Pending' NOT NULL,
    LABEL                VARCHAR2(128)  NOT NULL,
    DESCRIPTION          VARCHAR2(1000)     NULL,
    ABBREVIATION         VARCHAR2(20)       NULL,
    SYNONYMS             VARCHAR2(1000)     NULL,
    UNIT                 VARCHAR2(128)      NULL,
    BARD_URI             VARCHAR2(250)      NULL,
    EXTERNAL_URL         VARCHAR2(1000)     NULL,
    READY_FOR_EXTRACTION VARCHAR2(20)   DEFAULT 'Pending' NOT NULL,
    VERSION              NUMBER(38)     DEFAULT 0 NOT NULL,
    DATE_CREATED         TIMESTAMP(6)   DEFAULT sysdate NOT NULL,
    LAST_UPDATED         TIMESTAMP(6)       NULL,
    MODIFIED_BY          VARCHAR2(40)       NULL
)
;

-- Insert Data SQL

ALTER SESSION ENABLE PARALLEL DML
;
INSERT INTO ELEMENT(
                             ELEMENT_ID,
                             ELEMENT_STATUS,
                             LABEL,
                             DESCRIPTION,
                             ABBREVIATION,
                             SYNONYMS,
                             UNIT,
                             BARD_URI,
                             EXTERNAL_URL,
                             READY_FOR_EXTRACTION,
                             VERSION,
                             DATE_CREATED,
                             LAST_UPDATED,
                             MODIFIED_BY
                            )
                      SELECT
                             ELEMENT_ID,
                             ELEMENT_STATUS,
                             LABEL,
                             DESCRIPTION,
                             ABBREVIATION,
                             SYNONYMS,
                             UNIT,
                             NULL,
                             EXTERNAL_URL,
                             READY_FOR_EXTRACTION,
                             VERSION,
                             DATE_CREATED,
                             LAST_UPDATED,
                             MODIFIED_BY
                        FROM ELEMENT_08232012142820000
;
ALTER TABLE ELEMENT LOGGING
;

-- Add Indexes SQL

CREATE INDEX FK_ELEMENT_UNIT
    ON ELEMENT(UNIT)
;
CREATE INDEX IDX_ELEMENT_LOWER_LABEL
    ON ELEMENT(LOWER("LABEL"))
;

-- Add Constraint SQL

ALTER TABLE ELEMENT ADD CONSTRAINT PK_ELEMENT
PRIMARY KEY (ELEMENT_ID)
;
ALTER TABLE ELEMENT ADD CONSTRAINT AK_ELEMENT_LABEL
UNIQUE (LABEL)
;
ALTER TABLE ELEMENT ADD CONSTRAINT CK_ELEMENT_EXTRACTION
CHECK (ready_for_extraction IN ('Pending', 'Ready', 'Started', 'Complete'))
;
ALTER TABLE ELEMENT ADD CONSTRAINT CK_ELEMENT_STATUS
CHECK (Element_Status IN ('Pending', 'Published', 'Deprecated', 'Retired'))
;

-- Add Dependencies SQL

-- Update Views SQL

CREATE OR REPLACE VIEW ASSAY_ELEMENT
(Element_ID, Element_Status, Label, Description, Abbreviation, Synonyms, Unit, BARD_URI, External_URL, ready_for_extraction, Version, Date_Created, Last_Updated, Modified_by) AS
SELECT El.ELEMENT_ID, El.ELEMENT_STATUS, El.LABEL, El.DESCRIPTION, El.ABBREVIATION, El.SYNONYMS, El.UNIT, El.BARD_URI, El.EXTERNAL_URL, El.READY_FOR_EXTRACTION, El.VERSION, El.DATE_CREATED, El.LAST_UPDATED, El.MODIFIED_BY
FROM ELEMENT El
WHERE ELEMENT_ID in (select ELEMENT_ID from ASSAY_DESCRIPTOR_TREE)
;
CREATE OR REPLACE VIEW BIOLOGY_ELEMENT
(Element_ID, Element_Status, Label, Description, Abbreviation, Synonyms, Unit, BARD_URI, External_URL, ready_for_extraction, Version, Date_Created, Last_Updated, Modified_by) AS
SELECT El.ELEMENT_ID, El.ELEMENT_STATUS, El.LABEL, El.DESCRIPTION, El.ABBREVIATION, El.SYNONYMS, El.UNIT, El.BARD_URI, El.EXTERNAL_URL, El.READY_FOR_EXTRACTION, El.VERSION, El.DATE_CREATED, El.LAST_UPDATED, El.MODIFIED_BY
FROM ELEMENT El
WHERE el.ELEMENT_ID in (select ELEMENT_ID from BIOLOGY_DESCRIPTOR_TREE)
;
CREATE OR REPLACE VIEW INSTANCE_ELEMENT
(Element_ID, Element_Status, Label, Description, Abbreviation, Synonyms, Unit, BARD_URI, External_URL, ready_for_extraction, Version, Date_Created, Last_Updated, Modified_by) AS
SELECT El.ELEMENT_ID, El.ELEMENT_STATUS, El.LABEL, El.DESCRIPTION, El.ABBREVIATION, El.SYNONYMS, El.UNIT, El.BARD_URI, El.EXTERNAL_URL, El.READY_FOR_EXTRACTION, El.VERSION, El.DATE_CREATED, El.LAST_UPDATED, El.MODIFIED_BY
FROM ELEMENT El
WHERE el.ELEMENT_ID in (select ELEMENT_ID from INSTANCE_DESCRIPTOR_TREE)
;
CREATE OR REPLACE VIEW LABORATORY_ELEMENT
(Element_ID, Element_Status, Label, Description, Abbreviation, Synonyms, Unit, BARD_URI, External_URL, ready_for_extraction, Version, Date_Created, Last_Updated, Modified_by) AS
SELECT El.ELEMENT_ID, El.ELEMENT_STATUS, El.LABEL, El.DESCRIPTION, El.ABBREVIATION, El.SYNONYMS, El.UNIT, El.BARD_URI, El.EXTERNAL_URL, El.READY_FOR_EXTRACTION, El.VERSION, El.DATE_CREATED, El.LAST_UPDATED, El.MODIFIED_BY
FROM ELEMENT El
WHERE ELEMENT_ID in (select LABORATORY_ID from LABORATORY_TREE)
;
CREATE OR REPLACE VIEW RESULT_TYPE_ELEMENT
(Element_ID, Element_Status, Label, Description, Abbreviation, Synonyms, Unit, BARD_URI, External_URL, ready_for_extraction, Version, Date_Created, Last_Updated, Modified_by) AS
SELECT El.ELEMENT_ID, El.ELEMENT_STATUS, El.LABEL, El.DESCRIPTION, El.ABBREVIATION, El.SYNONYMS, El.UNIT, El.BARD_URI, El.EXTERNAL_URL, El.READY_FOR_EXTRACTION, El.VERSION, El.DATE_CREATED, El.LAST_UPDATED, El.MODIFIED_BY
FROM ELEMENT El
WHERE el.ELEMENT_ID in (select RESULT_TYPE_ID from RESULT_TYPE_TREE)
;
CREATE OR REPLACE VIEW STAGE_ELEMENT
(Element_ID, Element_Status, Label, Description, Abbreviation, Synonyms, Unit, BARD_URI, External_URL, ready_for_extraction, Version, Date_Created, Last_Updated, Modified_by) AS
SELECT El.ELEMENT_ID, El.ELEMENT_STATUS, El.LABEL, El.DESCRIPTION, El.ABBREVIATION, El.SYNONYMS, El.UNIT, El.BARD_URI, El.EXTERNAL_URL, El.READY_FOR_EXTRACTION, El.VERSION, El.DATE_CREATED, El.LAST_UPDATED, El.MODIFIED_BY
FROM ELEMENT El
WHERE el.ELEMENT_ID in (select STAGE_ID from STAGE_TREE)
;
CREATE OR REPLACE VIEW UNIT_ELEMENT
(Element_ID, Element_Status, Label, Description, Abbreviation, Synonyms, Unit, BARD_URI, External_URL, ready_for_extraction, Version, Date_Created, Last_Updated, Modified_by) AS
SELECT El.ELEMENT_ID, El.ELEMENT_STATUS, El.LABEL, El.DESCRIPTION, El.ABBREVIATION, El.SYNONYMS, El.UNIT, El.BARD_URI, El.EXTERNAL_URL, El.READY_FOR_EXTRACTION, El.VERSION, El.DATE_CREATED, El.LAST_UPDATED, El.MODIFIED_BY
FROM ELEMENT El
WHERE ELEMENT_ID in (select UNIT_ID from UNIT_TREE)
;

-- Add Referencing Foreign Keys SQL

ALTER TABLE ASSAY_CONTEXT_ITEM ADD CONSTRAINT FK_A_CONTEXT_ITEM_ATTRIBUTE
FOREIGN KEY (ATTRIBUTE_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ENABLE VALIDATE
;
ALTER TABLE ASSAY_CONTEXT_ITEM ADD CONSTRAINT FK_A_CONTEXT_ITEM_VALUE
FOREIGN KEY (VALUE_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ENABLE VALIDATE
;
ALTER TABLE ELEMENT_HIERARCHY ADD CONSTRAINT FK_E_HIERARCHY_CHILD_ELEM_ID
FOREIGN KEY (CHILD_ELEMENT_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ON DELETE CASCADE
ENABLE VALIDATE
;
ALTER TABLE ELEMENT_HIERARCHY ADD CONSTRAINT FK_E_HIERARCHY_PARENT_ELEM_ID
FOREIGN KEY (PARENT_ELEMENT_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ON DELETE CASCADE
ENABLE VALIDATE
;
ALTER TABLE MEASURE ADD CONSTRAINT FK_MEASURE_ELEMENT_UNIT
FOREIGN KEY (ENTRY_UNIT)
REFERENCES ELEMENT (LABEL)
ENABLE VALIDATE
;
ALTER TABLE MEASURE ADD CONSTRAINT FK_MEASURE_RESULT_TYPE
FOREIGN KEY (RESULT_TYPE_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ENABLE VALIDATE
;
ALTER TABLE ONTOLOGY_ITEM ADD CONSTRAINT FK_ONTOLOGY_ITEM_ELEMENT
FOREIGN KEY (ELEMENT_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ON DELETE CASCADE
ENABLE VALIDATE
;
ALTER TABLE PROJECT_CONTEXT_ITEM ADD CONSTRAINT FK_PROJ_CONTEXT_ITEM_ATTR
FOREIGN KEY (ATTRIBUTE_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ENABLE VALIDATE
;
ALTER TABLE PROJECT_CONTEXT_ITEM ADD CONSTRAINT FK_PROJ_CONTEXT_ITEM_VALUE
FOREIGN KEY (VALUE_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ENABLE VALIDATE
;
ALTER TABLE RESULT ADD CONSTRAINT FK_RESULT_RESULT_TYPE
FOREIGN KEY (RESULT_TYPE_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ENABLE VALIDATE
;
ALTER TABLE RUN_CONTEXT_ITEM ADD CONSTRAINT FK_R_CONTEXT_ITEM_ATTRIBUTE
FOREIGN KEY (ATTRIBUTE_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ENABLE VALIDATE
;
ALTER TABLE RUN_CONTEXT_ITEM ADD CONSTRAINT FK_R_CONTEXT_ITEM_VALUE
FOREIGN KEY (VALUE_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ENABLE VALIDATE
;
ALTER TABLE TREE_ROOT ADD CONSTRAINT FK_TREE_ROOT_ELEMENT
FOREIGN KEY (ELEMENT_ID)
REFERENCES ELEMENT (ELEMENT_ID)
ENABLE VALIDATE
;
ALTER TABLE UNIT_CONVERSION ADD CONSTRAINT FK_UNIT_CONVERSN_FRM_UNT_ELMNT
FOREIGN KEY (FROM_UNIT)
REFERENCES ELEMENT (LABEL)
ENABLE VALIDATE
;
ALTER TABLE UNIT_CONVERSION ADD CONSTRAINT FK_UNIT_CONVERSN_TO_UNT_ELMNT
FOREIGN KEY (TO_UNIT)
REFERENCES ELEMENT (LABEL)
ENABLE VALIDATE
;
ALTER TABLE ELEMENT ADD CONSTRAINT FK_ELEMENT_UNIT
FOREIGN KEY (UNIT)
REFERENCES ELEMENT (LABEL)
ENABLE VALIDATE
;
