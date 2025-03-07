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

package bard.db.context.item

import bard.db.ContextService
import bard.db.command.BardCommand
import bard.db.experiment.Experiment
import bard.db.model.AbstractContext
import bard.db.model.AbstractContextItem
import bard.db.model.AbstractContextOwner
import bard.db.project.InlineEditableCommand
import bard.db.project.Project
import bard.db.registration.Assay
import bard.db.registration.EditingHelper
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import grails.plugins.springsecurity.SpringSecurityService
import grails.validation.Validateable
import groovy.transform.InheritConstructors

import javax.servlet.http.HttpServletResponse

import org.springframework.security.access.AccessDeniedException

import static org.codehaus.groovy.grails.web.servlet.HttpHeaders.REFERER

@Mixin(EditingHelper)
@Secured(['isAuthenticated()'])
class ContextItemController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST", updatePreferredName: "POST"]
    ContextService contextService
    def permissionEvaluator
    SpringSecurityService springSecurityService

    def index() {}

    def create(Long contextId, String contextClass, Long contextOwnerId) {
        BasicContextItemCommand command = new BasicContextItemCommand(contextId: contextId, contextClass: contextClass, contextOwnerId: contextOwnerId)

        command.context = command.attemptFindContext()
        if (command.context) {
            boolean canCreateOrEdit = canEdit(permissionEvaluator, springSecurityService, command?.context?.getOwner())
            if (!canCreateOrEdit) {
                render accessDeniedErrorMessage()
                return
            }
        }
        [instance: command]
    }

    def save(BasicContextItemCommand contextItemCommand) {
        def context = contextItemCommand.attemptFindById(contextItemCommand.getContextClass(contextItemCommand.contextClass), contextItemCommand.contextId)
        if (context) {
            boolean canCreateOrEdit = canEdit(permissionEvaluator, springSecurityService, context.getOwner())
            if (!canCreateOrEdit) {
                render accessDeniedErrorMessage()
                return
            }
        }

        if (contextItemCommand.createNewContextItem() && !contextItemCommand.hasErrors()) {
            render(view: "edit", model: [instance: contextItemCommand, reviewNewItem: true])
        } else {
            render(view: "create", model: [instance: contextItemCommand])
        }
    }

    def edit(BasicContextItemCommand contextItemCommand) {
        AbstractContextItem contextItem = contextItemCommand.attemptFindItem()

        if (!contextItem) {
            render(view: "edit", model: [instance: contextItemCommand])
        } else {
            render(view: "edit", model: [instance: new BasicContextItemCommand(contextItem)])
        }
    }

    def update(BasicContextItemCommand contextItemCommand) {
        contextItemCommand.context = contextItemCommand.attemptFindContext()

        if (!contextItemCommand.update()) {
            render(view: "edit", model: [instance: contextItemCommand])
        } else {
            render(view: "edit", model: [instance: contextItemCommand, reviewNewItem: true])
        }
    }

    def delete(BasicContextItemCommand basicContextItemCommand) {

        basicContextItemCommand.delete()

        if (request.getHeader(REFERER)?.contains('/contextItem/')) {
            redirect(action: 'create', params: params)
        } else {
            redirect(controller: basicContextItemCommand.ownerController, action: "show", fragment: "card-${basicContextItemCommand.contextId}",
                    params: [id: basicContextItemCommand.contextOwnerId, groupBySection: basicContextItemCommand.context?.contextType.id])
        }

    }

    def updatePreferredName(InlineEditableCommand inlineEditableCommand) {
        attemptUpdate {
            InlineUpdateCommand command = new InlineUpdateCommand(inlineEditableCommand)
            AbstractContext context = BasicContextItemCommand.getContextClass(command.contextClass).findById(command.id)
            AbstractContextOwner owningContext = context.owner
            if (owningContext instanceof Assay) {
                final String name = contextService.updatePreferredAssayContextName(owningContext.id, context, command.value)
                render(status: HttpServletResponse.SC_OK, text: name, contentType: 'text/plain', template: null)
                return
            }
            if (owningContext instanceof Experiment) {
                String name = contextService.updatePreferredExperimentContextName(owningContext.id, context, command.value)
                render(status: HttpServletResponse.SC_OK, text: name, contentType: 'text/plain', template: null)
                return
            }
            if (owningContext instanceof Project) {
                String name = contextService.updatePreferredProjectContextName(owningContext.id, context, command.value)
                render(status: HttpServletResponse.SC_OK, text: name, contentType: 'text/plain', template: null)
                return
            }

            render(status: HttpServletResponse.SC_BAD_REQUEST, text: context.preferredName, contentType: 'text/plain', template: null)
        }
    }


    private Object attemptUpdate(Closure body) {
        try {
            def newValue = body.call()

            JSON jsonResponse = [data: newValue] as JSON
            render status: HttpServletResponse.SC_OK, text: jsonResponse, contentType: 'text/json', template: null
        }
        catch (AccessDeniedException ae) {
            log.error("Access denied", ae)
            render(status: HttpServletResponse.SC_FORBIDDEN, text: message(code: 'editing.forbidden.message'), contentType: 'text/plain', template: null)

        }
        catch (Exception ee) {
            log.error("update failed", ee)
            render(status: HttpServletResponse.SC_INTERNAL_SERVER_ERROR, text: message(code: 'editing.error.message'), contentType: 'text/plain', template: null)
        }
    }
}

@InheritConstructors
@Validateable
class InlineUpdateCommand extends BardCommand {
    Long id //primary key of the current entity
    String contextClass
    String value //the new value

    InlineUpdateCommand() {}

    InlineUpdateCommand(InlineEditableCommand inlineEditableCommand) {
        id = inlineEditableCommand.pk
        contextClass = inlineEditableCommand.name
        value = inlineEditableCommand.value
    }
    static constraints = {
        id(blank: false, nullable: false)
        contextClass(blank: false, nullable: false, inList: ["AssayContext", "ProjectContext"])
        value(blank: false, nullable: false)
    }
}

@InheritConstructors
@Validateable
class DeleteContextCommand extends BardCommand {
    Long id //primary key of the current entity
    String contextClass
    String value //the new value

    static constraints = {
        id(blank: false, nullable: false)
        contextClass(blank: false, nullable: false, inList: ["AssayContext", "ProjectContext"])
    }
}

