/* Copyright (c) 2014, The Broad Institute
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of The Broad Institute nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL The Broad Institute BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package bard.db.model

import bard.db.dictionary.Element
import bard.db.enums.ExpectedValueType
import bard.db.enums.ValueType
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

import static bard.db.enums.ExpectedValueType.*
import org.apache.commons.lang3.StringUtils
import org.springframework.validation.Errors

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 11/2/12
 * Time: 11:02 AM
 * To change this template use File | Settings | File Templates.
 */
abstract class AbstractContextItem<T extends AbstractContext> {
    private static final int EXT_VALUE_ID_MAX_SIZE = 60
    private static final int VALUE_DISPLAY_MAX_SIZE = 500
    private static final int MODIFIED_BY_MAX_SIZE = 40

    Element attributeElement
    Element valueElement

    ValueType valueType = ValueType.NONE

    String extValueId
    String qualifier
    Float valueNum
    Float valueMin
    Float valueMax
    String valueDisplay

    Date dateCreated = new Date()
    Date lastUpdated
    String modifiedBy

    static constraints = {

        attributeElement(nullable: false)
        valueElement(nullable: true)

        extValueId(nullable: true, maxSize: EXT_VALUE_ID_MAX_SIZE)
        qualifier(nullable: true, inList: ['= ', '< ', '<=', '> ', '>=', '<<', '>>', '~ '])

        valueNum(nullable: true)
        valueMin(nullable: true)
        valueMax(nullable: true)
        valueDisplay(nullable: true, maxSize: VALUE_DISPLAY_MAX_SIZE)

        dateCreated(nullable: false)
        lastUpdated(nullable: true,)
        //TODO: Why is the validation on the modifiedBy attribute?
        modifiedBy(nullable: true, maxSize: MODIFIED_BY_MAX_SIZE,
                validator: { String field, AbstractContextItem instance, Errors errors ->

                    instance.valueValidation(errors)
                })

    }

    abstract T getContext()

    abstract void setContext(T context)

    /**
     * Note: the thought is that this would be useful for code creating or editing contextItems so at queryTime
     * clients will have a precomputed displayValue, this isn't meant to be a dynamic method used a queryTime
     *
     * @return String will try and create a reasonable displayValue based on the values populated in this item
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    String deriveDisplayValue() {
        switch(valueType) {
            case ValueType.ELEMENT:
                return valueElement.label
            case ValueType.NUMERIC:
                String valueQualifier = qualifier?.trim()
//                return [qualifier?.trim(), valueNum, attributeElement.unit?.abbreviation ?: attributeElement.unit?.label].findAll().join(' ')
                if(valueQualifier.equals("="))
                    return [valueNum, attributeElement.unit?.abbreviation ?: attributeElement.unit?.label].findAll().join(' ')
                else
                    return [qualifier?.trim(), valueNum, attributeElement.unit?.abbreviation ?: attributeElement.unit?.label].findAll().join(' ')
            case ValueType.RANGE:
                return [[valueMin, valueMax].join(' - '), attributeElement.unit?.abbreviation ?: attributeElement.unit?.label].findAll().join(' ')
            case ValueType.NONE:
                return null;
            case ValueType.FREE_TEXT:
                return valueDisplay
            default:
                throw new RuntimeException("Invalid type: ${valueType}")
        }
    }

    /**
     * Business rules for validating a contextItem
     * The value a contextItem holds can be held in 1 or more columns, but only certain combinations are valid and the
     * value of other fields particularly the attributeElement impact what state is valid
     * @see <a href="https://github.com/broadinstitute/BARD/wiki/Business-rules#general-business-rules-for-assay_context_item">general-business-rules-for-assay_context_item</a>
     * @param errors adding any errors via reject methods indicates the class is not valid
     */
    @TypeChecked
    protected void valueValidation(Errors errors) {
        valueValidation(errors, true)
    }

    /**
     *
     * @param errors adding any errors via reject methods indicates the class is not valid
     * @param includeRangeConstraints range constraints be excluded by passing false here, assayContextItems need to do this
     */
    @TypeChecked
    protected void valueValidation(Errors errors, boolean includeRangeConstraints) {
        if (attributeElement) {
            final ExpectedValueType expectedValueType = attributeElement.expectedValueType
            if (EXTERNAL_ONTOLOGY == expectedValueType) {
                externalOntologyConstraints(errors)
            } else if (ELEMENT == expectedValueType) {
                dictionaryConstraints(errors)
            } else if (NUMERIC == expectedValueType) {
                    valueNumConstraints(errors)
            } else if (FREE_TEXT == expectedValueType) {
                textValueConstraints(errors)
            } else if (NONE == expectedValueType) {
                noneValueConstraints(errors)
            } else {
                throw new RuntimeException("Unsupported ExpectedValueType ${attributeElement.expectedValueType}")
            }
        }
    }

    @TypeChecked
    private textValueConstraints(Errors errors) {
        final boolean valueDisplayBlank = rejectBlankField('valueDisplay', errors)
        final boolean otherValuesNotNull = rejectNotNullFields(['extValueId', 'valueElement', 'qualifier', 'valueNum', 'valueMin', 'valueMax'], errors)
        if (valueDisplayBlank || otherValuesNotNull) {
            errors.reject('contextItem.attribute.expectedValueType.FREE_TEXT.required.fields')
        }
    }

    @TypeChecked
    private noneValueConstraints(Errors errors) {
        if (rejectNotNullFields(['extValueId', 'valueElement', 'qualifier', 'valueNum', 'valueMin', 'valueMax', 'valueDisplay'], errors)) {
            errors.reject('contextItem.attribute.expectedValueType.NONE.required.fields')
        }
    }

    @TypeChecked
    protected void valueNumConstraints(Errors errors) {
        final boolean valueNumNull = rejectNullField('valueNum', errors)
        final boolean qualifierBlank = rejectBlankField('qualifier', errors)
        final boolean valueDisplayBlank = rejectBlankField('valueDisplay', errors)
        final boolean otherFieldsNotNull = rejectNotNullFields(['extValueId', 'valueElement', 'valueMin', 'valueMax'], errors)
        if (valueNumNull || qualifierBlank || valueDisplayBlank || otherFieldsNotNull) {
            errors.reject('contextItem.valueNum.required.fields')
        }
    }

    @TypeChecked
    protected void rangeConstraints(Errors errors) {
        final boolean rangeValuesNull = rejectNullFields(['valueMin', 'valueMax'], errors)
        final boolean valueDisplayFieldBlank = rejectBlankField('valueDisplay', errors)
        final boolean otherValueFieldsNotNull = rejectNotNullFields(['extValueId', 'valueElement', 'qualifier', 'valueNum'], errors)
        if (rangeValuesNull || valueDisplayFieldBlank || otherValueFieldsNotNull) {
            errors.reject('contextItem.range.required.fields')
        } else if (valueMin != null || valueMax != null) {
            if (valueMin >= valueMax) {
                errors.rejectValue('valueMin', 'contextItem.valueMin.not.less.than.valueMax')
                errors.rejectValue('valueMax', 'contextItem.valueMax.not.greater.than.valueMin')
                errors.reject('contextItem.range.requirements')
            }
        }
    }

    @TypeChecked
    protected void dictionaryConstraints(Errors errors) {
        //TODO check to ensure valueElement is a descendant of the attributeElement
        final boolean valueElementNull = rejectNullField('valueElement', errors)
        final boolean valueDisplayBlank = rejectBlankField('valueDisplay', errors)
        final boolean otherValueFieldsNotNull = rejectNotNullFields(['extValueId', 'qualifier', 'valueNum', 'valueMin', 'valueMax'], errors)
        if (valueElementNull || valueDisplayBlank || otherValueFieldsNotNull) {
            errors.reject('contextItem.attribute.expectedValueType.ELEMENT.required.fields')
        }
    }

    @TypeChecked
    protected void externalOntologyConstraints(Errors errors) {
        final boolean externalOntologyFieldsBlank = rejectBlankFields(['extValueId', 'valueDisplay'], errors)
        final boolean otherFieldsNotNull = rejectNotNullFields(['valueElement', 'qualifier', 'valueNum', 'valueMin', 'valueMax'], errors)
        if (externalOntologyFieldsBlank || otherFieldsNotNull) {
            errors.reject('contextItem.attribute.externalURL.required.fields')
        }
    }

    @TypeChecked
    protected boolean rejectNonBlankFields(List<String> fieldNames, Errors errors) {
        List rejectedFields = []
        for (String fieldName in fieldNames) {
            rejectedFields << rejectNonBlankField(fieldName, errors)
        }
        rejectedFields.grep { it == true }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    protected boolean rejectNonBlankField(String fieldName, Errors errors) {
        if (StringUtils.isNotBlank(this[(fieldName)])) {
            errors.rejectValue(fieldName, "contextItem.${fieldName}.blank")
            return true
        }
        return false
    }

    @TypeChecked
    protected boolean rejectBlankFields(List<String> fieldNames, Errors errors) {
        List rejectedFields = []
        for (String fieldName in fieldNames) {
            rejectedFields << rejectBlankField(fieldName, errors)
        }
        rejectedFields.grep { it == true }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    protected boolean rejectBlankField(String fieldName, Errors errors) {
        if (StringUtils.isBlank(this[(fieldName)])) {
            errors.rejectValue(fieldName, "contextItem.${fieldName}.blank")
            return true
        }
        return false
    }

    @TypeChecked
    protected boolean rejectNotNullFields(List<String> fieldNames, Errors errors) {
        List rejectedFields = []
        for (String fieldName in fieldNames) {
            rejectedFields << rejectNotNullField(fieldName, errors)
        }
        rejectedFields.grep { it == true }
    }

    @TypeChecked
    private boolean rejectNotNullField(String fieldName, Errors errors) {
        if (this[(fieldName)] != null) {
            errors.rejectValue(fieldName, "contextItem.${fieldName}.not.null")
            return true
        }
        return false
    }

    @TypeChecked
    protected boolean rejectNullFields(List<String> fieldNames, Errors errors) {
        List rejectedFields = []
        for (String fieldName in fieldNames) {
            rejectedFields << rejectNullField(fieldName, errors)
        }
        rejectedFields.grep { it == true }
    }

    @TypeChecked
    private boolean rejectNullField(String fieldName, Errors errors) {
        if (this[(fieldName)] == null) {
            errors.rejectValue(fieldName, "contextItem.${fieldName}.null")
            return true
        }
        return false
    }

    public Object getValue() {
        switch(valueType) {
            case ValueType.ELEMENT:
                return valueElement;
            case ValueType.EXTERNAL_ONTOLOGY:
                return new ExternalOntologyValue(valueDisplay: valueDisplay, extValueId: extValueId)
            case ValueType.FREE_TEXT:
                return valueDisplay;
            case ValueType.NUMERIC:
                return new NumericValue(qualifier: qualifier, number: valueNum)
            case ValueType.RANGE:
                return new RangeValue(valueMin: valueMin, valueMax: valueMax)
            case ValueType.NONE:
                return null;
            default:
                throw new RuntimeException("Invalid type: ${valueType}")
        }
    }

    protected void clearValue() {
        valueElement = null
        valueType = null
        extValueId = null
        qualifier = null
        valueNum = null
        valueMin = null
        valueMax = null
        valueDisplay = null
    }


    public void setDictionaryValue(Element element) {
        clearValue()
        valueType = ValueType.ELEMENT

        this.valueElement = element

        this.valueDisplay = deriveDisplayValue()
    }

    public void setExternalOntologyValue(String extValueId, String valueDisplay) {
        clearValue()

        this.valueType = ValueType.EXTERNAL_ONTOLOGY
        this.valueDisplay = valueDisplay
        this.extValueId = extValueId
    }

    public void setFreeTextValue(String valueDisplay) {
        clearValue()

        this.valueType = ValueType.FREE_TEXT
        this.valueDisplay = valueDisplay
    }

    public void setNumericValue(String qualifier, float value) {
        clearValue()

        valueType = ValueType.NUMERIC
        this.qualifier = qualifier
        this.valueNum = value

        this.valueDisplay = deriveDisplayValue()
    }

    public void setRange(float valueMin, float valueMax) {
        clearValue()

        valueType = ValueType.RANGE
        this.valueMin = valueMin
        this.valueMax = valueMax

        this.valueDisplay = deriveDisplayValue()
    }

    public void setNoneValue() {
        clearValue()

        valueType = ValueType.NONE
    }
}
