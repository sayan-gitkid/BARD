package molspreadsheet

import bard.core.HillCurveValue

class MolSpreadSheetCell {
    // static final int SPREAD_SHEET_PRECISION = 3

    static hasOne = [spreadSheetActivityStorage: SpreadSheetActivityStorage]

    static belongsTo = [molSpreadSheetData: MolSpreadSheetData]

    Boolean activity = true
    MolSpreadSheetCellType molSpreadSheetCellType = MolSpreadSheetCellType.unknown
    String strInternalValue = "null"
    Integer intInternalValue = 0
    String supplementalInternalValue
    SpreadSheetActivityStorage spreadSheetActivityStorage


    static constraints = {
        activity(nullable: false)
        molSpreadSheetCellType(blank: false)
        strInternalValue(nullable: true)
        intInternalValue(nullable: false)
        supplementalInternalValue(nullable: true)
        spreadSheetActivityStorage(nullable: true)
    }

    public MolSpreadSheetCell() {

    }

    public MolSpreadSheetCell(MolSpreadSheetCell molSpreadSheetCellToCopy) {
        this.activity = molSpreadSheetCellToCopy.activity
        this.molSpreadSheetCellType = molSpreadSheetCellToCopy.molSpreadSheetCellType
        this.strInternalValue = molSpreadSheetCellToCopy.strInternalValue
        this.intInternalValue = molSpreadSheetCellToCopy.intInternalValue
        this.supplementalInternalValue = molSpreadSheetCellToCopy.supplementalInternalValue
        this.spreadSheetActivityStorage = null
    }


    public MolSpreadSheetCell(MolSpreadSheetCell molSpreadSheetCellToCopy, int exptIndex) {
        this.activity = molSpreadSheetCellToCopy.activity
        this.molSpreadSheetCellType = molSpreadSheetCellToCopy.molSpreadSheetCellType
        this.strInternalValue = molSpreadSheetCellToCopy.strInternalValue
        this.intInternalValue = molSpreadSheetCellToCopy.intInternalValue
        this.supplementalInternalValue = molSpreadSheetCellToCopy.supplementalInternalValue
        this.spreadSheetActivityStorage = new SpreadSheetActivityStorage(molSpreadSheetCellToCopy.spreadSheetActivityStorage, exptIndex)
    }

    /**
     *  non image, no units  specified
     * @param value
     * @param molSpreadSheetCellType
     */
    MolSpreadSheetCell(String value, MolSpreadSheetCellType molSpreadSheetCellType, SpreadSheetActivityStorage spreadSheetActivityStorage = null) {
        this.spreadSheetActivityStorage = spreadSheetActivityStorage
        this.molSpreadSheetCellType = molSpreadSheetCellType
        if (this.molSpreadSheetCellType == MolSpreadSheetCellType.identifier) {
            if ("NaN".equals(value)) {
                activity = false;
                intInternalValue = 0
            } else {
                intInternalValue = new Integer(value)
            }
        }
        if (this.molSpreadSheetCellType == MolSpreadSheetCellType.string) {
            strInternalValue = value
        }
    }

    /**
     *  numeric elements with unit specifications
     * @param value
     * @param molSpreadSheetCellType
     * @param molSpreadSheetCellUnit
     */
    MolSpreadSheetCell(String value, MolSpreadSheetCellType molSpreadSheetCellType, MolSpreadSheetCellUnit molSpreadSheetCellUnit, SpreadSheetActivityStorage spreadSheetActivityStorage = null) {
        this.spreadSheetActivityStorage = spreadSheetActivityStorage
        this.molSpreadSheetCellType = molSpreadSheetCellType
    }



    MolSpreadSheetCell(SpreadSheetActivity spreadSheetActivity) {
        this.molSpreadSheetCellType = MolSpreadSheetCellType.numeric
        this.spreadSheetActivityStorage = new SpreadSheetActivityStorage(eid: spreadSheetActivity.eid,
                cid: spreadSheetActivity.cid,
                sid: spreadSheetActivity.sid,
                activityOutcome: spreadSheetActivity.activityOutcome)
        for (HillCurveValue hillCurveValue in spreadSheetActivity.hillCurveValueList) {
            HillCurveValueHolder hillCurveValueHolder = new HillCurveValueHolder(identifier: hillCurveValue.id,
                    s0: hillCurveValue.s0,
                    sInf: hillCurveValue.sInf,
                    slope: hillCurveValue.slope,
                    coef: hillCurveValue.coef,
                    conc: hillCurveValue.conc,
                    response: hillCurveValue.response)
            if (!this.spreadSheetActivityStorage.columnNames.contains(hillCurveValue.id)) {
                this.spreadSheetActivityStorage.columnNames << hillCurveValue.id
            }
            hillCurveValueHolder.subColumnIndex = this.spreadSheetActivityStorage.columnNames.indexOf(hillCurveValue.id)
            this.spreadSheetActivityStorage.hillCurveValueHolderList << hillCurveValueHolder
        }

    }

    /**
     * ctor for images
     * @param value1
     * @param value2
     * @param molSpreadSheetCellType
     */
    MolSpreadSheetCell(String value1, String value2, MolSpreadSheetCellType molSpreadSheetCellType, SpreadSheetActivityStorage spreadSheetActivityStorage = null) {
        this.molSpreadSheetCellType = molSpreadSheetCellType
        this.spreadSheetActivityStorage = spreadSheetActivityStorage
        if (this.molSpreadSheetCellType == MolSpreadSheetCellType.image) {
            strInternalValue = value1 ?: ""
            supplementalInternalValue = value2 ?: ""
        }
    }

    /**
     *
     * @return
     */
    Map<String, String> mapForMolecularSpreadsheet() {
        Map<String, String> returnValue = [:]
        if (molSpreadSheetCellType == MolSpreadSheetCellType.image) {
            returnValue = retrieveValues()
        } else {
            returnValue["value"] = toString()
        }
        returnValue
    }

    /**
     *
     * @return
     */
    Map<String, String> retrieveValues() {
        return ["name": strInternalValue, "smiles": supplementalInternalValue]
    }



    @Override
    String toString() {
        String returnValue = null
        if (molSpreadSheetCellType == MolSpreadSheetCellType.identifier) {
            returnValue = intInternalValue
        } else if (molSpreadSheetCellType == MolSpreadSheetCellType.string) {
            returnValue = strInternalValue
        }
        returnValue

    }


}



