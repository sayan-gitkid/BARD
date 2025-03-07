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

import bard.db.command.BardCommand
import bard.db.enums.DocumentType
import bard.db.experiment.Experiment
import bard.db.experiment.ExperimentDocument
import bard.db.model.AbstractDocument
import bard.db.project.InlineEditableCommand
import bard.db.project.Project
import bard.db.project.ProjectDocument
import grails.plugins.springsecurity.Secured
import grails.validation.Validateable
import groovy.transform.InheritConstructors
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.Errors
import org.springframework.validation.ObjectError

import javax.servlet.http.HttpServletResponse

@Mixin([DocumentHelper, EditingHelper])
@Secured(['isAuthenticated()'])
class DocumentController {
    static allowedMethods = [save: "POST"]

    Map<String, Class> nameToDomain = ["Assay": AssayDocument, "Project": ProjectDocument, "Experiment": ExperimentDocument]
    DocumentService documentService
    def permissionEvaluator
    def springSecurityService


    def create(DocumentCommand documentCommand) {
        documentCommand.clearErrors()
        def entity = null
        if (documentCommand.assayId) {
            entity = Assay.findById(documentCommand.assayId)
        } else if (documentCommand.projectId) {
            entity = Project.findById(documentCommand.projectId)
        } else if (documentCommand.experimentId) {
            entity = Experiment.findById(documentCommand.experimentId)
        }
        if (!canEdit(permissionEvaluator, springSecurityService, entity)) {
            render accessDeniedErrorMessage()
            return
        }
        [document: documentCommand]
    }

    def save(DocumentCommand documentCommand) {

        if (!documentCommand.documentType) {
            documentCommand.documentType = DocumentType.byId(params.documentType)
        }
        def entity = null
        if (documentCommand.assayId) {
            entity = Assay.findById(documentCommand.assayId)
        } else if (documentCommand.projectId) {
            entity = Project.findById(documentCommand.projectId)
        } else if (documentCommand.experimentId) {
            entity = Experiment.findById(documentCommand.experimentId)
        }
        if (!canEdit(permissionEvaluator, springSecurityService, entity)) {
            render accessDeniedErrorMessage()
            return
        }
        documentCommand.documentContent = documentCommand.removeHtmlLineBreaks()

        Object document = documentCommand.createNewDocument()
        if (document) {
            redirectToOwner(document)
        } else {
            render(view: "create", model: [document: documentCommand])
        }
    }
    /**
     * No longer used. remove method and corresponding gsp
     * @param type
     * @param id
     * @return
     */
    def edit(String type, Long id) {
        DocumentCommand dc = new DocumentCommand()
        Class domainClass = nameToDomain[type]
        if (domainClass == null) {
            throw new RuntimeException("Not a valid value ${domainClass}")
        }
        dc.populateWithExistingDocument(domainClass, id)
        [document: dc]
    }

    def editDocument(InlineEditableCommand inlineEditableCommand) {
        if (!inlineEditableCommand.validate()) {
            render status: HttpServletResponse.SC_BAD_REQUEST, text: "Field is required and must not be empty", contentType: 'text/plain', template: null
        } else {
            DocumentKind documentKind = params.documentKind as DocumentKind
            DocumentCommand documentCommand =
                createDocumentCommand(
                        params.documentName,
                        inlineEditableCommand.pk,
                        inlineEditableCommand.value.trim(),
                        DocumentType.byId(params.documentType),
                        inlineEditableCommand.version,
                        inlineEditableCommand.owningEntityId,
                        documentKind
                )
            def entity = null
            if (DocumentKind.AssayDocument == documentKind) {
                entity = Assay.findById(documentCommand.assayId)
            } else if (DocumentKind.ProjectDocument == documentKind) {
                entity = Project.findById(documentCommand.projectId)
            } else if (DocumentKind.ExperimentDocument == documentKind) {
                entity = Experiment.findById(documentCommand.experimentId)
            }
            if (entity) {
                if (!canEdit(permissionEvaluator, springSecurityService, entity)) {
                    render accessDeniedErrorMessage()
                    return
                }
            } else {
                throw new RuntimeException("Could not find owning entity with id ${inlineEditableCommand.owningEntityId}")
            }
            render(renderDocument(documentCommand,grailsApplication))
        }

    }

    def editDocumentName(InlineEditableCommand inlineEditableCommand) {
        if (!inlineEditableCommand.validate()) {
            render status: HttpServletResponse.SC_BAD_REQUEST, text: "Field is required and must not be empty", contentType: 'text/plain', template: null
        } else {
            DocumentKind documentKind = params.documentKind as DocumentKind
            DocumentCommand documentCommand =
                createDocumentCommand(
                        inlineEditableCommand.value.trim(),
                        inlineEditableCommand.pk,
                        params.documentName,
                        DocumentType.byId(params.documentType),
                        inlineEditableCommand.version,
                        inlineEditableCommand.owningEntityId,
                        documentKind
                )
            def entity = null
            if (DocumentKind.AssayDocument == documentKind) {
                entity = Assay.findById(documentCommand.assayId)
            } else if (DocumentKind.ProjectDocument == documentKind) {
                entity = Project.findById(documentCommand.projectId)
            } else if (DocumentKind.ExperimentDocument == documentKind) {
                entity = Experiment.findById(documentCommand.experimentId)
            }
            if (entity) {
                if (!canEdit(permissionEvaluator, springSecurityService, entity)) {
                    render accessDeniedErrorMessage()
                    return
                }
            }
            render(renderDocument(documentCommand,grailsApplication))
        }

    }

    def update(DocumentCommand documentCommand) {
        documentCommand.documentType = DocumentType.byId(params.documentType)
        Object document = documentCommand.updateExistingDocument()
        if (document) {
            redirectToOwner(document)
        } else {
            render(view: "edit", model: [document: documentCommand])
        }
    }

    def delete(String type, Long id) {
        Class domainClass = nameToDomain[type]
        def document = domainClass.get(id)
        if (!document) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'assayDocument.label', default: 'AssayDocument'), params.id])
            return
        }
        try {
            if (document instanceof AssayDocument) {
                documentService.deleteAssayDocument(document.assay.id, document)
            } else if (document instanceof ProjectDocument) {
                documentService.deleteProjectDocument(document.project.id, document)
            } else if (document instanceof ExperimentDocument) {
                documentService.deleteExperimentDocument(document.experiment.id, document)
            } else {
                throw new RuntimeException("UnHandled Document Type " + domainClass.toString())
            }
        } catch (AccessDeniedException ade) {
            render accessDeniedErrorMessage()
        }
        redirectToOwner(document)
    }

    private def redirectToOwner(document) {
        if (document.getOwner() instanceof Assay) {
            Assay assay = document.getOwner()
            redirect(controller: "assayDefinition", action: "show", id: assay.id, fragment: "document-${document.id}")
        } else if (document.getOwner() instanceof Project) {
            Project project = document.getOwner()
            redirect(controller: "project", action: "show", id: project.id, fragment: "document-${document.id}")
        } else if (document.getOwner() instanceof Experiment) {
            Experiment experiment = document.getOwner()
            redirect(controller: "experiment", action: "show", id: experiment.id, fragment: "document-${document.id}")
        } else {
            throw new RuntimeException("document owner ${document.getOwner()} is neither an assay, project or experiment")
        }
    }
}

class DocumentHelper {


    def renderDocument(DocumentCommand documentCommand,GrailsApplication grailsApplication) {
        final String DOCUMENT_INTERNAL_SERVER_ERROR = "An internal server error occurred while you were editing this page. Please refresh your browser and try again. If you still encounter issues please report it to the BARD team ${grailsApplication.config.bard.users.email}"

        try {
            def document = documentCommand.updateExistingDocument()
            if (documentCommand.hasErrors()) {
                final Errors errors = documentCommand.errors
                final String message = errors.allErrors.collect{ObjectError objectError -> g.message(code: objectError.code)}.join('\n')
                return [status: HttpServletResponse.SC_BAD_REQUEST, text: message, contentType: 'text/plain', template: null]
            } else {
                response.setHeader('version', document.version.toString())
                response.setHeader('entityId', document.id.toString())
                if (document.documentType == DocumentType.DOCUMENT_TYPE_EXTERNAL_URL || document.documentType == DocumentType.DOCUMENT_TYPE_PUBLICATION) {
                    return [status: HttpServletResponse.SC_OK, text: "${document.documentName}", contentType: 'text/plain', template: null]
                } else {
                    return [status: HttpServletResponse.SC_OK, template: "/document/docsWithLineBreaks", model: [documentContent: document.documentContent]]
                }
            }
        } catch (Exception ee) {
            log.error(ee,ee)
            return [status: HttpServletResponse.SC_INTERNAL_SERVER_ERROR, text: DOCUMENT_INTERNAL_SERVER_ERROR, contentType: 'text/plain', template: null]
        }
    }
    /**
     *
     * @param documentName
     * @param documentId
     * @param documentContent
     * @param documentType
     * @param version
     * @param owningEntityId
     * @param currentDocumentKind
     * @return {@link DocumentCommand}
     */
    DocumentCommand createDocumentCommand(final String documentName,
                                          final Long documentId,
                                          final String documentContent,
                                          final DocumentType documentType,
                                          final Long version,
                                          final Long owningEntityId,
                                          final DocumentKind currentDocumentKind) {
        Long assayId = null
        Long projectId = null
        Long experimentId = null
        if (DocumentKind.AssayDocument == currentDocumentKind) {
            assayId = owningEntityId
        }
        if (DocumentKind.ProjectDocument == currentDocumentKind) {
            projectId = owningEntityId
        }
        if (DocumentKind.ExperimentDocument == currentDocumentKind) {
            experimentId = owningEntityId
        }

        return new DocumentCommand(
                documentName: documentName,
                documentId: documentId,
                documentContent: documentContent,
                documentType: documentType,
                version: version,
                assayId: assayId,
                projectId: projectId,
                experimentId: experimentId
        )
    }
}
@InheritConstructors
@Validateable
class DocumentCommand extends BardCommand {

    Long assayId
    Long projectId
    Long experimentId

    Long documentId
    Long version
    String documentName
    DocumentType documentType
    String documentContent
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    public static final List<String> PROPS_FROM_CMD_TO_DOMAIN = ['version', 'documentName', 'documentType', 'documentContent'].asImmutable()
    public static final List<String> PROPS_FROM_DOMAIN_TO_CMD = [PROPS_FROM_CMD_TO_DOMAIN, 'dateCreated', 'lastUpdated', 'modifiedBy'].flatten().asImmutable()

    static constraints = {
        importFrom(AssayDocument, exclude: ['dateCreated', 'lastUpdated', 'modifiedBy'])
        assayId(nullable: true)
        projectId(nullable: true)
        experimentId(nullable: true)
        ownerController(nullable: false, blank: false)
        documentId(nullable: true, validator: { val, self, errors ->
            if (self.assayId == null && self.documentId == null && self.projectId == null && self.experimentId == null) {
                errors.rejectValue('documentId', 'nullable')
                return false
            }
        })
        version(nullable: true)
    }

    DocumentCommand() {}

    DocumentCommand(AssayDocument assayDocument) {
        copyFromDomainToCmd(assayDocument)
    }

    AbstractDocument createNewDocument() {
        AbstractDocument documentToReturn = null
        if (validate()) {
            AbstractDocument doc
            if (assayId) {
                doc = new AssayDocument()
                doc.assay = attemptFindById(Assay, assayId)
            }
            if (projectId) {
                doc = new ProjectDocument()
                doc.project = attemptFindById(Project, projectId)
            }
            if (experimentId) {
                doc = new ExperimentDocument()
                doc.experiment = attemptFindById(Experiment, experimentId)
            }
            copyFromCmdToDomain(doc)
            if (attemptSave(doc)) {
                documentToReturn = doc
            }
        }
        documentToReturn
    }

    void populateWithExistingDocument(final Class domainClass, final Long id) {
        AbstractDocument document = attemptFindById(domainClass, id)
        if (document) {
            copyFromDomainToCmd(document)
        }
    }

    AbstractDocument updateExistingDocument() {
        AbstractDocument document = null
        if (validate()) {
            if (assayId != null) {
                document = attemptFindById(AssayDocument, documentId)
            } else if (projectId != null) {
                document = attemptFindById(ProjectDocument, documentId)
            } else if (experimentId != null) {
                document = attemptFindById(ExperimentDocument, documentId)
            } else {
                throw new RuntimeException("Neither assayId, projectId or experimentId was provided")
            }

            if (document) {
                if (this.version?.longValue() != document.version.longValue()) {
                    getErrors().reject('default.optimistic.locking.failure', [AbstractDocument] as Object[], 'optimistic lock failure')
                    copyFromDomainToCmd(document)
                    document = null
                } else {
                    copyFromCmdToDomain(document)
                    if (!attemptSave(document)) {
                        document = null
                    }
                }
            }
        }
        return document
    }







    void copyFromCmdToDomain(AbstractDocument document) {
        for (String field in PROPS_FROM_CMD_TO_DOMAIN) {
            document[(field)] = this[(field)]
        }
    }

    DocumentCommand copyFromDomainToCmd(AbstractDocument document) {
        assert document
        if (document instanceof AssayDocument) {
            this.assayId = document.assay.id
        }
        if (document instanceof ProjectDocument) {
            this.projectId = document.project.id
        }
        if (document instanceof ExperimentDocument) {
            this.experimentId = document.experiment.id
        }
        this.documentId = document.id
        for (String field in PROPS_FROM_DOMAIN_TO_CMD) {
            this[(field)] = document[(field)]
        }
        return this
    }

/**
 * Hack to determine the id of owning to forward to given presence of assay or project ids
 * @return
 */
    Long getOwnerId() {
        if (projectId) {
            return projectId
        } else if (assayId) {
            return assayId
        } else if (experimentId) {
            return experimentId
        } else {
            throw new RuntimeException('need either a projectId, assayId or experimentId to determine ownerId')
        }
    }
    /**
     * Hack to determine the controller to forward to given presence of assay or project ids
     * @return
     */
    String getOwnerController() {
        if (projectId) {
            return 'project'
        } else if (assayId) {
            return 'assayDefinition'
        } else if (experimentId) {
            return 'experiment'
        } else {
            throw new RuntimeException('need either a projectId, assayId or experimentId to determine owner')
        }
    }

    String removeHtmlLineBreaks() {

        if (this.documentType == DocumentType.DOCUMENT_TYPE_EXTERNAL_URL ||
                this.documentType == DocumentType.DOCUMENT_TYPE_PUBLICATION) {
            String content = this.documentContent
            return content?.replaceAll("<div>", "")?.replaceAll("</div>", "")?.replaceAll("<br>", "")?.replaceAll("\n", "")?.replaceAll("\r", "")?.trim()
        }
        return this.documentContent
    }
}
enum DocumentKind {
    AssayDocument,
    ProjectDocument,
    ExperimentDocument
}
