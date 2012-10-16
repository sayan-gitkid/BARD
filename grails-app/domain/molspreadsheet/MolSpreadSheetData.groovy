package molspreadsheet

class MolSpreadSheetData {

    static hasMany = [ molSpreadSheetCell : MolSpreadSheetCell ]
    static transients = ['rowCount','columnCount']

    Map<String,MolSpreadSheetCell> mssData   = [:]
    Map<Long,Integer> rowPointer  = [:]
    Map<Long,Integer> columnPointer   = [:]
    List<String> mssHeaders   = []

    /**
     * Display a cell, as specified by a row and column
     * @param rowCnt
     * @param colCnt
     * @return
     */
    Map displayValue(int rowCnt, int colCnt) {
        Map<String, String> returnValue = [:]
        String key = "${rowCnt}_${colCnt}"
        MolSpreadSheetCell molSpreadSheetCell
        if (mssData.containsKey(key)) {
            molSpreadSheetCell = mssData[key]
            returnValue = molSpreadSheetCell.mapForMolecularSpreadsheet ()
        }   else {  // This is a critical error.  Try to cover all the bases so we don't crash at least.
            returnValue.put("value","-")
            returnValue.put("name", "Unknown name")
            returnValue.put("smiles","Unknown smiles")
        }
        returnValue
    }

    SpreadSheetActivityStorage findSpreadSheetActivity(int rowCnt, int colCnt){
        SpreadSheetActivityStorage spreadSheetActivityStorage = null
        String key = "${rowCnt}_${colCnt}"
        MolSpreadSheetCell molSpreadSheetCell
        if (mssData.containsKey(key)) {
            molSpreadSheetCell = mssData[key]
            spreadSheetActivityStorage = molSpreadSheetCell.spreadSheetActivityStorage
        }
        return spreadSheetActivityStorage
    }


    /**
     *
     * @return
     */
    int getRowCount(){
        if (rowPointer) {
            return rowPointer.size()
        }
        return 0
    }

    /**
     *
     * @return
     */
    int getColumnCount(){
        if (mssHeaders) {
            return mssHeaders.size()
        }
        return 0

    }

    static constraints = {
        mssData  (nullable: false)
        rowPointer  (nullable: false)
        columnPointer  (nullable: false)
        //mssHeaders (nullable: false)
    }
}


