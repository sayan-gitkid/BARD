package barddataexport.dictionary

import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

class DictionaryExportHelperService {
    GrailsApplication grailsApplication
    LinkGenerator grailsLinkGenerator

    /**
     * Root node for generating the entire dictionary
     * @return dictionary as XML
     */
    public void generateDictionary(final Sql sql, final MarkupBuilder xml) {
        xml.dictionary() {
            generateElements(sql, xml)
            generateElementHierarchies(sql, xml)
            generateResultTypes(sql, xml)
            generateStages(sql, xml)
            generateDescriptors(sql, xml, [DescriptorType.BIOLOGY_DESCRIPTOR, DescriptorType.ASSAY_DESCRIPTOR, DescriptorType.INSTANCE_DESCRIPTOR])
            generateLabs(sql, xml)
            generateUnits(sql, xml)
        }
    }
    /**
     * Root node for generating all the descriptors
     * @param sql
     * @param xml
     * @param descriptorType
     */
    public void generateDescriptors(final Sql sql, final MarkupBuilder xml, final List<DescriptorType> descriptorTypes) {
        for (DescriptorType descriptorType in descriptorTypes) {
            switch (descriptorType) {
                case DescriptorType.BIOLOGY_DESCRIPTOR:
                    xml.biologyDescriptors() {
                        generateDescriptors(sql, xml, descriptorType)
                    }
                    break
                case DescriptorType.ASSAY_DESCRIPTOR:
                    xml.assayDescriptors() {
                        generateDescriptors(sql, xml, descriptorType)
                    }
                    break
                case DescriptorType.INSTANCE_DESCRIPTOR:
                    xml.instanceDescriptors() {
                        generateDescriptors(sql, xml, descriptorType)
                    }
                    break
                default:
                    throw new RuntimeException("Unsupported Descriptor Type ${descriptorType}")
            }
        }
    }
    /**
     * Root node for generating XML for a specific descriptor type (instanceDescriptor)
     *
     * @param sql
     * @param xml
     * @param descriptorType
     */
    public void generateDescriptors(final Sql sql, final MarkupBuilder xml, final DescriptorType descriptorType) {

        final String selectQuery = "SELECT NODE_ID,PARENT_NODE_ID,ELEMENT_ID,LABEL,DESCRIPTION,ABBREVIATION,SYNONYMS,EXTERNAL_URL,UNIT,ELEMENT_STATUS FROM ${descriptorType}"

        sql.eachRow(selectQuery) { descriptionRow ->
            final DescriptorDTO descriptorDTO =
                new DescriptorDTO(
                        descriptionRow.PARENT_NODE_ID,
                        descriptionRow.NODE_ID,
                        descriptionRow.ELEMENT_ID,
                        descriptionRow.LABEL,
                        descriptionRow.DESCRIPTION,
                        descriptionRow.ABBREVIATION,
                        descriptionRow.SYNONYMS,
                        descriptionRow.EXTERNAL_URL,
                        descriptionRow.UNIT,
                        descriptionRow.ELEMENT_STATUS
                )
            generateDescriptor(xml, descriptorDTO, descriptorType)
        }
    }

    /**
     * Generate a descriptor element for the given descriptorType
     * @param xml
     * @param descriptorDTO
     * @param descriptorType
     */
    public void generateDescriptor(final MarkupBuilder xml, final DescriptorDTO descriptorDTO, final DescriptorType descriptorType) {
        final Map<String, String> attributes = generateAttributesForDescriptor(descriptorDTO);
        switch (descriptorType) {
            case DescriptorType.BIOLOGY_DESCRIPTOR:
                xml.biologyDescriptor(attributes) {
                    generateSingleDescriptor(xml, descriptorDTO)
                }
                break
            case DescriptorType.ASSAY_DESCRIPTOR:
                xml.assayDescriptor(attributes) {
                    generateSingleDescriptor(xml, descriptorDTO)
                }
                break
            case DescriptorType.INSTANCE_DESCRIPTOR:
                xml.instanceDescriptor(attributes) {
                    generateSingleDescriptor(xml, descriptorDTO)
                }
                break
            default:
                throw new RuntimeException("Unknown Descriptor Type ${descriptorType}")
        }
    }

    /**
     * Generate result type element for a given dto
     *
     * <resultType> </resultType>
     * @param xml
     * @param resultTypeDTO
     */
    public void generateResultType(final MarkupBuilder xml, final ResultTypeDTO resultTypeDTO) {
        final Map<String, String> attributes = generateAttributesForResultType(resultTypeDTO)

        xml.resultType(attributes) {

            if (resultTypeDTO.resultTypeName) {
                resultTypeName(resultTypeDTO.resultTypeName)
            }
            if (resultTypeDTO.description) {
                description(resultTypeDTO.description)
            }
            if (resultTypeDTO.synonyms) {
                synonyms(resultTypeDTO.synonyms)
            }
        }
    }

    /**
     * Generate a single stage from the given parameters
     * @param xml
     * @param stageId
     * @param parentStageId
     * @param stage
     * @param descriptionText
     * @param stageStatus
     */
    public void generateStage(final MarkupBuilder xml,
                              final BigDecimal stageId,
                              final BigDecimal parentStageId,
                              final String stage,
                              final String descriptionText,
                              final String stageStatus) {

        final Map<String, String> attributes = generateAttributesForStage(stageId, parentStageId, stageStatus)
        xml.stage(attributes) {
            if (stage) {
                stageName(stage)
            }
            if (descriptionText) {
                description(descriptionText)
            }
        }
    }
    /**
     *  Generate element from a dto
     * @param xml
     * @param elementDTO
     *
     * <element></element>
     */
    public void generateElement(final MarkupBuilder xml, final ElementDTO elementDTO) {
        final Map<String, String> attributes = [:]


        attributes.put('elementId', elementDTO.elementId.toString())
        attributes.put('readyForExtraction', elementDTO.readyForExtraction)
        if (elementDTO.elementStatus) {
            attributes.put('elementStatus', elementDTO.elementStatus)
        }
        if (elementDTO.abbreviation) {
            attributes.put('abbreviation', elementDTO.abbreviation)
        }

        if (elementDTO.unit) {
            attributes.put('unit', elementDTO.unit)
        }
        xml.element(attributes) {
            if (elementDTO.label) {
                label(elementDTO.label)
            }
            if (elementDTO.description) {
                description(elementDTO.description)
            }
            if (elementDTO.synonyms) {
                synonyms(elementDTO.synonyms)
            }
            if (elementDTO.externalUrl) {
                externalUrl(elementDTO.externalUrl)
            }
            //now generate links for editing the element
            //clients can use this link to indicate that they have consumed the element
            final String ELEMENT_MEDIA_TYPE = grailsApplication.config.bard.data.export.dictionary.element.xml
            final String elementHref = grailsLinkGenerator.link(mapping: 'element', absolute: true, params: [id: elementDTO.elementId]).toString()
            link(rel: 'edit', href: "${elementHref}", type: "${ELEMENT_MEDIA_TYPE}")

        }
    }
    /**
     * Generate an element from an elementId
     * <element elementId=""></element>
     * @param sql
     * @param xml
     * @param elementId
     */
    public void generateElementWithElementId(final Sql sql, final MarkupBuilder xml, final BigDecimal elementId) {
        sql.eachRow('SELECT ELEMENT_ID, LABEL,DESCRIPTION,ABBREVIATION,SYNONYMS, EXTERNAL_URL,UNIT, ELEMENT_STATUS, READY_FOR_EXTRACTION FROM ELEMENT WHERE ELEMENT_ID=' + elementId) { elementRow ->


            final ElementDTO elementDTO =
                new ElementDTO(
                        elementRow.ELEMENT_ID,
                        elementRow.LABEL,
                        elementRow.DESCRIPTION,
                        elementRow.ABBREVIATION,
                        elementRow.SYNONYMS,
                        elementRow.EXTERNAL_URL,
                        elementRow.UNIT,
                        elementRow.ELEMENT_STATUS,
                        elementRow.READY_FOR_EXTRACTION)
            generateElement(xml, elementDTO)
        }
    }
    /**
     * Root Node for generating all elements
     * @param sql
     * @param xml
     */
    public void generateElements(final Sql sql, final MarkupBuilder xml) {

        xml.elements() {
            sql.eachRow('SELECT ELEMENT_ID FROM ELEMENT') { elementRow ->
                final BigDecimal elementId = elementRow.ELEMENT_ID
                generateElementWithElementId(sql, xml, elementId)
            }
        }
    }
    /**
     * Root node for generating element hierarchies
     * @param sql
     * @param xml
     */
    public void generateElementHierarchies(final Sql sql, final MarkupBuilder xml) {
        xml.elementHierarchies() {
            sql.eachRow('SELECT ELEMENT_HIERARCHY_ID,PARENT_ELEMENT_ID,CHILD_ELEMENT_ID, RELATIONSHIP_TYPE FROM ELEMENT_HIERARCHY') { elementHierarchyRow ->
                generateElementHierarchy(xml, elementHierarchyRow.ELEMENT_HIERARCHY_ID,
                        elementHierarchyRow.PARENT_ELEMENT_ID, elementHierarchyRow.CHILD_ELEMENT_ID, elementHierarchyRow.RELATIONSHIP_TYPE)
            }
        }
    }
    /**
     *
     * @param sql
     * @param xml
     * @param resultTypeId
     */
    public void generateResultType(final Sql sql, final MarkupBuilder xml, final BigDecimal resultTypeId) {
        sql.eachRow('SELECT NODE_ID,PARENT_NODE_ID,RESULT_TYPE_ID,RESULT_TYPE_NAME,DESCRIPTION,ABBREVIATION,SYNONYMS,BASE_UNIT,RESULT_TYPE_STATUS FROM RESULT_TYPE WHERE RESULT_TYPE_ID=' + resultTypeId) { row ->

            final BigDecimal parentNodeId = row.PARENT_NODE_ID
            BigDecimal resultTypeParentId = null
            sql.eachRow('select RESULT_TYPE_ID FROM RESULT_TYPE WHERE NODE_ID=' + parentNodeId) { parentResultTypeRow ->
                resultTypeParentId = parentResultTypeRow.RESULT_TYPE_ID
            }
            final ResultTypeDTO resultTypeDTO =
                new ResultTypeDTO(
                        resultTypeParentId,
                        row.RESULT_TYPE_ID,
                        row.RESULT_TYPE_NAME,
                        row.DESCRIPTION,
                        row.ABBREVIATION,
                        row.SYNONYMS,
                        row.BASE_UNIT,
                        row.RESULT_TYPE_STATUS
                )
            generateResultType(xml, resultTypeDTO)
        }

    }
    /**
     * Generate stages
     * @param sql
     * @param xml
     */
    public void generateStages(final Sql sql, final MarkupBuilder xml) {
        xml.stages() {
            sql.eachRow('SELECT STAGE_ID FROM STAGE') { row ->
                generateStage(sql, xml, row.STAGE_ID)
            }
        }
    }
    /**
     * Generate stages
     * @param sql
     * @param xml
     * @param stageId
     */
    public void generateStage(final Sql sql, final MarkupBuilder xml, final BigDecimal stageId) {
        sql.eachRow('SELECT STAGE_ID, NODE_ID, PARENT_NODE_ID,STAGE,DESCRIPTION, STAGE_STATUS FROM STAGE WHERE STAGE_ID=' + stageId) { row ->

            final BigDecimal parentNodeId = row.PARENT_NODE_ID
            BigDecimal stageParentId = null
            sql.eachRow('SELECT STAGE_ID FROM STAGE WHERE NODE_ID=' + parentNodeId) { parentStageRow ->
                stageParentId = parentStageRow.STAGE_ID
            }
            generateStage(xml, row.STAGE_ID, stageParentId, row.STAGE, row.DESCRIPTION, row.STAGE_STATUS)
        }
    }
    /**
     * Generate Result Types
     * @param sql
     * @param xml
     */
    public void generateResultTypes(final Sql sql, final MarkupBuilder xml) {

        xml.resultTypes() {
            sql.eachRow('SELECT RESULT_TYPE_ID FROM RESULT_TYPE') { row ->
                final BigDecimal resultTypeId = row.RESULT_TYPE_ID
                generateResultType(sql, xml, resultTypeId)
            }
        }
    }
    /**
     *
     * @param xml
     * @param elementHierarchyId
     * @param parentElementId
     * @param childElementId
     * @param relationshipTypeValue
     */
    public void generateElementHierarchy(final MarkupBuilder xml,
                                         final BigDecimal elementHierarchyId,
                                         final BigDecimal parentElementId,
                                         final BigDecimal childElementId,
                                         final BigDecimal relationshipTypeValue) {

        final Map<String, String> attributes = generateAttributesForElementHierarchy(elementHierarchyId, parentElementId, childElementId)
        xml.elementHierarchy(attributes) {
            if (relationshipTypeValue) {
                relationshipType(relationshipTypeValue)
            }
        }
    }
    /**
     *
     * @param sql
     * @param xml
     */
    public void generateLabs(final Sql sql, MarkupBuilder xml) {
        xml.laboratories() {
            sql.eachRow('select LABORATORY_ID,LABORATORY, DESCRIPTION, PARENT_NODE_ID,LABORATORY_STATUS  FROM LABORATORY') { labRow ->
                BigDecimal parentLabId = null
                sql.eachRow('SELECT LABORATORY_ID FROM LABORATORY WHERE NODE_ID=' + labRow.PARENT_NODE_ID) { parentLaboratoryRow ->
                    parentLabId = parentLaboratoryRow.LABORATORY_ID
                }
                final LaboratoryDTO laboratoryDTO = new LaboratoryDTO(labRow.LABORATORY_ID, parentLabId, labRow.DESCRIPTION, labRow.LABORATORY, labRow.LABORATORY_STATUS)
                generateLab(xml, laboratoryDTO)
            }
        }
    }
    /**
     *
     * @param sql
     * @param xml
     */
    public void generateUnits(final Sql sql, final MarkupBuilder xml) {
        xml.units() {
            sql.eachRow('SELECT UNIT_ID, NODE_ID,PARENT_NODE_ID,UNIT,DESCRIPTION FROM UNIT') { row ->

                final BigDecimal parentNodeId = row.PARENT_NODE_ID
                BigDecimal parentUnitId = null
                if (parentNodeId) {
                    sql.eachRow('SELECT UNIT_ID FROM UNIT WHERE NODE_ID=' + parentNodeId) { parentUnitRow ->
                        parentUnitId = parentUnitRow.UNIT_ID
                    }
                }
                final BigDecimal unitId = new BigDecimal(row.UNIT_ID.toString())
                final String unit = row.UNIT
                final String descriptionText = row.DESCRIPTION
                final UnitDTO unitDTO = new UnitDTO(unitId, parentUnitId, unit, descriptionText)
                generateUnit(xml, unitDTO)
            }
        }
    }
    /**
     *
     * @param xml
     * @param laboratoryDTO
     */
    public void generateLab(final MarkupBuilder xml, final LaboratoryDTO laboratoryDTO) {
        Map<String, String> attributes = [:]
        attributes.put('laboratoryId', laboratoryDTO.labId.toString())

        if (laboratoryDTO.parentLabId) {
            attributes.put('parentLaboratoryId', laboratoryDTO.parentLabId.toString())
        }
        if (laboratoryDTO.laboratoryStatus) {
            attributes.put('laboratoryStatus', laboratoryDTO.laboratoryStatus)
        }
        xml.laboratory(attributes) {
            laboratoryName(laboratoryDTO.labName)
            description(laboratoryDTO.description)
        }
    }
    /**
     *
     * @param xml
     * @param unitId
     * @param parentUnitId
     * @param unit
     * @param descriptionText
     */
    public void generateUnit(
            final MarkupBuilder xml,
            final UnitDTO unitDTO
    ) {
        final Map<String, String> attributes = generateAttributesForUnit(unitDTO.unitId, unitDTO.parentUnitId, unitDTO.unit)

        xml.unit(attributes) {

            if (unitDTO.descriptionText) {
                description(unitDTO.descriptionText)
            }
        }
    }
    /**
     * Generate a single descriptor element from a DTO
     *
     * <biologyDescriptor></biologyDescriptor>
     * @param xml
     * @param descriptorDTO
     */
    public void generateSingleDescriptor(final MarkupBuilder xml, final DescriptorDTO descriptorDTO) {

        if (descriptorDTO.elementStatus) {
            xml.elementStatus(descriptorDTO.elementStatus)
        }
        if (descriptorDTO.label) {
            xml.label(descriptorDTO.label)
        }
        if (descriptorDTO.description) {
            xml.description(descriptorDTO.description)
        }
        if (descriptorDTO.synonyms) {
            xml.synonyms(descriptorDTO.synonyms)
        }
    }
    /**
     *  Attributes for a Stage
     * @param stageId
     * @param parentStageId
     * @param stageStatus
     * @return Map for attribute
     */
    public Map<String, String> generateAttributesForStage(final BigDecimal stageId, final BigDecimal parentStageId, final String stageStatus) {
        final Map<String, String> attributes = [:]

        if (stageId) {
            attributes.put('stageId', stageId.toString())
        }

        if (parentStageId) {
            attributes.put('parentStageId', parentStageId.toString())
        }
        if (stageStatus) {
            attributes.put('stageStatus', stageStatus)
        }
        return attributes
    }
    /**
     *
     * @param elementHierarchyId
     * @param parentElementId
     * @param childElementId
     * @return Map
     */
    public Map<String, String> generateAttributesForElementHierarchy(final BigDecimal elementHierarchyId, final BigDecimal parentElementId, final BigDecimal childElementId) {
        final Map<String, String> attributes = [:]

        if (elementHierarchyId) {
            attributes.put('elementHierarchyId', elementHierarchyId.toString())
        }
        if (parentElementId) {
            attributes.put('parentElementId', parentElementId.toString())
        }
        if (childElementId) {
            attributes.put('childElementId', childElementId.toString())
        }
        return attributes
    }
    /**
     *
     * @param unitId
     * @param parentUnitId
     * @param unit
     * @return Map
     */
    public Map<String, String> generateAttributesForUnit(final BigDecimal unitId,
                                                         final BigDecimal parentUnitId,
                                                         final String unit) {
        final Map<String, String> attributes = [:]
        if (unitId) {
            attributes.put('unitId', unitId.toString())
        }

        if (parentUnitId) {
            attributes.put('parentUnitId', parentUnitId.toString())
        }
        if (unit) {
            attributes.put('unit', unit)
        }
        return attributes
    }
    /**
     *
     * @param resultTypeDTO
     * @return Map for attributes
     */
    public Map<String, String> generateAttributesForResultType(final ResultTypeDTO resultTypeDTO) {
        final Map<String, String> attributes = [:]

        if (resultTypeDTO.resultTypeId) {
            attributes.put("resultTypeId", resultTypeDTO.resultTypeId.toString())
        }


        if (resultTypeDTO.parentResultTypeId) {
            attributes.put("parentResultTypeId", resultTypeDTO.parentResultTypeId.toString())
        }
        if (resultTypeDTO.abbreviation) {
            attributes.put("abbreviation", resultTypeDTO.abbreviation)
        }

        if (resultTypeDTO.baseUnit) {
            attributes.put("baseUnit", resultTypeDTO.baseUnit)
        }
        if (resultTypeDTO.status) {
            attributes.put("resultTypeStatus", resultTypeDTO.status)
        }
        return attributes
    }
    /**
     * Generate Attributes as a Map for a descriptor element
     * @param descriptorDTO
     * @return key value pairs to be used as attributes for element
     */
    public Map<String, String> generateAttributesForDescriptor(final DescriptorDTO descriptorDTO) {
        final Map<String, String> attributes = [:]

        //use toString because map expects a string key and value
        attributes.put('descriptorId', descriptorDTO.descriptorId.toString())

        if (descriptorDTO.parentDescriptorId) {
            attributes.put('parentDescriptorId', descriptorDTO.parentDescriptorId.toString())
        }
        if (descriptorDTO.elementId) {
            attributes.put('elementId', descriptorDTO.elementId.toString())
        }
        if (descriptorDTO.abbreviation) {
            attributes.put('abbreviation', descriptorDTO.abbreviation)
        }
        if (descriptorDTO.externalUrl) {
            attributes.put('externalUrl', descriptorDTO.externalUrl)
        }
        if (descriptorDTO.unit) {
            attributes.put('unit', descriptorDTO.unit)
        }
        return attributes;
    }
}

class UnitDTO {
    final BigDecimal unitId
    final BigDecimal parentUnitId
    final String unit
    final String descriptionText

    UnitDTO(final BigDecimal unitId,
            final BigDecimal parentUnitId,
            final String unit,
            final String descriptionText) {
        this.unitId = unitId
        this.parentUnitId = parentUnitId
        this.unit = unit
        this.descriptionText = descriptionText
    }
}
class LaboratoryDTO {
    final BigDecimal labId
    final BigDecimal parentLabId
    final String description
    final String labName
    final String laboratoryStatus

    LaboratoryDTO(final BigDecimal labId,
                  final BigDecimal parentLabId,
                  final String description,
                  final String labName,
                  final String laboratoryStatus) {
        this.labId = labId
        this.parentLabId = parentLabId
        this.description = description
        this.labName = labName
        this.laboratoryStatus = laboratoryStatus
    }

}
class DescriptorDTO {
    final BigDecimal parentDescriptorId
    final BigDecimal descriptorId
    final BigDecimal elementId
    final String label
    final String description
    final String abbreviation
    final String synonyms
    final String externalUrl
    final String unit
    final String elementStatus

    DescriptorDTO(
            final BigDecimal parentDescriptorId,
            final BigDecimal descriptorId,
            final BigDecimal elementId,
            final String label,
            final String description,
            final String abbreviation,
            final String synonyms,
            final String externalUrl,
            final String unit,
            final String elementStatus
    ) {

        this.parentDescriptorId = parentDescriptorId
        this.descriptorId = descriptorId
        this.elementId = elementId
        this.label = label

        this.description = description
        this.abbreviation = abbreviation

        this.synonyms = synonyms
        this.externalUrl = externalUrl
        this.unit = unit
        this.elementStatus = elementStatus
    }
}
public enum DescriptorType {
    //name must match name of database table
    ASSAY_DESCRIPTOR, BIOLOGY_DESCRIPTOR, INSTANCE_DESCRIPTOR
}
class ResultTypeDTO {
    final BigDecimal parentResultTypeId
    final BigDecimal resultTypeId
    final String resultTypeName
    final String description
    final String abbreviation
    final String synonyms
    final String baseUnit
    final String status

    ResultTypeDTO(
            final BigDecimal parentResultTypeId,
            final BigDecimal resultTypeId,
            final String resultTypeName,
            final String description,
            final String abbreviation,
            final String synonyms,
            final String baseUnit,
            final String status) {

        this.parentResultTypeId = parentResultTypeId
        this.resultTypeId = resultTypeId
        this.resultTypeName = resultTypeName
        this.description = description
        this.abbreviation = abbreviation
        this.synonyms = synonyms
        this.baseUnit = baseUnit
        this.status = status
    }
}