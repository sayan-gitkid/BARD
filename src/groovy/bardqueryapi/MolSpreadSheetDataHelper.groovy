package bardqueryapi

import bard.core.Experiment
import molspreadsheet.MolSpreadSheetData


/**
 * Created with IntelliJ IDEA.
 * User: balexand
 * Date: 9/14/12
 * Time: 11:55 AM
 * To change this template use File | Settings | File Templates.
 */

class MolSpreadSheetDataHelper{
}


class MolSpreadSheetDataBuilder{
    protected MolecularSpreadSheetService molecularSpreadSheetService
    protected  MolSpreadSheetData molSpreadSheetData
    List<CartCompound> cartCompoundList
    List<CartAssay> cartAssayList
    List<CartProject> cartProjectList
    Object etag
    List<SpreadSheetActivity> spreadSheetActivityList

    public MolSpreadSheetDataBuilder(MolecularSpreadSheetService molecularSpreadSheetService){
        this.molecularSpreadSheetService = molecularSpreadSheetService
    }

    public  MolSpreadSheetData getMolSpreadSheetData() {    molSpreadSheetData  }
    public void createNewMolSpreadSheetData() {
        molSpreadSheetData=new MolSpreadSheetData()
    }

    public void holdCartResults(List<CartCompound> cartCompoundList,List<CartAssay> cartAssayList,List<CartProject> cartProjectList){
        this.cartCompoundList = cartCompoundList
        this.cartAssayList =  cartAssayList
        this.cartProjectList = cartProjectList
    }



    public List<Experiment> deriveListOfExperiments() {
        List<Experiment> experimentList

        // Any projects can be converted to assays, then assays to experiments
        if (this.cartProjectList?.size() > 0) {
//            Collection<Assay> assayCollection = molecularSpreadSheetService.cartProjectsToAssays(cartProjectList)
//            experimentList = molecularSpreadSheetService.assaysToExperiments(assayCollection)
            experimentList = molecularSpreadSheetService.cartProjectsToExperiments(this.cartProjectList)
        }

        // Any assays explicitly selected on the cart are added to the  experimentList
        if (this.cartAssayList?.size() > 0) {
            experimentList = molecularSpreadSheetService.cartAssaysToExperiments(experimentList, this.cartAssayList)
        }


        experimentList
    }




    public void populateMolSpreadSheet(List<Experiment> experimentList) {

        // this is the variable we plan to fill
        molSpreadSheetData = new MolSpreadSheetData()

        // use experiment names to provide names for the columns
        molecularSpreadSheetService.populateMolSpreadSheetColumnMetadata (molSpreadSheetData, experimentList)

        // next deal with the compounds
        if (experimentList.size() > 0) {

            if (cartCompoundList.size() > 0) {

                // Explicitly specified assays and explicitly specified compounds
                molecularSpreadSheetService.populateMolSpreadSheetRowMetadata(molSpreadSheetData, cartCompoundList)
                etag = molecularSpreadSheetService.generateETagFromCartCompounds(cartCompoundList)
                spreadSheetActivityList = molecularSpreadSheetService.extractMolSpreadSheetData(molSpreadSheetData, experimentList, etag)

            } else if (cartCompoundList.size() == 0) {

                // Explicitly specified assay, for which we will retrieve all compounds
                etag = molecularSpreadSheetService.retrieveImpliedCompoundsEtagFromAssaySpecification(experimentList)
                spreadSheetActivityList = molecularSpreadSheetService.extractMolSpreadSheetData(molSpreadSheetData, experimentList, etag)
                Map map = molecularSpreadSheetService.convertSpreadSheetActivityToCompoundInformation(spreadSheetActivityList)
                molecularSpreadSheetService.populateMolSpreadSheetRowMetadata(molSpreadSheetData, map)

             }
            // finally deal with the data
            molecularSpreadSheetService.populateMolSpreadSheetData(molSpreadSheetData, experimentList, spreadSheetActivityList)
        }
    }


}

class MolSpreadSheetDataBuilderDirector {
    private MolSpreadSheetDataBuilder molSpreadSheetDataBuilder

    public setMolSpreadSheetDataBuilder(MolSpreadSheetDataBuilder molSpreadSheetDataBuilder) {
        this.molSpreadSheetDataBuilder = molSpreadSheetDataBuilder
    }

    public MolSpreadSheetData getMolSpreadSheetData() {
        molSpreadSheetDataBuilder.getMolSpreadSheetData()
    }

    public void constructMolSpreadSheetData(List<CartCompound> cartCompoundList,
                                            List<CartAssay> cartAssayList,
                                            List<CartProject> cartProjectList) {

        molSpreadSheetDataBuilder.holdCartResults(cartCompoundList, cartAssayList, cartProjectList)

        List<Experiment> experimentList = molSpreadSheetDataBuilder.deriveListOfExperiments()

        molSpreadSheetDataBuilder.populateMolSpreadSheet(experimentList)

    }

}