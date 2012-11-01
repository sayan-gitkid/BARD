package molspreadsheet

import grails.test.mixin.TestFor
import spock.lang.Unroll
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(SpreadSheetActivityStorage)
@Unroll
class SpreadSheetActivityStorageSpec  extends Specification{

    MolSpreadSheetCell molSpreadSheetCell

    void setup() {
         molSpreadSheetCell = new MolSpreadSheetCell()
    }

    void tearDown() {
        // Tear down logic here
    }

    void "Test that we can build a Basic molecular spreadsheet cell"() {
        when:
        SpreadSheetActivityStorage spreadSheetActivityStorage = new SpreadSheetActivityStorage()
        assertNotNull(spreadSheetActivityStorage)

        then:
        assertNotNull spreadSheetActivityStorage.hillCurveValueHolderList
        assertNotNull spreadSheetActivityStorage.columnNames
    }


    void "Test molecular spreadsheet cell constructor"() {
        given:
        SpreadSheetActivityStorage originalSpreadSheetActivityStorage = new SpreadSheetActivityStorage()
        originalSpreadSheetActivityStorage.eid=41L
        originalSpreadSheetActivityStorage.cid=42L
        originalSpreadSheetActivityStorage.sid=43L
        originalSpreadSheetActivityStorage.hillCurveValueHolderList << new HillCurveValueHolder(slope: 1, identifier:"a",subColumnIndex: 1)
        originalSpreadSheetActivityStorage.hillCurveValueHolderList << new HillCurveValueHolder(slope: 1, identifier:"b",subColumnIndex: 2)


        when:
        SpreadSheetActivityStorage copySpreadSheetActivityStorage = new SpreadSheetActivityStorage(originalSpreadSheetActivityStorage,hillCurveValueIndex)
        assertNotNull(copySpreadSheetActivityStorage)

        then:
        assert originalSpreadSheetActivityStorage.eid ==  copySpreadSheetActivityStorage.eid
        assert originalSpreadSheetActivityStorage.cid ==  copySpreadSheetActivityStorage.cid
        assert originalSpreadSheetActivityStorage.sid ==  copySpreadSheetActivityStorage.sid
        assert copySpreadSheetActivityStorage.hillCurveValueHolderList[0]?.subColumnIndex ==  columnIndex

        where:
        hillCurveValueIndex     |   identifier  |   columnIndex
        0                       |  "a"          |   1
        1                       |  "b"          |   2
        2                       |  null         |   null
    }



    void "Test constraints for molecular spreadsheet data"() {
        given:
        mockForConstraintsTests(SpreadSheetActivityStorage)

        when:
        SpreadSheetActivityStorage spreadSheetActivityStorage = new SpreadSheetActivityStorage()
        spreadSheetActivityStorage.molSpreadSheetCell = molSpreadSheetCell
        spreadSheetActivityStorage.validate()
        spreadSheetActivityStorage.hasErrors()

        then:
        spreadSheetActivityStorage.setEid(null)
        assertTrue spreadSheetActivityStorage.validate()

        spreadSheetActivityStorage.setCid(null)
        assertTrue spreadSheetActivityStorage.validate()

        spreadSheetActivityStorage.setSid(null)
        assertTrue spreadSheetActivityStorage.validate()

        spreadSheetActivityStorage.setActivityOutcome(null)
        assertTrue spreadSheetActivityStorage.validate()

        spreadSheetActivityStorage.setPotency(null)
        assertTrue spreadSheetActivityStorage.validate()

    }



}
