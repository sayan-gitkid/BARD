
-- Standard Alter Table SQL

ALTER TABLE ASSAY DROP CONSTRAINT CK_ASSAY_TYPE
;
ALTER TABLE ASSAY RENAME COLUMN ASSAY_TITLE TO ASSAY_SHORT_NAME
;
ALTER TABLE ASSAY ADD CONSTRAINT CK_ASSAY_TYPE
CHECK (Assay_Type IN ('Regular', 'Panel - Array', 'Panel - Group', 'Template'))
;
DROP INDEX AK_EXT_REFERENCE
;
ALTER TABLE PROJECT DROP CONSTRAINT CK_PROJECT_TYPE
;
ALTER TABLE PROJECT ADD CONSTRAINT CK_PROJECT_TYPE
CHECK (GROUP_TYPE in ('Project', 'Probe Report', 'Campaign', 'Panel', 'Study', 'Template'))
;

-- Drop Referencing Constraint SQL

ALTER TABLE ASSAY_CONTEXT_ITEM DROP CONSTRAINT FK_A_CONTEXT_ITEM_A_CONTEXT
;
ALTER TABLE ASSAY_CONTEXT_ITEM DROP CONSTRAINT FK_A_CONTEXT_ITEM_ATTRIBUTE
;
ALTER TABLE ASSAY_CONTEXT_ITEM DROP CONSTRAINT FK_A_CONTEXT_ITEM_VALUE
;
ALTER TABLE ELEMENT_HIERARCHY DROP CONSTRAINT FK_E_HIERARCHY_CHILD_ELEM_ID
;
ALTER TABLE ELEMENT_HIERARCHY DROP CONSTRAINT FK_E_HIERARCHY_PARENT_ELEM_ID
;
ALTER TABLE MEASURE DROP CONSTRAINT FK_MEASURE_RESULT_TYPE
;
ALTER TABLE MEASURE DROP CONSTRAINT FK_MEASURE_ELEMENT_UNIT
;
ALTER TABLE MEASURE DROP CONSTRAINT FK_MEASURE_A_CONTEXT
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
ALTER TABLE RESULT_HIERARCHY DROP CONSTRAINT FK_RESULT_HIERARCHY_RESULT
;
ALTER TABLE RESULT_HIERARCHY DROP CONSTRAINT FK_RESULT_HIERARCHY_RSLT_PRNT
;
ALTER TABLE RUN_CONTEXT_ITEM DROP CONSTRAINT FK_R_CONTEXT_ITEM_RESULT
;
DROP TABLE STAGE_TREE CASCADE CONSTRAINTS
;
CREATE TABLE STAGE_TREE
(
    NODE_ID        NUMBER(19)     NOT NULL,
    PARENT_NODE_ID NUMBER(19)         NULL,
    STAGE_ID       NUMBER(19)     NOT NULL,
    STAGE_STATUS   VARCHAR2(20)   NOT NULL,
    STAGE          VARCHAR2(128)  NOT NULL,
    IS_LEAF        CHAR(1)        NOT NULL,
    DESCRIPTION    VARCHAR2(1000)     NULL,
    FULL_PATH      VARCHAR2(2500)     NULL
)
;
ALTER TABLE UNIT_CONVERSION DROP PRIMARY KEY DROP INDEX
;
DROP INDEX FK_UNIT_CONVERSN_FRM_UNT_ELMNT
;
DROP INDEX FK_UNIT_CONVERSN_TO_UNT_ELMNT
;
DROP TABLE UNIT_CONVERSION
;
CREATE TABLE UNIT_CONVERSION
(
    UNIT_CONVERSION_ID NUMBER(19)    NOT NULL,
    FROM_UNIT_ID       NUMBER(19)    NOT NULL,
    TO_UNIT_ID         NUMBER(19)    NOT NULL,
    MULTIPLIER         NUMBER(30,15)     NULL,
    OFFSET             NUMBER(30,15)     NULL,
    FORMULA            VARCHAR2(256)     NULL,
    VERSION            NUMBER(38)    DEFAULT 0 NOT NULL,
    DATE_CREATED       TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED       TIMESTAMP(6)      NULL,
    MODIFIED_BY        VARCHAR2(40)      NULL
)
;
DROP TABLE UNIT_TREE
;
CREATE TABLE UNIT_TREE
(
    NODE_ID        NUMBER(19)     NOT NULL,
    PARENT_NODE_ID NUMBER(19)         NULL,
    UNIT_ID        NUMBER(19)     NOT NULL,
    UNIT           VARCHAR2(128)  NOT NULL,
    ABBREVIATION   VARCHAR2(20)       NULL,
    DESCRIPTION    VARCHAR2(1000)     NULL,
    IS_LEAF        CHAR(1)        NOT NULL,
    FULL_PATH      VARCHAR2(2500)     NULL
)
;
ALTER TABLE ASSAY_CONTEXT DROP CONSTRAINT FK_ASSAY_CONTEXT_ASSAY
;
ALTER TABLE ASSAY_CONTEXT DROP PRIMARY KEY DROP INDEX
;
DROP TABLE ASSAY_CONTEXT
;
CREATE TABLE ASSAY_CONTEXT
(
    ASSAY_CONTEXT_ID NUMBER(19)    NOT NULL,
    ASSAY_ID         NUMBER(19)    NOT NULL,
    CONTEXT_NAME     VARCHAR2(128)     NULL,
    CONTEXT_GROUP    VARCHAR2(256)     NULL,
    VERSION          NUMBER(38)    DEFAULT 0 NOT NULL,
    DISPLAY_ORDER    NUMBER(5)     DEFAULT 0 NOT NULL,
    DATE_CREATED     TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED     TIMESTAMP(6)      NULL,
    MODIFIED_BY      VARCHAR2(40)      NULL
)
;
COMMENT ON COLUMN ASSAY_CONTEXT.CONTEXT_NAME IS
'used as a title for the cards in the UI'
;
ALTER TABLE ASSAY_CONTEXT_ITEM DROP PRIMARY KEY DROP INDEX
;
ALTER TABLE ASSAY_CONTEXT_ITEM DROP CONSTRAINT CK_ASSAY_CONTEXT_ITEM_QALFR
;
ALTER TABLE ASSAY_CONTEXT_ITEM DROP CONSTRAINT CK_ATTRIBUTE_TYPE
;
DROP INDEX FK_A_CONTEXT_ITEM_QUALIFIER
;
DROP INDEX FK_A_CONTEXT_ITEM_ATTRIBUTE
;
DROP INDEX AK_ASSAY_CONTEXT_ITEM
;
DROP INDEX FK_A_CONTEXT_ITEM_A_CONTEXT
;
DROP TABLE ASSAY_CONTEXT_ITEM;
;
CREATE TABLE ASSAY_CONTEXT_ITEM
(
    ASSAY_CONTEXT_ITEM_ID NUMBER(19)    NOT NULL,
    ASSAY_CONTEXT_ID      NUMBER(19)    NOT NULL,
    DISPLAY_ORDER         NUMBER(5)     DEFAULT 0 NOT NULL,
    ATTRIBUTE_TYPE        VARCHAR2(20)  NOT NULL,
    ATTRIBUTE_ID          NUMBER(19)    NOT NULL,
    QUALIFIER             CHAR(2)           NULL,
    VALUE_ID              NUMBER(19)        NULL,
    EXT_VALUE_ID          VARCHAR2(60)      NULL,
    VALUE_DISPLAY         VARCHAR2(500)     NULL,
    VALUE_NUM             NUMBER(30,15)     NULL,
    VALUE_MIN             NUMBER(30,15)     NULL,
    VALUE_MAX             NUMBER(30,15)     NULL,
    VERSION               NUMBER(38)    DEFAULT 0 NOT NULL,
    DATE_CREATED          TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED          TIMESTAMP(6)      NULL,
    MODIFIED_BY           VARCHAR2(40)      NULL
)
;
COMMENT ON COLUMN ASSAY_CONTEXT_ITEM.VALUE_DISPLAY IS
'This is not a general text entry field, rather it is an easily displayable text version of the other value columns'
;
DROP TABLE ASSAY_DESCRIPTOR_TREE CASCADE CONSTRAINTS
;
CREATE TABLE ASSAY_DESCRIPTOR_TREE
(
    NODE_ID        NUMBER(19)     NOT NULL,
    PARENT_NODE_ID NUMBER(19)         NULL,
    ELEMENT_ID     NUMBER(19)     NOT NULL,
    ELEMENT_STATUS VARCHAR2(20)   NOT NULL,
    LABEL          VARCHAR2(128)  NOT NULL,
    IS_LEAF        CHAR(1)        NOT NULL,
    DESCRIPTION    VARCHAR2(1000)     NULL,
    FULL_PATH      VARCHAR2(2500)     NULL,
    ABBREVIATION   VARCHAR2(20)       NULL,
    SYNONYMS       VARCHAR2(1000)     NULL,
    EXTERNAL_URL   VARCHAR2(1000)     NULL,
    UNIT_ID        NUMBER(19)         NULL
)
;
DROP TABLE BIOLOGY_DESCRIPTOR_TREE CASCADE CONSTRAINTS
;
CREATE TABLE BIOLOGY_DESCRIPTOR_TREE
(
    NODE_ID        NUMBER(19)     NOT NULL,
    PARENT_NODE_ID NUMBER(19)         NULL,
    ELEMENT_ID     NUMBER(19)     NOT NULL,
    ELEMENT_STATUS VARCHAR2(20)   NOT NULL,
    LABEL          VARCHAR2(128)  NOT NULL,
    IS_LEAF        CHAR(1)        NOT NULL,
    DESCRIPTION    VARCHAR2(1000)     NULL,
    FULL_PATH      VARCHAR2(2500)     NULL,
    ABBREVIATION   VARCHAR2(20)       NULL,
    SYNONYMS       VARCHAR2(1000)     NULL,
    EXTERNAL_URL   VARCHAR2(1000)     NULL,
    UNIT_ID        NUMBER(19)         NULL
)
;
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
DROP TABLE ELEMENT;
;
CREATE TABLE ELEMENT
(
    ELEMENT_ID           NUMBER(19)     NOT NULL,
    ELEMENT_STATUS       VARCHAR2(20)   DEFAULT 'Pending' NOT NULL,
    LABEL                VARCHAR2(128)  NOT NULL,
    UNIT_ID              NUMBER(19)         NULL,
    ABBREVIATION         VARCHAR2(20)       NULL,
    BARD_URI             VARCHAR2(250)      NULL,
    DESCRIPTION          VARCHAR2(1000)     NULL,
    SYNONYMS             VARCHAR2(1000)     NULL,
    EXTERNAL_URL         VARCHAR2(1000)     NULL,
    READY_FOR_EXTRACTION VARCHAR2(20)   DEFAULT 'Pending' NOT NULL,
    VERSION              NUMBER(38)     DEFAULT 0 NOT NULL,
    DATE_CREATED         TIMESTAMP(6)   DEFAULT sysdate NOT NULL,
    LAST_UPDATED         TIMESTAMP(6)       NULL,
    MODIFIED_BY          VARCHAR2(40)       NULL,
    CONSTRAINT PK_ELEMENT PRIMARY KEY (ELEMENT_ID),
    CONSTRAINT FK_ELEMENT_UNIT FOREIGN KEY (UNIT_ID) REFERENCES ELEMENT(ELEMENT_ID) INITIALLY DEFERRED DEFERRABLE
)
;

DROP TABLE INSTANCE_DESCRIPTOR_TREE CASCADE CONSTRAINTS
;
CREATE TABLE INSTANCE_DESCRIPTOR_TREE
(
    NODE_ID        NUMBER(19)     NOT NULL,
    PARENT_NODE_ID NUMBER(19)         NULL,
    ELEMENT_ID     NUMBER(19)     NOT NULL,
    ELEMENT_STATUS VARCHAR2(20)   NOT NULL,
    LABEL          VARCHAR2(128)  NOT NULL,
    IS_LEAF        CHAR(1)        NOT NULL,
    DESCRIPTION    VARCHAR2(1000)     NULL,
    FULL_PATH      VARCHAR2(2500)     NULL,
    ABBREVIATION   VARCHAR2(20)       NULL,
    SYNONYMS       VARCHAR2(1000)     NULL,
    EXTERNAL_URL   VARCHAR2(1000)     NULL,
    UNIT_ID        NUMBER(19)         NULL
)
;
ALTER TABLE MEASURE DROP CONSTRAINT FK_MEASURE_ASSAY
;
--ALTER TABLE MEASURE DROP CONSTRAINT FK_MEASURE_A_CONTEXT
--;
ALTER TABLE MEASURE DROP CONSTRAINT FK_MEASURE_PARENT_MEASURE_ID
;
ALTER TABLE MEASURE DROP PRIMARY KEY DROP INDEX
;
DROP INDEX FK_MEASURE_RESULT_TYPE
;
DROP INDEX FK_MEASURE_A_CONTEXT
;
DROP INDEX FK_MEASURE_ELEMENT_UNIT
;
DROP INDEX FK_MEASURE_ASSAY
;
DROP INDEX FK_MEASURE_PARENT_MEASURE_ID
;
DROP TABLE MEASURE
;
CREATE TABLE MEASURE
(
    MEASURE_ID        NUMBER(19)   NOT NULL,
    ASSAY_ID          NUMBER(19)   NOT NULL,
    RESULT_TYPE_ID    NUMBER(19)   NOT NULL,
    PARENT_MEASURE_ID NUMBER(19)       NULL,
    ENTRY_UNIT_ID     NUMBER(19)       NULL,
    STATS_MODIFIER_ID NUMBER(19)       NULL,
    VERSION           NUMBER(38)   DEFAULT 0 NOT NULL,
    DATE_CREATED      TIMESTAMP(6) DEFAULT sysdate NOT NULL,
    LAST_UPDATED      TIMESTAMP(6)     NULL,
    MODIFIED_BY       VARCHAR2(40)     NULL
)
;
ALTER TABLE PROJECT_CONTEXT_ITEM DROP CONSTRAINT FK_PROJ_CONTEXT_ITEM_GROUP
;
ALTER TABLE PROJECT_CONTEXT_ITEM DROP CONSTRAINT FK_PROJ_CONTEXT_ITEM_PROJECT
;
ALTER TABLE PROJECT_CONTEXT_ITEM DROP CONSTRAINT FK_PROJ_CONTEXT_ITEM_STEP
;
ALTER TABLE PROJECT_CONTEXT_ITEM DROP PRIMARY KEY DROP INDEX
;
ALTER TABLE PROJECT_CONTEXT_ITEM DROP CONSTRAINT CK_PROJECT_STEP_ITEM_QUALIFIER
;
ALTER TABLE PROJECT_CONTEXT_ITEM DROP CONSTRAINT CK_PROJ_CONTEXT_ITEM_DISCRIM
;
DROP INDEX FK_PROJECT_STEP_ITEM_EXPRMNT
;
DROP INDEX FK_PROJECT_STEP_ITEM_ATTRIBUTE
;
DROP INDEX FK_PROJECT_STEP_ITEM_VALUE
;
DROP INDEX FK_PROJECT_STEP_ITEM_QUALIFIER
;
DROP INDEX FK_PROJECT_STEP_ITEM_GROUP
;
DROP TABLE PROJECT_CONTEXT_ITEM
;
CREATE TABLE PROJECT_CONTEXT_ITEM
(
    PROJECT_CONTEXT_ITEM_ID NUMBER(19)    NOT NULL,
    PROJECT_CONTEXT_ID      NUMBER(19)    NOT NULL,
    ATTRIBUTE_ID            NUMBER(19)    NOT NULL,
    DISPLAY_ORDER           NUMBER(5)     NOT NULL,
    VALUE_ID                NUMBER(19)        NULL,
    EXT_VALUE_ID            VARCHAR2(60)      NULL,
    QUALIFIER               CHAR(2)           NULL,
    VALUE_DISPLAY           VARCHAR2(500)     NULL,
    VALUE_NUM               NUMBER(30,15)     NULL,
    VALUE_MIN               NUMBER(30,15)     NULL,
    VALUE_MAX               NUMBER(30,15)     NULL,
    VERSION                 NUMBER(38)    DEFAULT 0 NOT NULL,
    DATE_CREATED            TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED            TIMESTAMP(6)      NULL,
    MODIFIED_BY             VARCHAR2(40)      NULL
)
;
COMMENT ON COLUMN PROJECT_CONTEXT_ITEM.VALUE_DISPLAY IS
'This is not a general text entry field, rather it is an easily displayable text version of the other value columns'
;
ALTER TABLE RESULT DROP CONSTRAINT FK_RESULT_EXPERIMENT
;
ALTER TABLE RESULT DROP PRIMARY KEY DROP INDEX
;
ALTER TABLE RESULT DROP CONSTRAINT CK_RESULT_EXTRACTION
;
ALTER TABLE RESULT DROP CONSTRAINT CK_RESULT_QUALIFIER
;
ALTER TABLE RESULT DROP CONSTRAINT CK_RESULT_STATUS
;
DROP INDEX FK_RESULT_EXPERIMENT
;
DROP INDEX FK_RESULT_RESULT_TYPE
;
DROP TABLE RESULT
;
CREATE TABLE RESULT
(
    RESULT_ID            NUMBER(19)    NOT NULL,
    RESULT_STATUS        VARCHAR2(20)  DEFAULT 'Pending' NOT NULL,
    READY_FOR_EXTRACTION VARCHAR2(20)  DEFAULT 'Pending' NOT NULL,
    EXPERIMENT_ID        NUMBER(19)    NOT NULL,
    RESULT_TYPE_ID       NUMBER(19)    NOT NULL,
    SUBSTANCE_ID         NUMBER(19)    NOT NULL,
    STATS_MODIFIER_ID    NUMBER (19)       NULL,
    REPLICATE_NO         NUMBER(5)         NULL,
    QUALIFIER            CHAR(2)           NULL,
    VALUE_NUM            NUMBER(30,15)     NULL,
    VALUE_MIN            NUMBER(30,15)     NULL,
    VALUE_MAX            NUMBER(30,15)     NULL,
    VALUE_DISPLAY        VARCHAR2(256)     NULL,
    VERSION              NUMBER(38)    DEFAULT 0 NOT NULL,
    DATE_CREATED         TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED         TIMESTAMP(6)      NULL,
    MODIFIED_BY          VARCHAR2(40)      NULL
)
;
COMMENT ON COLUMN RESULT.SUBSTANCE_ID IS
'Has external reference to the PubChem SID'
;
ALTER TABLE RESULT_HIERARCHY DROP PRIMARY KEY DROP INDEX
;
ALTER TABLE RESULT_HIERARCHY DROP CONSTRAINT CK_RESULT_HIERARCHY_TYPE
;
DROP INDEX FK_RESULT_HIERARCHY_RSLT_PRNT
;
DROP INDEX FK_RESULT_HIERARCHY_RESULT
;
DROP TABLE RESULT_HIERARCHY
;
CREATE TABLE RESULT_HIERARCHY
(
    RESULT_HIERARCHY_ID NUMBER(19)   NOT NULL,
    RESULT_ID           NUMBER(19)   NOT NULL,
    PARENT_RESULT_ID    NUMBER(19)   NOT NULL,
    HIERARCHY_TYPE      VARCHAR2(10) NOT NULL,
    VERSION             NUMBER(38)   DEFAULT 0 NOT NULL,
    DATE_CREATED        TIMESTAMP(6) DEFAULT sysdate NOT NULL,
    LAST_UPDATED        TIMESTAMP(6)     NULL,
    MODIFIED_BY         VARCHAR2(40)     NULL
)
;
COMMENT ON COLUMN RESULT_HIERARCHY.HIERARCHY_TYPE IS
'two types of hierarchy are allowed: parent;child where one result is dependant on or grouped with another; derived from where aresult is used to claculate another (e.g. PI used for IC50).  The hierarchy types are mutually exclusive.'
;
DROP TABLE RESULT_TYPE_TREE CASCADE CONSTRAINTS
;
CREATE TABLE RESULT_TYPE_TREE
(
    NODE_ID            NUMBER(19)     NOT NULL,
    PARENT_NODE_ID     NUMBER(19)         NULL,
    RESULT_TYPE_ID     NUMBER(19)     NOT NULL,
    RESULT_TYPE_STATUS VARCHAR2(20)   NOT NULL,
    RESULT_TYPE_NAME   VARCHAR2(128)  NOT NULL,
    IS_LEAF            CHAR(1)        NOT NULL,
    DESCRIPTION        VARCHAR2(1000)     NULL,
    FULL_PATH          VARCHAR2(2500)     NULL,
    ABBREVIATION       VARCHAR2(20)       NULL,
    SYNONYMS           VARCHAR2(1000)     NULL,
    BASE_UNIT_ID       NUMBER(19)         NULL
)
;
ALTER TABLE RUN_CONTEXT_ITEM DROP CONSTRAINT FK_R_CONTEXT_ITEM_EXPERIMENT
;
ALTER TABLE RUN_CONTEXT_ITEM DROP CONSTRAINT FK_R_CONTEXT_ITEM_GROUP
;
ALTER TABLE RUN_CONTEXT_ITEM DROP PRIMARY KEY DROP INDEX
;
ALTER TABLE RUN_CONTEXT_ITEM DROP CONSTRAINT CK_RUN_CONTEXT_ITEM_DISCRIM
;
ALTER TABLE RUN_CONTEXT_ITEM DROP CONSTRAINT CK_RUN_CONTEXT_ITEM_QUALFR
;
DROP INDEX FK_R_CONTEXT_ITEM_RESULT
;
DROP INDEX FK_R_CONTEXT_ITEM_QUALIFIER
;
DROP INDEX FK_R_CONTEXT_ITEM_VALUE
;
DROP INDEX FK_R_CONTEXT_ITEM_ATTRIBUTE
;
DROP INDEX FK_R_CONTEXT_ITEM_GROUP
;
DROP INDEX FK_R_CONTEXT_ITEM_EXPERIMENT
;
DROP TABLE RUN_CONTEXT_ITEM
;
CREATE TABLE RSLT_CONTEXT_ITEM
(
    RSLT_CONTEXT_ITEM_ID NUMBER(19)    NOT NULL,
    RESULT_ID            NUMBER(19)    NOT NULL,
    ATTRIBUTE_ID         NUMBER(19)    NOT NULL,
    VALUE_ID             NUMBER(19)        NULL,
    DISPLAY_ORDER        NUMBER(5)     DEFAULT 0 NOT NULL,
    EXT_VALUE_ID         VARCHAR2(60)      NULL,
    QUALIFIER            CHAR(2)           NULL,
    VALUE_NUM            NUMBER(30,15)     NULL,
    VALUE_MIN            NUMBER(30,15)     NULL,
    VALUE_MAX            NUMBER(30,15)     NULL,
    VALUE_DISPLAY        VARCHAR2(500)     NULL,
    VERSION              NUMBER(38)    DEFAULT 0 NOT NULL,
    DATE_CREATED         TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED         TIMESTAMP(6)      NULL,
    MODIFIED_BY          VARCHAR2(40)      NULL
)
;
COMMENT ON COLUMN RSLT_CONTEXT_ITEM.VALUE_DISPLAY IS
'This is not a general text entry field, rather it is an easily displayable text version of the other value columns'
;
CREATE TABLE STEP_CONTEXT
(
    STEP_CONTEXT_ID NUMBER(19)    NOT NULL,
    PROJECT_STEP_ID NUMBER(19)    NOT NULL,
    CONTEXT_NAME    VARCHAR2(128)     NULL,
    CONTEXT_GROUP   VARCHAR2(256)     NULL,
    DISPLAY_ORDER   NUMBER(5)     DEFAULT 0 NOT NULL,
    VERSION         NUMBER(38)    DEFAULT 0 NOT NULL,
    DATE_CREATED    TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED    TIMESTAMP(6)      NULL,
    MODIFIED_BY     VARCHAR2(40)      NULL
);
ALTER TABLE STEP_CONTEXT
    ADD CONSTRAINT PK_ASSAY_CONTEXT_2
    PRIMARY KEY (STEP_CONTEXT_ID)
    USING INDEX STORAGE(BUFFER_POOL DEFAULT)
    ENABLE
    VALIDATE
;
COMMENT ON COLUMN STEP_CONTEXT.CONTEXT_NAME IS
'used as a title for the cards in the UI'
;
CREATE TABLE STEP_CONTEXT_ITEM
(
    STEP_CONTEXT_ITEM_ID NUMBER(19)    NOT NULL,
    STEP_CONTEXT_ID      NUMBER(19)    NOT NULL,
    DISPLAY_ORDER        NUMBER(5)     NOT NULL,
    ATTRIBUTE_ID         NUMBER(19)    NOT NULL,
    VALUE_ID             NUMBER(19)        NULL,
    EXT_VALUE_ID         VARCHAR2(60)      NULL,
    QUALIFIER            CHAR(2)           NULL,
    VALUE_NUM            NUMBER(30,15)     NULL,
    VALUE_MIN            NUMBER(30,15)     NULL,
    VALUE_MAX            NUMBER(30,15)     NULL,
    VALUE_DISPLAY        VARCHAR2(500)     NULL,
    VERSION              NUMBER(38)    DEFAULT 0 NOT NULL,
    DATE_CREATED         TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED         TIMESTAMP(6)      NULL,
    MODIFIED_BY          VARCHAR2(40)      NULL
)
;
ALTER TABLE STEP_CONTEXT_ITEM
    ADD CONSTRAINT PK_STEP_CONTEXT_ITEM
    PRIMARY KEY (STEP_CONTEXT_ITEM_ID)
    USING INDEX STORAGE(BUFFER_POOL DEFAULT)
    ENABLE
    VALIDATE
;
ALTER TABLE STEP_CONTEXT_ITEM
    ADD CONSTRAINT CK_STEP_CONTEXT_ITEM_QUALIFIER
CHECK (Qualifier IN ('= ', '< ', '<=', '> ', '>=', '<<', '>>', '~ '))
;
COMMENT ON COLUMN STEP_CONTEXT_ITEM.VALUE_DISPLAY IS
'This is not a general text entry field, rather it is an easily displayable text version of the other value columns'
;
CREATE TABLE TEAM
(
    TEAM_ID      NUMBER(19)    NOT NULL,
    TEAM_NAME    VARCHAR2(255) NOT NULL,
    LOCATION     VARCHAR2(255)     NULL,
    VERSION      NUMBER(5)     DEFAULT 0 NOT NULL,
    DATE_CREATED DATE          DEFAULT sysdate     NULL,
    LAST_UPDATED DATE              NULL,
    MODIFIED_BY  VARCHAR2(40)      NULL
)
;
ALTER TABLE TEAM
    ADD CONSTRAINT PK_TEAM
    PRIMARY KEY (TEAM_ID)
    USING INDEX STORAGE(BUFFER_POOL DEFAULT)
    ENABLE
    VALIDATE
;
CREATE TABLE TEAM_MEMBER
(
    TEAM_MEMBER_ID    NUMBER(19)   NOT NULL,
    TEAM_ID           NUMBER(19)   NOT NULL,
    PERSON_ROLE_ID    NUMBER(19)   NOT NULL,
    MEMBERSHIP_STATUS VARCHAR2(20) DEFAULT 'Active'     NULL,
    VERSION           NUMBER(5)    DEFAULT 0 NOT NULL,
    DATE_CREATED      DATE         DEFAULT sysdate     NULL,
    LAST_UPDATED      DATE             NULL,
    MODIFIED_BY       VARCHAR2(40)     NULL
)
;
ALTER TABLE TEAM_MEMBER
    ADD CONSTRAINT PK_TEAM_MEMBER
    PRIMARY KEY (TEAM_MEMBER_ID)
    USING INDEX STORAGE(BUFFER_POOL DEFAULT)
    ENABLE
    VALIDATE
;
ALTER TABLE TEAM_MEMBER
    ADD CONSTRAINT CK_TEAM_MEMBER_STATUS
CHECK (Membership_Status IN ('Active', 'Suspended', 'Inactive'))
;
CREATE TABLE ASSAY_CONTEXT_MEASURE
(
    ASSAY_CONTEXT_MEASURE_ID NUMBER(19)   NOT NULL,
    ASSAY_CONTEXT_ID         NUMBER(19)   NOT NULL,
    MEASURE_ID               NUMBER(19)   NOT NULL,
    VERSION                  NUMBER(38)   DEFAULT 0 NOT NULL,
    DATE_CREATED             TIMESTAMP(6) DEFAULT sysdate NOT NULL,
    LAST_UPDATED             TIMESTAMP(6)     NULL,
    MODIFIED_BY              VARCHAR2(40)     NULL
)
;
ALTER TABLE ASSAY_CONTEXT_MEASURE
    ADD CONSTRAINT PK_ASSAY_CONTEXT_MEASURE
    PRIMARY KEY (ASSAY_CONTEXT_MEASURE_ID)
    USING INDEX STORAGE(BUFFER_POOL DEFAULT)
    ENABLE
    VALIDATE
;
CREATE TABLE EXPRMT_CONTEXT
(
    EXPRMT_CONTEXT_ID NUMBER(19)    NOT NULL,
    EXPERIMENT_ID     NUMBER(19)    NOT NULL,
    CONTEXT_NAME      VARCHAR2(128)     NULL,
    CONTEXT_GROUP     VARCHAR2(256)     NULL,
    DISPLAY_ORDER     NUMBER(5)     DEFAULT 0 NOT NULL,
    VERSION           NUMBER(38)    DEFAULT 0 NOT NULL,
    DATE_CREATED      TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED      TIMESTAMP(6)      NULL,
    MODIFIED_BY       VARCHAR2(40)      NULL
)
;
ALTER TABLE EXPRMT_CONTEXT
    ADD CONSTRAINT PK_EXPRMT_CONTEXT
    PRIMARY KEY (EXPRMT_CONTEXT_ID)
    USING INDEX STORAGE(BUFFER_POOL DEFAULT)
    ENABLE
    VALIDATE
;
COMMENT ON COLUMN EXPRMT_CONTEXT.CONTEXT_NAME IS
'used as a title for the cards in the UI'
;
CREATE TABLE EXPRMT_CONTEXT_ITEM
(
    EXPRMT_CONTEXT_ITEM_ID NUMBER(19)    NOT NULL,
    EXPRMT_CONTEXT_ID      NUMBER(19)    NOT NULL,
    DISPLAY_ORDER          NUMBER(5)     DEFAULT 0 NOT NULL,
    ATTRIBUTE_ID           NUMBER(19)    NOT NULL,
    VALUE_ID               NUMBER(19)        NULL,
    EXT_VALUE_ID           VARCHAR2(60)      NULL,
    QUALIFIER              CHAR(2)           NULL,
    VALUE_NUM              NUMBER(30,15)     NULL,
    VALUE_MIN              NUMBER(30,15)     NULL,
    VALUE_MAX              NUMBER(30,15)     NULL,
    VALUE_DISPLAY          VARCHAR2(500)     NULL,
    VERSION                NUMBER(38)    DEFAULT 0 NOT NULL,
    DATE_CREATED           TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED           TIMESTAMP(6)      NULL,
    MODIFIED_BY            VARCHAR2(40)      NULL
)
;
ALTER TABLE EXPRMT_CONTEXT_ITEM
    ADD CONSTRAINT PK_EXPRMT_CONTEXT_ITEM
    PRIMARY KEY (EXPRMT_CONTEXT_ITEM_ID)
    ENABLE
    VALIDATE
;
ALTER TABLE EXPRMT_CONTEXT_ITEM
    ADD CONSTRAINT CK_EXPRMT_CNTXT_ITM_QUALFR
CHECK (Qualifier IN ('= ', '< ', '<=', '> ', '>=', '<<', '>>', '~ '))
;
COMMENT ON COLUMN EXPRMT_CONTEXT_ITEM.VALUE_DISPLAY IS
'This is not a general text entry field, rather it is an easily displayable text version of the other value columns'
;
CREATE TABLE FAVORITE
(
    FAVORITE_ID   NUMBER(19)     NOT NULL,
    PERSON_ID     NUMBER(19)     NOT NULL,
    FAVORITE_URL  VARCHAR2(1000)     NULL,
    FAVORITE_TYPE VARCHAR2(20)   DEFAULT 'Favorite' NOT NULL,
    FAVORITE_NAME VARCHAR2(255)  NOT NULL,
    DISPLAY_ORDER NUMBER(5)      DEFAULT 0 NOT NULL,
    VERSION       NUMBER(5)      DEFAULT 0 NOT NULL,
    DATE_CREATED  DATE           DEFAULT sysdate     NULL,
    LAST_UPDATED  DATE               NULL,
    MODIFIED_BY   VARCHAR2(40)       NULL
)
;
ALTER TABLE FAVORITE
    ADD CONSTRAINT PK_FAVORITE
    PRIMARY KEY (FAVORITE_ID)
    ENABLE
    VALIDATE
;
ALTER TABLE FAVORITE
    ADD CONSTRAINT CK_FAVORITE_TYPE
CHECK (Favorite_Type IN ('Favorite', 'Recently Used'))
;
CREATE TABLE PERSON
(
    PERSON_ID        NUMBER(19)    NOT NULL,
    USERNAME         VARCHAR2(255)     NULL,
    ACCOUNT_EXPIRED  NUMBER(1)     DEFAULT 0 NOT NULL,
    ACCOUNT_LOCKED   NUMBER(1)     DEFAULT 0 NOT NULL,
    ACCOUNT_ENABLED  NUMBER(1)     DEFAULT 1 NOT NULL,
    PASSWORD         VARCHAR2(255) NOT NULL,
    PASSWORD_EXPIRED NUMBER(1)     DEFAULT 0 NOT NULL,
    VERSION          NUMBER(5)     DEFAULT 0 NOT NULL,
    DATE_CREATED     DATE          DEFAULT sysdate     NULL,
    LAST_UPDATED     DATE              NULL,
    MODIFIED_BY      VARCHAR2(40)      NULL
)
;
ALTER TABLE PERSON
    ADD CONSTRAINT PK_PERSON
    PRIMARY KEY (PERSON_ID)
    USING INDEX STORAGE(BUFFER_POOL DEFAULT)
    ENABLE
    VALIDATE
;
CREATE TABLE PERSON_ROLE
(
    PERSON_ROLE_ID NUMBER(19)   NOT NULL,
    ROLE_ID        NUMBER(19)   NOT NULL,
    PERSON_ID      NUMBER(19)   NOT NULL,
    VERSION        NUMBER(5)    DEFAULT 0 NOT NULL,
    DATE_CREATED   DATE         DEFAULT sysdate     NULL,
    LAST_UPDATED   DATE             NULL,
    MODIFIED_BY    VARCHAR2(40)     NULL
)
;
ALTER TABLE PERSON_ROLE
    ADD CONSTRAINT PK_PERSON_ROLE
    PRIMARY KEY (PERSON_ROLE_ID)
    USING INDEX STORAGE(BUFFER_POOL DEFAULT)
    ENABLE
    VALIDATE
;
CREATE TABLE PROJECT_CONTEXT
(
    PROJECT_CONTEXT_ID NUMBER(19)    NOT NULL,
    PROJECT_ID         NUMBER(19)    NOT NULL,
    CONTEXT_NAME       VARCHAR2(128)     NULL,
    CONTEXT_GROUP      VARCHAR2(256)     NULL,
    DISPLAY_ORDER      NUMBER(5)     DEFAULT 0 NOT NULL,
    VERSION            NUMBER(38)    DEFAULT 0 NOT NULL,
    DATE_CREATED       TIMESTAMP(6)  DEFAULT sysdate NOT NULL,
    LAST_UPDATED       TIMESTAMP(6)      NULL,
    MODIFIED_BY        VARCHAR2(40)      NULL
)
;
ALTER TABLE PROJECT_CONTEXT
    ADD CONSTRAINT PK_PROJECT_CONTEXT
    PRIMARY KEY (PROJECT_CONTEXT_ID)
    USING INDEX STORAGE(BUFFER_POOL DEFAULT)
    ENABLE
    VALIDATE
;
COMMENT ON COLUMN PROJECT_CONTEXT.CONTEXT_NAME IS
'used as a title for the cards in the UI'
;
CREATE TABLE PROJECT_DOCUMENT
(
    PROJECT_DOCUMENT_ID NUMBER(19)    NOT NULL,
    PROJECT_ID          NUMBER(19)    NOT NULL,
    DOCUMENT_NAME       VARCHAR2(500) NOT NULL,
    DOCUMENT_TYPE       VARCHAR2(20)  NOT NULL,
    DOCUMENT_CONTENT    CLOB              NULL,
    VERSION             NUMBER(38)    NOT NULL,
    DATE_CREATED        TIMESTAMP(6)  NOT NULL,
    LAST_UPDATED        TIMESTAMP(6)      NULL,
    MODIFIED_BY         VARCHAR2(40)      NULL
)
;
ALTER TABLE PROJECT_DOCUMENT
    ADD CONSTRAINT PK_PROJECT_DOCUMENT
    PRIMARY KEY (PROJECT_DOCUMENT_ID)
    ENABLE
    VALIDATE
;
ALTER TABLE PROJECT_DOCUMENT
    ADD CONSTRAINT CK_PROJECT_DOCUMENT_TYPE
CHECK (DOCUMENT_TYPE IN ('Description', 'Protocol', 'Comments', 'Paper', 'External URL', 'Other'))
;
CREATE TABLE ROLE
(
    ROLE_ID      NUMBER(19)    NOT NULL,
    AUTHORITY    VARCHAR2(255) NOT NULL,
    VERSION      NUMBER(5)     DEFAULT 0 NOT NULL,
    DATE_CREATED DATE          DEFAULT sysdate     NULL,
    LAST_UPDATED DATE              NULL,
    MODIFIED_BY  VARCHAR2(40)      NULL
)
;
ALTER TABLE ROLE
    ADD CONSTRAINT PK_ROLE
    PRIMARY KEY (ROLE_ID)
    USING INDEX STORAGE(BUFFER_POOL DEFAULT)
    ENABLE
    VALIDATE
;
INSERT INTO ROLE (role_id, authority) values (role_id_seq.nextval, 'Viewer')
;
INSERT INTO ROLE (role_id, authority) values (role_id_seq.nextval, 'BARD Administrator')
;
INSERT INTO ROLE (role_id, authority) values (role_id_seq.nextval, 'Dictionary Manager')
;
INSERT INTO ROLE (role_id, authority) values (role_id_seq.nextval, 'Assay Registrar')
;
INSERT INTO ROLE (role_id, authority) values (role_id_seq.nextval, 'Results Depositor')
;
INSERT INTO ROLE (role_id, authority) values (role_id_seq.nextval, 'Project Manager')
;