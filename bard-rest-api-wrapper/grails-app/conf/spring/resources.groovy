import org.springframework.web.client.RestTemplate

import java.util.concurrent.Executors

import bard.core.rest.spring.*

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
    substanceRestService(SubstanceRestService) {
        baseUrl = ncgcBaseURL
        restTemplate = ref('restTemplate')
        //executorService = ref('executorService')

    }
    experimentRestService(ExperimentRestService) {
        baseUrl = ncgcBaseURL
        restTemplate = ref('restTemplate')
        //executorService = ref('executorService')
    }
    projectRestService(ProjectRestService) {
        baseUrl = ncgcBaseURL
        restTemplate = ref('restTemplate')
        //executorService = ref('executorService')
    }
    assayRestService(AssayRestService) {
        baseUrl = ncgcBaseURL
        restTemplate = ref('restTemplate')
        //executorService = ref('executorService')
    }
}
