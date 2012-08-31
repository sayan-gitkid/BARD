package bardqueryapi

import bard.core.adapter.CompoundAdapter
import bard.core.rest.RESTAssayService
import bard.core.rest.RESTCompoundService
import bard.core.rest.RESTProjectService
import elasticsearchplugin.ElasticSearchService
import elasticsearchplugin.QueryExecutorService
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll
import wslite.json.JSONObject
import bard.core.*
import bard.core.adapter.AssayAdapter
import bard.core.adapter.ProjectAdapter

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Unroll
@TestFor(QueryService)
class QueryServiceUnitSpec extends Specification {
    QueryServiceWrapper queryServiceWrapper
    QueryExecutorService queryExecutorService
    ElasticSearchService elasticSearchService
    RESTCompoundService restCompoundService
    RESTProjectService restProjectService
    RESTAssayService restAssayService
    final static String AUTO_COMPLETE_NAMES = '''
{
    "hits": {
        "hits": [
            {
                "fields": {
                    "name": "Broad Institute MLPCN Platelet Activation"
                }
            }
        ]
    }
  }
'''

    void setup() {
        queryExecutorService = Mock(QueryExecutorService.class)
        restCompoundService = Mock(RESTCompoundService.class)
        restProjectService = Mock(RESTProjectService.class)
        restAssayService = Mock(RESTAssayService.class)
        elasticSearchService = Mock(ElasticSearchService.class)
        queryServiceWrapper = Mock(QueryServiceWrapper.class)
        service.queryExecutorService = queryExecutorService
        service.elasticSearchService = elasticSearchService
        service.queryServiceWrapper = queryServiceWrapper
        service.elasticSearchRootURL = 'httpMock://'
        service.ncgcSearchBaseUrl = 'httpMock://'
    }

    void tearDown() {
        // Tear down logic here
    }

    /**
     */
    void "test autoComplete #label"() {

        when:
        final List<String> response = service.autoComplete(term)

        then:
        elasticSearchService.searchQueryStringQuery(_, _) >> { jsonResponse }

        assert response == expectedResponse

        where:
        label                       | term  | jsonResponse                        | expectedResponse
        "Partial match of a String" | "Bro" | new JSONObject(AUTO_COMPLETE_NAMES) | ["Broad Institute MLPCN Platelet Activation"]
        "Empty String"              | ""    | new JSONObject()                    | []
    }
    /**
     */
    void "test handleAutoComplete #label"() {

        when:
        final List<String> response = service.handleAutoComplete(term)

        then:
        elasticSearchService.searchQueryStringQuery(_, _) >> { jsonResponse }
        assert response == expectedResponse

        where:
        label                       | term  | jsonResponse                        | expectedResponse
        "Partial match of a String" | "Bro" | new JSONObject(AUTO_COMPLETE_NAMES) | ["Broad Institute MLPCN Platelet Activation"]
        "Empty String"              | ""    | new JSONObject()                    | []
    }
    /**
     */
    void "test Show Compound #label"() {

        given:
        Compound compound = Mock(Compound.class)
        when: "Client enters a CID and the showCompound method is called"
        CompoundAdapter compoundAdapter = service.showCompound(compoundId)
        then: "The CompoundDocument is called"
        queryServiceWrapper.getRestCompoundService() >> { restCompoundService }
        restCompoundService.get(_) >> {compound}
        assert compoundAdapter
        assert compoundAdapter.compound

        where:
        label                       | compoundId
        "Return a Compound Adapter" | new Integer(872)
    }



    void "test Show Project"() {
        given:
        Project project = Mock(Project.class)
        when: "Client enters a project ID and the showProject method is called"
        ProjectAdapter foundProjectAdpater = service.showProject(projectId)
        then: "The Project document is displayed"
        queryServiceWrapper.getRestProjectService() >> { restProjectService }
        restProjectService.get(_) >> {project}
        assert foundProjectAdpater

        where:
        label              | projectId
        "Return a Project" | new Integer(872)

    }

    void "test Show Assay"() {
        given:
        Assay assay = Mock(Assay.class)
        when: "Client enters a assay ID and the showAssay method is called"
        AssayAdapter foundAssay = service.showAssay(assayId)
        then: "The Assay document is displayed"
        queryServiceWrapper.getRestAssayService() >> { restAssayService }
        restAssayService.get(_) >> {assay}
        assert foundAssay

        where:
        label              | assayId
        "Return a Project" | new Integer(872)

    }

    void "test Structure Search #label"() {
        given:
        ServiceIterator<Compound> iter = Mock(ServiceIterator.class)
        when:
        service.structureSearch(smiles, structureSearchParamsType)
        then:
        queryServiceWrapper.getRestCompoundService() >> { restCompoundService }
        restCompoundService.structureSearch(_) >> {iter}

        where:
        label                    | structureSearchParamsType                 | smiles
        "Sub structure Search"   | StructureSearchParams.Type.Substructure   | "CC"
        "Exact match Search"     | StructureSearchParams.Type.Exact          | "CC"
        "Similarity Search"      | StructureSearchParams.Type.Similarity     | "CC"
        "Super structure search" | StructureSearchParams.Type.Superstructure | "CC"
    }
}
