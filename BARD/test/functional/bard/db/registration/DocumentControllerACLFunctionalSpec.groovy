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

package bard.db.registration

import bard.db.enums.DocumentType
import bard.db.people.Role
import bard.db.project.Project
import bard.db.project.ProjectDocument
import groovy.sql.Sql
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import spock.lang.Shared
import spock.lang.Unroll
import wslite.rest.RESTClient
import wslite.rest.RESTClientException

import javax.servlet.http.HttpServletResponse

/**
 * Created with IntelliJ IDEA.
 * User: jasiedu
 * Date: 7/2/13
 * Time: 9:16 PM
 * To change this template use File | Settings | File Templates.
 *
 *  TEAM_A_1 and TEAM_A_2 belong to the same group
 *
 *  TEAM_B_1 belong to a different group
 *
 *  TEAM_A_1 also has ROLE_CURATOR
 *
 *
 *
 */
@Unroll
class DocumentControllerACLFunctionalSpec extends BardControllerFunctionalSpec {
    static final String controllerUrl = getBaseUrl() + "document/"

    @Shared
    Map documentData
    @Shared
    List<Long> assayIdList = []  //we keep ids of all assays here so we can delete after all the tests have finished
    @Shared
    List<Long> projectIdList = []

    def setupSpec() {
        String reauthenticateWithUser = TEAM_A_1_USERNAME

        documentData = (Map) remote.exec({
            //Build assay as TEAM_A
            SpringSecurityUtils.reauthenticate(reauthenticateWithUser, null)
            Role role = Role.findByAuthority('ROLE_TEAM_A')
            if (!role) {
                role = Role.build(authority: 'ROLE_TEAM_A', displayName: 'ROLE_TEAM_A').save(flush: true)
            }
            Role otherRole = Role.findByAuthority('ROLE_TEAM_B')
            if (!otherRole) {
                otherRole = Role.build(authority: 'ROLE_TEAM_B', displayName: 'ROLE_TEAM_B').save(flush: true)
            }
            Assay assay = Assay.build(assayName: "Assay Name10", ownerRole:role).save(flush: true)
            Project project = Project.build( ownerRole:role).save(flush: true)

            String documentName = "Comments"
            DocumentType documentType = DocumentType.DOCUMENT_TYPE_COMMENTS
            AssayDocument assayDocument = AssayDocument.build(documentName: documentName, documentType: documentType, assay: assay).save(flush: true)
            ProjectDocument projectDocument = ProjectDocument.build(documentName: documentName, documentType: documentType, project: project).save(flush: true)

            //create assay context
            return [assayId: assay.id, projectId: project.id, assayDocumentId: assayDocument.id, projectDocumentId: projectDocument.id]
        })
        assayIdList.add(documentData.assayId)
        projectIdList.add(documentData.projectId)

    }     // run before the first feature method
    def cleanupSpec() {

        Sql sql = Sql.newInstance(dburl, dbusername,
                dbpassword, driverClassName)
        sql.call("{call bard_context.set_username(?)}", [TEAM_A_1_USERNAME])


        sql.execute("DELETE FROM ASSAY_DOCUMENT WHERE ASSAY_ID=${documentData.assayId}")
        sql.execute("DELETE FROM PROJECT_DOCUMENT WHERE PROJECT_ID=${documentData.projectId}")
        sql.execute("DELETE FROM ASSAY WHERE ASSAY_ID=${documentData.assayId}")
        sql.execute("DELETE FROM PROJECT WHERE PROJECT_ID=${documentData.projectId}")


    }

    Map createProjectDocument() {
        String reauthenticateWithUser = TEAM_A_1_USERNAME
        Long projectId = documentData.projectId

        return remote.exec({
            SpringSecurityUtils.reauthenticate(reauthenticateWithUser, null)
            Project project = Project.findById(projectId)

            String documentName = "Comments"
            DocumentType documentType = DocumentType.DOCUMENT_TYPE_COMMENTS
            ProjectDocument projectDocument = ProjectDocument.build(documentName: documentName, documentType: documentType, project: project).save(flush: true)

            return [projectId: project.id, projectDocumentId: projectDocument.id]
        })

    }

    Map createAssayDocument() {
        String reauthenticateWithUser = TEAM_A_1_USERNAME
        Long assayId = documentData.assayId

        return remote.exec({
            SpringSecurityUtils.reauthenticate(reauthenticateWithUser, null)
            Assay assay = Assay.findById(assayId)

            String documentName = "Comments"
            DocumentType documentType = DocumentType.DOCUMENT_TYPE_COMMENTS
            AssayDocument assayDocument = AssayDocument.build(documentName: documentName, documentType: documentType, assay: assay).save(flush: true)

            return [assayId: assay.id, assayDocumentId: assayDocument.id]
        })
    }


    def 'test delete Project Document #desc'() {
        given:
        Map currentData = (Map) createProjectDocument()
        long projectDocumentId = currentData.projectDocumentId
        RESTClient client = getRestClient(controllerUrl, "delete", team, teamPassword)

        when:
        def response = client.post() {
            urlenc type: "Project", id: projectDocumentId
        }
        then:
        assert response.statusCode == expectedHttpResponse
        where:
        desc       | team              | teamPassword      | expectedHttpResponse
        "User A_1" | TEAM_A_1_USERNAME | TEAM_A_1_PASSWORD | HttpServletResponse.SC_FOUND
        "User A_2" | TEAM_A_2_USERNAME | TEAM_A_2_PASSWORD | HttpServletResponse.SC_FOUND
        "ADMIN"    | ADMIN_USERNAME    | ADMIN_PASSWORD    | HttpServletResponse.SC_FOUND
    }

    def 'test delete Project Document #forbidden'() {
        Map currentData = (Map) createProjectDocument()
        long projectDocumentId = currentData.projectDocumentId

        RESTClient client = getRestClient(controllerUrl, "delete", team, teamPassword)

        when:
        client.post() {
            urlenc type: "Project", id: projectDocumentId
        }
        then:
        def ex = thrown(RESTClientException)
        assert ex.response.statusCode == expectedHttpResponse

        where:
        desc                  | team              | teamPassword      | expectedHttpResponse
        "User B cannot Edit"  | TEAM_B_1_USERNAME | TEAM_B_1_PASSWORD | HttpServletResponse.SC_FORBIDDEN
        "CURATOR cannot Edit" | CURATOR_USERNAME  | CURATOR_PASSWORD  | HttpServletResponse.SC_FORBIDDEN
    }

    def 'test delete Assay Document #desc'() {
        given:
        Map currentData = (Map) createAssayDocument()
        long assayDocumentId = currentData.assayDocumentId
        RESTClient client = getRestClient(controllerUrl, "delete", team, teamPassword)

        when:
        def response = client.post() {
            urlenc type: "Assay", id: assayDocumentId
        }
        then:
        assert response.statusCode == expectedHttpResponse
        where:
        desc       | team              | teamPassword      | expectedHttpResponse
        "User A_1" | TEAM_A_1_USERNAME | TEAM_A_1_PASSWORD | HttpServletResponse.SC_FOUND
        "User A_2" | TEAM_A_2_USERNAME | TEAM_A_2_PASSWORD | HttpServletResponse.SC_FOUND
        "ADMIN"    | ADMIN_USERNAME    | ADMIN_PASSWORD    | HttpServletResponse.SC_FOUND
    }

    def 'test delete Assay Document #forbidden'() {
        given:
        Map currentData = (Map) createAssayDocument()
        long assayDocumentId = currentData.assayDocumentId
        RESTClient client = getRestClient(controllerUrl, "delete", team, teamPassword)

        when:
        response = client.post() {
            urlenc type: "Assay", id: assayDocumentId
        }

        then:
        def ex = thrown(RESTClientException)
        assert ex.response.statusCode == expectedHttpResponse

        where:
        desc                  | team              | teamPassword      | expectedHttpResponse
        "User B cannot Edit"  | TEAM_B_1_USERNAME | TEAM_B_1_PASSWORD | HttpServletResponse.SC_FORBIDDEN
        "CURATOR cannot Edit" | CURATOR_USERNAME  | CURATOR_PASSWORD  | HttpServletResponse.SC_FORBIDDEN
    }

    def 'test create Project Document #desc'() {
        given:
        long projectId = documentData.projectId
        RESTClient client = getRestClient(controllerUrl, "create", team, teamPassword)

        when:
        def response = client.post() {
            urlenc projectId: projectId, documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.toString()
        }
        then:
        assert response.statusCode == expectedHttpResponse
        where:
        desc       | team              | teamPassword      | expectedHttpResponse
        "User A_1" | TEAM_A_1_USERNAME | TEAM_A_1_PASSWORD | HttpServletResponse.SC_OK
        "User A_2" | TEAM_A_2_USERNAME | TEAM_A_2_PASSWORD | HttpServletResponse.SC_OK
        "ADMIN"    | ADMIN_USERNAME    | ADMIN_PASSWORD    | HttpServletResponse.SC_OK
    }

    def 'test create Project Document #forbidden'() {
        given:
        long projectId = documentData.projectId
        RESTClient client = getRestClient(controllerUrl, "create", team, teamPassword)

        when:
        client.post() {
            urlenc projectId: projectId, documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.toString()
        }

        then:
        def ex = thrown(RESTClientException)
        assert ex.response.statusCode == expectedHttpResponse

        where:
        desc                  | team              | teamPassword      | expectedHttpResponse
        "User B cannot Edit"  | TEAM_B_1_USERNAME | TEAM_B_1_PASSWORD | HttpServletResponse.SC_FORBIDDEN
        "CURATOR cannot Edit" | CURATOR_USERNAME  | CURATOR_PASSWORD  | HttpServletResponse.SC_FORBIDDEN
    }

    def 'test save Project Document #desc'() {
        given:
        long projectId = documentData.projectId
        RESTClient client = getRestClient(controllerUrl, "save", team, teamPassword)

        when:
        def response = client.post() {
            urlenc projectId: projectId, documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.toString(), documentContent: "Test"
        }
        then:
        assert response.statusCode == expectedHttpResponse
        where:
        desc       | team              | teamPassword      | expectedHttpResponse
        "User A_1" | TEAM_A_1_USERNAME | TEAM_A_1_PASSWORD | HttpServletResponse.SC_OK
        "User A_2" | TEAM_A_2_USERNAME | TEAM_A_2_PASSWORD | HttpServletResponse.SC_OK
        "ADMIN"    | ADMIN_USERNAME    | ADMIN_PASSWORD    | HttpServletResponse.SC_OK
    }

    def 'test save Project Document #forbidden'() {
        given:
        long projectId = documentData.projectId
        RESTClient client = getRestClient(controllerUrl, "save", team, teamPassword)

        when:
        client.post() {
            urlenc projectId: projectId, documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.toString(), documentContent: "Test"
        }

        then:
        def ex = thrown(RESTClientException)
        assert ex.response.statusCode == expectedHttpResponse

        where:
        desc                  | team              | teamPassword      | expectedHttpResponse
        "User B cannot Edit"  | TEAM_B_1_USERNAME | TEAM_B_1_PASSWORD | HttpServletResponse.SC_FORBIDDEN
        "CURATOR cannot Edit" | CURATOR_USERNAME  | CURATOR_PASSWORD  | HttpServletResponse.SC_FORBIDDEN
    }

    def 'test edit Project Document Name #desc'() {
        given:
        final Map projectDocumentProperties = getCurrentProjectDocumentProperties()
        long projectId = documentData.projectId
        long projectDocumentId = documentData.projectDocumentId
        RESTClient client = getRestClient(controllerUrl, "editDocumentName", team, teamPassword)

        when:
        def response = client.post() {
            urlenc name: "name", owningEntityId: projectId, documentKind: DocumentKind.ProjectDocument.toString(),
                    version: projectDocumentProperties.version, documentName: "Project Doc", pk: projectDocumentId,
                    documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.id, value: "Test"
        }
        then:
        assert response.statusCode == expectedHttpResponse
        where:
        desc       | team              | teamPassword      | expectedHttpResponse
        "User A_1" | TEAM_A_1_USERNAME | TEAM_A_1_PASSWORD | HttpServletResponse.SC_OK
        "User A_2" | TEAM_A_2_USERNAME | TEAM_A_2_PASSWORD | HttpServletResponse.SC_OK
        "ADMIN"    | ADMIN_USERNAME    | ADMIN_PASSWORD    | HttpServletResponse.SC_OK
    }

    def 'test edit Project Document Name #forbidden'() {

        final Map projectDocumentProperties = getCurrentProjectDocumentProperties()
        long projectId = documentData.projectId
        long projectDocumentId = documentData.projectDocumentId
        RESTClient client = getRestClient(controllerUrl, "editDocumentName", team, teamPassword)

        when:
        client.post() {
            urlenc name: "name", owningEntityId: projectId, documentKind: DocumentKind.ProjectDocument.toString(),
                    version: projectDocumentProperties.version, documentName: "Project Doc", pk: projectDocumentId,
                    documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.id, value: "Test"
        }

        then:
        def ex = thrown(RESTClientException)
        assert ex.response.statusCode == expectedHttpResponse

        where:
        desc                  | team              | teamPassword      | expectedHttpResponse
        "User B cannot Edit"  | TEAM_B_1_USERNAME | TEAM_B_1_PASSWORD | HttpServletResponse.SC_FORBIDDEN
        "CURATOR cannot Edit" | CURATOR_USERNAME  | CURATOR_PASSWORD  | HttpServletResponse.SC_FORBIDDEN
    }


    def 'test edit Project Document #desc'() {
        given:
        final Map projectDocumentProperties = getCurrentProjectDocumentProperties()
        long projectId = documentData.projectId
        long projectDocumentId = documentData.projectDocumentId
        RESTClient client = getRestClient(controllerUrl, "editDocument", team, teamPassword)

        when:
        def response = client.post() {
            urlenc name: "name", owningEntityId: projectId, documentKind: DocumentKind.ProjectDocument.toString(),
                    version: projectDocumentProperties.version, documentName: "Project Doc", pk: projectDocumentId,
                    documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.id, value: "Test"
        }
        then:
        assert response.statusCode == expectedHttpResponse
        where:
        desc       | team              | teamPassword      | expectedHttpResponse
        "User A_1" | TEAM_A_1_USERNAME | TEAM_A_1_PASSWORD | HttpServletResponse.SC_OK
        "User A_2" | TEAM_A_2_USERNAME | TEAM_A_2_PASSWORD | HttpServletResponse.SC_OK
        "ADMIN"    | ADMIN_USERNAME    | ADMIN_PASSWORD    | HttpServletResponse.SC_OK
    }

    def 'test edit Project Document #forbidden'() {

        final Map projectDocumentProperties = getCurrentProjectDocumentProperties()
        long projectId = documentData.projectId
        long projectDocumentId = documentData.projectDocumentId
        RESTClient client = getRestClient(controllerUrl, "editDocument", team, teamPassword)

        when:
        client.post() {
            urlenc name: "name", owningEntityId: projectId, documentKind: DocumentKind.ProjectDocument.toString(),
                    version: projectDocumentProperties.version, documentName: "Project Doc", pk: projectDocumentId,
                    documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.id, value: "Test"
        }

        then:
        def ex = thrown(RESTClientException)
        assert ex.response.statusCode == expectedHttpResponse

        where:
        desc                  | team              | teamPassword      | expectedHttpResponse
        "User B cannot Edit"  | TEAM_B_1_USERNAME | TEAM_B_1_PASSWORD | HttpServletResponse.SC_FORBIDDEN
        "CURATOR cannot Edit" | CURATOR_USERNAME  | CURATOR_PASSWORD  | HttpServletResponse.SC_FORBIDDEN
    }


    def 'test create Assay Document #desc'() {
        given:
        long assayId = documentData.assayId
        RESTClient client = getRestClient(controllerUrl, "create", team, teamPassword)

        when:
        def response = client.post() {
            urlenc assayId: assayId, documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.toString()
        }
        then:
        assert response.statusCode == expectedHttpResponse
        where:
        desc       | team              | teamPassword      | expectedHttpResponse
        "User A_1" | TEAM_A_1_USERNAME | TEAM_A_1_PASSWORD | HttpServletResponse.SC_OK
        "User A_2" | TEAM_A_2_USERNAME | TEAM_A_2_PASSWORD | HttpServletResponse.SC_OK
        "ADMIN"    | ADMIN_USERNAME    | ADMIN_PASSWORD    | HttpServletResponse.SC_OK
    }

    def 'test create Assay Document #forbidden'() {
        given:
        long assayId = documentData.assayId
        RESTClient client = getRestClient(controllerUrl, "create", team, teamPassword)

        when:
        client.post() {
            urlenc assayId: assayId, documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.toString()
        }

        then:
        def ex = thrown(RESTClientException)
        assert ex.response.statusCode == expectedHttpResponse

        where:
        desc                  | team              | teamPassword      | expectedHttpResponse
        "User B cannot Edit"  | TEAM_B_1_USERNAME | TEAM_B_1_PASSWORD | HttpServletResponse.SC_FORBIDDEN
        "CURATOR cannot Edit" | CURATOR_USERNAME  | CURATOR_PASSWORD  | HttpServletResponse.SC_FORBIDDEN
    }

    def 'test save Assay Document #desc'() {
        given:
        long assayId = documentData.assayId
        RESTClient client = getRestClient(controllerUrl, "save", team, teamPassword)

        when:
        def response = client.post() {
            urlenc assayId: assayId, documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.toString(), documentContent: "Test"
        }
        then:
        assert response.statusCode == expectedHttpResponse
        where:
        desc       | team              | teamPassword      | expectedHttpResponse
        "User A_1" | TEAM_A_1_USERNAME | TEAM_A_1_PASSWORD | HttpServletResponse.SC_OK
        "User A_2" | TEAM_A_2_USERNAME | TEAM_A_2_PASSWORD | HttpServletResponse.SC_OK
        "ADMIN"    | ADMIN_USERNAME    | ADMIN_PASSWORD    | HttpServletResponse.SC_OK
    }

    def 'test save Assay Document #forbidden'() {
        given:
        long assayId = documentData.assayId
        RESTClient client = getRestClient(controllerUrl, "save", team, teamPassword)

        when:
        client.post() {
            urlenc assayId: assayId, documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.toString(), documentContent: "Test"
        }

        then:
        def ex = thrown(RESTClientException)
        assert ex.response.statusCode == expectedHttpResponse

        where:
        desc                  | team              | teamPassword      | expectedHttpResponse
        "User B cannot Edit"  | TEAM_B_1_USERNAME | TEAM_B_1_PASSWORD | HttpServletResponse.SC_FORBIDDEN
        "CURATOR cannot Edit" | CURATOR_USERNAME  | CURATOR_PASSWORD  | HttpServletResponse.SC_FORBIDDEN
    }

    def 'test edit Assay Document #desc'() {
        given:
        final Map assayDocumentProperties = getCurrentAssayDocumentProperties()
        long assayId = documentData.assayId
        long assayDocumentId = documentData.assayDocumentId
        RESTClient client = getRestClient(controllerUrl, "editDocument", team, teamPassword)

        when:
        def response = client.post() {
            urlenc name: "name", owningEntityId: assayId, documentKind: DocumentKind.AssayDocument.toString(),
                    version: assayDocumentProperties.version, documentName: "Assay Doc", pk: assayDocumentId,
                    documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.id, value: "Test"
        }
        then:
        assert response.statusCode == expectedHttpResponse
        where:
        desc       | team              | teamPassword      | expectedHttpResponse
        "User A_1" | TEAM_A_1_USERNAME | TEAM_A_1_PASSWORD | HttpServletResponse.SC_OK
        "User A_2" | TEAM_A_2_USERNAME | TEAM_A_2_PASSWORD | HttpServletResponse.SC_OK
        "ADMIN"    | ADMIN_USERNAME    | ADMIN_PASSWORD    | HttpServletResponse.SC_OK
    }

    def 'test edit Assay Document #forbidden'() {

        given:
        final Map assayDocumentProperties = getCurrentAssayDocumentProperties()
        long assayId = documentData.assayId
        long assayDocumentId = documentData.assayDocumentId
        RESTClient client = getRestClient(controllerUrl, "editDocument", team, teamPassword)

        when:
        client.post() {
            urlenc name: "name", owningEntityId: assayId, documentKind: DocumentKind.AssayDocument.toString(),
                    version: assayDocumentProperties.version, documentName: "Assay Doc", pk: assayDocumentId,
                    documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.id, value: "Test"
        }

        then:
        def ex = thrown(RESTClientException)
        assert ex.response.statusCode == expectedHttpResponse

        where:
        desc                  | team              | teamPassword      | expectedHttpResponse
        "User B cannot Edit"  | TEAM_B_1_USERNAME | TEAM_B_1_PASSWORD | HttpServletResponse.SC_FORBIDDEN
        "CURATOR cannot Edit" | CURATOR_USERNAME  | CURATOR_PASSWORD  | HttpServletResponse.SC_FORBIDDEN
    }

    def 'test edit Assay Document Name #desc'() {
        given:
        final Map assayDocumentProperties = getCurrentAssayDocumentProperties()
        long assayId = documentData.assayId
        long assayDocumentId = documentData.assayDocumentId
        RESTClient client = getRestClient(controllerUrl, "editDocumentName", team, teamPassword)

        when:
        def response = client.post() {
            urlenc name: "name", owningEntityId: assayId, documentKind: DocumentKind.AssayDocument.toString(),
                    version: assayDocumentProperties.version, documentName: "Assay Doc", pk: assayDocumentId,
                    documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.id, value: "Test"
        }
        then:
        assert response.statusCode == expectedHttpResponse
        where:
        desc       | team              | teamPassword      | expectedHttpResponse
        "User A_1" | TEAM_A_1_USERNAME | TEAM_A_1_PASSWORD | HttpServletResponse.SC_OK
        "User A_2" | TEAM_A_2_USERNAME | TEAM_A_2_PASSWORD | HttpServletResponse.SC_OK
        "ADMIN"    | ADMIN_USERNAME    | ADMIN_PASSWORD    | HttpServletResponse.SC_OK
    }

    def 'test edit Assay Document Name #forbidden'() {

        final Map assayDocumentProperties = getCurrentAssayDocumentProperties()
        long assayId = documentData.assayId
        long assayDocumentId = documentData.assayDocumentId
        RESTClient client = getRestClient(controllerUrl, "editDocumentName", team, teamPassword)

        when:
        client.post() {
            urlenc name: "name", owningEntityId: assayId, documentKind: DocumentKind.AssayDocument.toString(),
                    version: assayDocumentProperties.version, documentName: "Assay Doc", pk: assayDocumentId,
                    documentType: DocumentType.DOCUMENT_TYPE_COMMENTS.id, value: "Test"
        }

        then:
        def ex = thrown(RESTClientException)
        assert ex.response.statusCode == expectedHttpResponse

        where:
        desc                  | team              | teamPassword      | expectedHttpResponse
        "User B cannot Edit"  | TEAM_B_1_USERNAME | TEAM_B_1_PASSWORD | HttpServletResponse.SC_FORBIDDEN
        "CURATOR cannot Edit" | CURATOR_USERNAME  | CURATOR_PASSWORD  | HttpServletResponse.SC_FORBIDDEN
    }

    private Map getCurrentAssayDocumentProperties() {
        long id = documentData.assayDocumentId
        Map currentDataMap = (Map) remote.exec({
            AssayDocument assayDocument = AssayDocument.findById(id)
            return [version: assayDocument.version]
        })
        return currentDataMap
    }

    private Map getCurrentProjectDocumentProperties() {
        long id = documentData.projectDocumentId
        Map currentDataMap = (Map) remote.exec({
            ProjectDocument projectDocument = ProjectDocument.findById(id)
            return [version: projectDocument.version]
        })
        return currentDataMap
    }
}
