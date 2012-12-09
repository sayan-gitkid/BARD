import bard.core.rest.spring.AssayRestService
import bard.core.rest.spring.CompoundRestService
import bard.core.rest.spring.ExperimentRestService
import bard.core.rest.spring.ProjectRestService
import bardqueryapi.QueryService
import grails.util.GrailsUtil
import mockServices.MockQueryService
import org.springframework.web.client.RestTemplate
import java.util.concurrent.Executors

/**
 * Spring Configuration of resources
 */
beans = {
    final String ncgcBaseURL = grailsApplication.config.ncgc.server.root.url
    final String badApplePromiscuityUrl = grailsApplication.config.promiscuity.badapple.url
    restTemplate(RestTemplate) {
    }

    compoundRestService(CompoundRestService) {
        baseUrl = ncgcBaseURL
        promiscuityUrl = badApplePromiscuityUrl
        restTemplate = ref('restTemplate')
        executorService = Executors.newCachedThreadPool()
    }

    experimentRestService(ExperimentRestService) {
        baseUrl = ncgcBaseURL
        restTemplate = ref('restTemplate')
    }
    projectRestService(ProjectRestService) {
        baseUrl = ncgcBaseURL
        restTemplate = ref('restTemplate')
    }
    assayRestService(AssayRestService) {
        baseUrl = ncgcBaseURL
        restTemplate = ref('restTemplate')
    }

    switch (GrailsUtil.environment) {
        case "offline":
            queryService(MockQueryService) {
                queryHelperService = ref('queryHelperService')
            }
            break;
        default:
            queryService(QueryService) {
                queryHelperService = ref('queryHelperService')
                compoundRestService = ref('compoundRestService')
                projectRestService = ref('projectRestService')
                assayRestService = ref('assayRestService')
            }
    }
    crowdAuthenticationProvider(org.broadinstitute.cbip.crowd.CrowdAuthenticationProviderService) {// beans here
        crowdClient = ref('crowdClient')
        grailsApplication = application
    }
    inMemMapAuthenticationProviderService(org.broadinstitute.cbip.crowd.noServer.MockCrowdAuthenticationProviderService)
}
