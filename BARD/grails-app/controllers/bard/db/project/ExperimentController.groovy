package bard.db.project

import acl.CapPermissionService
import bard.db.enums.ContextType
import bard.db.enums.ExperimentStatus
import bard.db.experiment.Experiment
import bard.db.experiment.ExperimentService
import bard.db.experiment.AsyncResultsService
import bard.db.experiment.results.JobStatus
import bard.db.model.AbstractContextOwner
import bard.db.people.Role
import bard.db.registration.Assay
import bard.db.registration.AssayDefinitionService
import bard.db.registration.EditingHelper
import bard.db.registration.MeasureTreeService
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import grails.plugins.springsecurity.SpringSecurityService
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.access.AccessDeniedException

import java.text.DateFormat
import java.text.SimpleDateFormat


@Mixin([EditingHelper])
@Secured(['isAuthenticated()'])
class ExperimentController {
    static final DateFormat inlineDateFormater = new SimpleDateFormat("yyyy-MM-dd")

    ExperimentService experimentService
    AssayDefinitionService assayDefinitionService
    MeasureTreeService measureTreeService
    SpringSecurityService springSecurityService
    def permissionEvaluator
    CapPermissionService capPermissionService
    AsyncResultsService asyncResultsService

    def myExperiments() {
        List<Experiment> experiments = capPermissionService.findAllObjectsForRoles(Experiment)
        Set<Experiment> uniqueExperiments = new HashSet<Experiment>(experiments)
        [experiments: uniqueExperiments]
    }
    def create() {
        def assay = Assay.get(params.assayId)
        render renderEditFieldsForView("create", new Experiment(), assay);
    }

    def edit() {
        def experiment = Experiment.get(params.id)
        render renderEditFieldsForView("edit", experiment, experiment.assay);
    }

    /**
     * Draft is excluded as End users cannot set a status back to Draft
     * @return  list of strings representing available status options
     */
    def experimentStatus() {
        List<String> sorted = []
        final Collection<ExperimentStatus> experimentStatuses = ExperimentStatus.values()
        for (ExperimentStatus experimentStatus : experimentStatuses) {
            if (experimentStatus != ExperimentStatus.DRAFT) {
                sorted.add(experimentStatus.id)
            }
        }
        sorted.sort()
        final JSON json = sorted as JSON
        render text: json, contentType: 'text/json', template: null
    }

    def show() {
        def experimentInstance = Experiment.get(params.id)
        if (!experimentInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'experiment.label', default: 'Experiment'), params.id])
            return
        }

        JSON measuresAsJsonTree = new JSON(measureTreeService.createMeasureTree(experimentInstance, false))

        JSON assayMeasuresAsJsonTree = new JSON([])
            //new JSON(measureTreeService.createMeasureTree(experimentInstance.assay, false))
        boolean editable = canEdit(permissionEvaluator, springSecurityService, experimentInstance)
        boolean isAdmin = SpringSecurityUtils.ifAnyGranted('ROLE_BARD_ADMINISTRATOR')
        String owner = capPermissionService.getOwner(experimentInstance)
        [instance: experimentInstance,
                experimentOwner: owner,
                measuresAsJsonTree: measuresAsJsonTree,
                assayMeasuresAsJsonTree: assayMeasuresAsJsonTree,
                editable: editable ? 'canedit' : 'cannotedit',
                isAdmin: isAdmin]
    }

    def editHoldUntilDate(InlineEditableCommand inlineEditableCommand) {
        try {
            Experiment experiment = Experiment.findById(inlineEditableCommand.pk)
            final String message = inlineEditableCommand.validateVersions(experiment.version, Experiment.class)
            if (message) {
                conflictMessage(message)
                return
            }
            //format the date
            Date holdUntilDate = inlineDateFormater.parse(inlineEditableCommand.value)


            experiment = experimentService.updateHoldUntilDate(inlineEditableCommand.pk, holdUntilDate)
            final String updatedDateAsString = formatter.format(experiment.holdUntilDate)
            generateAndRenderJSONResponse(experiment.version, experiment.modifiedBy, null, experiment.lastUpdated, updatedDateAsString)
        } catch (AccessDeniedException ade) {
            log.error(ade)
            render accessDeniedErrorMessage()
            return
        } catch (Exception ee) {
            log.error(ee)
            editErrorMessage()
        }
    }

    def editRunFromDate(InlineEditableCommand inlineEditableCommand) {
        try {
            Experiment experiment = Experiment.findById(inlineEditableCommand.pk)
            final String message = inlineEditableCommand.validateVersions(experiment.version, Experiment.class)
            if (message) {
                conflictMessage(message)
                return
            }
            //format the date
            Date runFromDate = inlineDateFormater.parse(inlineEditableCommand.value)


            experiment = experimentService.updateRunFromDate(inlineEditableCommand.pk, runFromDate)
            final String updatedDateAsString = formatter.format(experiment.runDateFrom)
            generateAndRenderJSONResponse(experiment.version, experiment.modifiedBy, null, experiment.lastUpdated, updatedDateAsString)
        } catch (AccessDeniedException ade) {
            log.error(ade)
            render accessDeniedErrorMessage()
            return
        } catch (Exception ee) {
            log.error(ee)
            editErrorMessage()
        }
    }

    def editRunToDate(InlineEditableCommand inlineEditableCommand) {
        try {
            Experiment experiment = Experiment.findById(inlineEditableCommand.pk)
            final String message = inlineEditableCommand.validateVersions(experiment.version, Experiment.class)
            if (message) {
                conflictMessage(message)
                return
            }
            //format the date
            Date runToDate = inlineDateFormater.parse(inlineEditableCommand.value)


            experiment = experimentService.updateRunToDate(inlineEditableCommand.pk, runToDate)
            final String updatedDateAsString = formatter.format(experiment.runDateTo)
            generateAndRenderJSONResponse(experiment.version, experiment.modifiedBy, null, experiment.lastUpdated, updatedDateAsString)
        } catch (AccessDeniedException ade) {
            log.error(ade)
            render accessDeniedErrorMessage()
            return
        } catch (Exception ee) {
            log.error(ee)
            editErrorMessage()
        }
    }

    def editDescription(InlineEditableCommand inlineEditableCommand) {
        try {
            Experiment experiment = Experiment.findById(inlineEditableCommand.pk)
            final String message = inlineEditableCommand.validateVersions(experiment.version, Experiment.class)
            if (message) {
                conflictMessage(message)
                return
            }

            final String inputValue = inlineEditableCommand.value.trim()
            String maxSizeMessage = validateInputSize(Experiment.DESCRIPTION_MAX_SIZE, inputValue.length())
            if(maxSizeMessage){
                editExceedsLimitErrorMessage(maxSizeMessage)
                return
            }
            experiment = experimentService.updateExperimentDescription(inlineEditableCommand.pk, inputValue)

            if(experiment?.hasErrors()){
                throw new Exception("Error while editing Experiment Description")
            }

            generateAndRenderJSONResponse(experiment.version, experiment.modifiedBy, null, experiment.lastUpdated, experiment.description)

        } catch (AccessDeniedException ade) {
            log.error(ade)
            render accessDeniedErrorMessage()
        } catch (Exception ee) {
            log.error(ee)
            editErrorMessage()
        }
    }

    def editExperimentName(InlineEditableCommand inlineEditableCommand) {
        try {
            Experiment experiment = Experiment.findById(inlineEditableCommand.pk)
            final String message = inlineEditableCommand.validateVersions(experiment.version, Experiment.class)
            if (message) {
                conflictMessage(message)
                return
            }

            final String inputValue = inlineEditableCommand.value.trim()
            String maxSizeMessage = validateInputSize(Experiment.DESCRIPTION_MAX_SIZE, inputValue.length())
            if(maxSizeMessage){
                editExceedsLimitErrorMessage(maxSizeMessage)
                return
            }
            experiment = experimentService.updateExperimentName(inlineEditableCommand.pk, inputValue)
            if(experiment?.hasErrors()){
                throw new Exception("Error while editing Experiment Name")
            }

            generateAndRenderJSONResponse(experiment.version, experiment.modifiedBy, null, experiment.lastUpdated, experiment.experimentName)

        } catch (AccessDeniedException ade) {
            log.error(ade)
            render accessDeniedErrorMessage()
        } catch (Exception ee) {
            log.error(ee)
            editErrorMessage()
        }
    }

//    def editOwnerRole(InlineEditableCommand inlineEditableCommand) {
//        try {
//            final Role ownerRole = Role.findById(inlineEditableCommand.value)
//            final Experiment experiment = Experiment.findById(inlineEditableCommand.pk)
//            final String message = inlineEditableCommand.validateVersions(experiment.version, Experiment.class)
//            if (message) {
//                conflictMessage(message)
//                return
//            }
//            experiment = experimentService.updateOwnerRole(inlineEditableCommand.pk, ownerRole)
//            generateAndRenderJSONResponse(experiment.version, experiment.modifiedBy, null, experiment.lastUpdated, experiment.experimentStatus.id)
//
//        } catch (AccessDeniedException ade) {
//            log.error(ade)
//            render accessDeniedErrorMessage()
//        } catch (Exception ee) {
//            log.error(ee)
//            editErrorMessage()
//        }
//    }

    def editExperimentStatus(InlineEditableCommand inlineEditableCommand) {
        try {
            final ExperimentStatus experimentStatus = ExperimentStatus.byId(inlineEditableCommand.value)
            final Experiment experiment = Experiment.findById(inlineEditableCommand.pk)
            final String message = inlineEditableCommand.validateVersions(experiment.version, Experiment.class)
            if (message) {
                conflictMessage(message)
                return
            }
            experiment = experimentService.updateExperimentStatus(inlineEditableCommand.pk, experimentStatus)
            generateAndRenderJSONResponse(experiment.version, experiment.modifiedBy, null, experiment.lastUpdated, experiment.experimentStatus.id)

        } catch (AccessDeniedException ade) {
            log.error(ade)
            render accessDeniedErrorMessage()
        } catch (Exception ee) {
            log.error(ee)
            editErrorMessage()
        }
    }

    def editContext(Long id, String groupBySection) {
        Experiment instance = Experiment.get(id)
        if (!instance) {
            // FIXME:  Should not use flash if we do not redirect afterwards
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'experiment.label', default: 'Experiment'), id])
            return

        }
        AbstractContextOwner.ContextGroup contextGroup = instance.groupBySection(ContextType.byId(groupBySection?.decodeURL()))
        render view: '../project/editContext', model: [instance: instance, contexts: [contextGroup]]
    }

    def save() {
        def assay = Assay.get(params.assayId)
        boolean editable = canEdit(permissionEvaluator, springSecurityService, assay)
        if (!editable) {
            render accessDeniedErrorMessage();
            return
        }
        Experiment experiment = new Experiment()
        experiment.assay = assay
        setEditFormParams(experiment)
        experiment.ownerRole=assay.ownerRole
        experiment.dateCreated = new Date()

        if (!validateExperiment(experiment)) {
            render renderEditFieldsForView("create", experiment, assay);
         } else {
            if (!experiment.save(flush: true)) {
                render renderEditFieldsForView("create", experiment, assay);
            } else {
                experimentService.updateMeasures(experiment.id, JSON.parse(params.experimentTree))
                redirect(action: "show", id: experiment.id)
            }
        }
    }

    def update() {
        def experiment = Experiment.get(params.id)
        try {
             experimentService.updateMeasures(experiment.id, JSON.parse(params.experimentTree))
        } catch (AccessDeniedException ade) {
            log.error("Access denied on update measure", ade)
            render accessDeniedErrorMessage()
            return
        }
        if (!experiment.save(flush: true)) {
            render renderEditFieldsForView("edit", experiment, experiment.assay);
        } else {
            redirect(action: "show", id: experiment.id)
        }
    }

    def reloadResults(Long id) {
        String jobKey = asyncResultsService.createJobKey()
        String link = createLink(action: 'viewLoadStatus', params:[experimentId: id, jobKey: jobKey])
        asyncResultsService.doReloadResultsAsync(id, jobKey, link)
        redirect(action: "viewLoadStatus", params:[jobKey: jobKey, experimentId: id])
    }

    def viewLoadStatus(String jobKey, String experimentId) {
        JobStatus status = asyncResultsService.getStatus(jobKey)
        [experimentId: experimentId, status: status]
    }

    private Map renderEditFieldsForView(String viewName, Experiment experiment, Assay assay) {
        JSON experimentMeasuresAsJsonTree = new JSON(measureTreeService.createMeasureTree(experiment, false))
        JSON assayMeasuresAsJsonTree = new JSON([])
            //new JSON(measureTreeService.createMeasureTreeWithSelections(assay, experiment, true))

        return [view: viewName, model: [experiment: experiment, assay: assay, experimentMeasuresAsJsonTree: experimentMeasuresAsJsonTree, assayMeasuresAsJsonTree: assayMeasuresAsJsonTree]]
    }
    private boolean validateExperiment(Experiment experiment) {
        println "Validating Experiment dates"

        Date today = new Date();
        Calendar cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, 1)
        Date oneYearFromToday = cal.getTime()
        if (experiment.holdUntilDate) {
            // Checks that the Hold Until Today is before today date
            if (experiment.holdUntilDate.before(today)) {
                experiment.errors.reject('experiment.holdUntilDate.incorrectEarlyValue', 'Hold Until Date must be equal or after today')
            }
            // Checks that the Hold Until Today date is not more than 1 year from today
            if (experiment.holdUntilDate.after(oneYearFromToday)) {
                experiment.errors.reject('experiment.holdUntilDate.incorrecMoreThan1YearFromToday', 'Hold Until Date is more than 1 year from today')
            }
        }
        if (experiment.runDateFrom && experiment.runDateTo) {
            //Checks that Run Date To is not before Run From Date
            if (experiment.runDateFrom.after(experiment.runDateTo)) {
                experiment.errors.reject('experiment.holdUntilDate.incorrecRunDateFrom', 'Run Date To cannot be before Run From Date')
            }
        }
        return !experiment.hasErrors()
    }

    private def setEditFormParams(Experiment experiment) {
        experiment.properties["experimentName", "description", "experimentStatus"] = params
        experiment.holdUntilDate = params.holdUntilDate ? new SimpleDateFormat("MM/dd/yyyy").parse(params.holdUntilDate) : null
        experiment.runDateFrom = params.runDateFrom ? new SimpleDateFormat("MM/dd/yyyy").parse(params.runDateFrom) : null
        experiment.runDateTo = params.runDateTo ? new SimpleDateFormat("MM/dd/yyyy").parse(params.runDateTo) : null
    }
}

