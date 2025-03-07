ALTER TABLE MEASURE DROP CONSTRAINT CK_MEASURE_PC_RELATIONSHIP
;
UPDATE MEASURE SET PARENT_CHILD_RELATIONSHIP='calculated from' where PARENT_CHILD_RELATIONSHIP='is calculated from'
;
UPDATE MEASURE SET PARENT_CHILD_RELATIONSHIP='supported by' where PARENT_CHILD_RELATIONSHIP='is related to'
;
ALTER TABLE MEASURE
    ADD CONSTRAINT CK_MEASURE_PC_RELATIONSHIP
CHECK (Parent_Child_Relationship in ('calculated from', 'supported by'))
;
ALTER TABLE EXPRMT_MEASURE DROP CONSTRAINT CK_EXPRMT_MEASURE_RELATIONSHIP
;
UPDATE EXPRMT_MEASURE SET PARENT_CHILD_RELATIONSHIP='calculated from' where PARENT_CHILD_RELATIONSHIP='is calculated from'
;
UPDATE EXPRMT_MEASURE SET PARENT_CHILD_RELATIONSHIP='supported by' where PARENT_CHILD_RELATIONSHIP='is related to'
;
ALTER TABLE EXPRMT_MEASURE ADD CONSTRAINT CK_EXPRMT_MEASURE_RELATIONSHIP CHECK (PARENT_CHILD_RELATIONSHIP in ('supported by', 'calculated from'))
;
ALTER TABLE RESULT_HIERARCHY DROP CONSTRAINT CK_RESULT_HIERARCHY_TYPE
;
UPDATE RESULT_HIERARCHY SET HIERARCHY_TYPE='calculated from' where HIERARCHY_TYPE='is calculated from'
;
UPDATE RESULT_HIERARCHY SET HIERARCHY_TYPE='supported by' where HIERARCHY_TYPE='is related to'
;
ALTER TABLE RESULT_HIERARCHY ADD CONSTRAINT CK_RESULT_HIERARCHY_TYPE CHECK (HIERARCHY_TYPE in ('supported by', 'calculated from'))
;

