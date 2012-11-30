package bard.db.project

import bard.db.model.AbstractContext

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 11/1/12
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */
class ProjectExperimentContext extends AbstractContext{

    ProjectExperiment projectExperiment
//    List<ProjectContextItem> projectContextItems = []

    static belongsTo = [projectExperiment: ProjectExperiment]

//    static hasMany = [projectContextItems: ProjectContextItem]

    static mapping = {
        table('PRJCT_EXPRMT_CONTEXT')
        id(column: "PRJCT_EXPRMT_CONTEXT_ID", generator: "sequence", params: [sequence: 'PRJCT_EXPRMT_CONTEXT_ID_SEQ'])
//        projectContextItems(indexColumn: [name: 'DISPLAY_ORDER'])
    }

}
