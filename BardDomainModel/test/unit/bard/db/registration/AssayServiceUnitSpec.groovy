package bard.db.registration

import bard.db.enums.AssayStatus
import bard.db.enums.AssayType
import bard.db.enums.ReadyForExtraction
import bard.db.experiment.AssayContextExperimentMeasure
import bard.db.experiment.Experiment
import bard.db.experiment.ExperimentMeasure
import bard.db.people.Role
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.services.ServiceUnitTestMixin
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import registration.AssayService
import spock.lang.Specification

/**
 * Created with IntelliJ IDEA.
 * User: pmontgom
 * Date: 5/2/13
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */
@Build([Assay, Experiment, AssayContextExperimentMeasure, ExperimentMeasure, AssayContext, AssayContextItem, AssayDocument])
@Mock([Assay, Experiment, AssayContextExperimentMeasure, ExperimentMeasure, AssayContext, AssayContextItem, AssayDocument])
@TestMixin(ServiceUnitTestMixin)
@TestFor(AssayService)
public class AssayServiceUnitSpec extends Specification {


    void 'test cloneDocuments'() {
        given:
        Assay assay = Assay.build(assayName: "assayName")
        AssayDocument.build(assay: assay)
        Assay clonedAssay = service.cloneAssayOnly(assay, assay.dateCreated, "me", "Clone ")
        assert !clonedAssay.documents
        when:
        service.cloneDocuments(assay, clonedAssay)
        then:
        assert clonedAssay.documents
    }

    void 'test cloneContexts'() {
        given:
        Assay assay = Assay.build()
        AssayContext context = AssayContext.build(assay: assay, contextName: "alpha")
        AssayContextItem.build(assayContext: context)
        Assay clonedAssay = service.cloneAssayOnly(assay, assay.dateCreated, "me", "Clone ")

        when:
        Map<AssayContext, AssayContext> map = service.cloneContexts(assay, clonedAssay, false)
        then:
        assert map
        assert map.size() == assay.assayContexts.size()
    }

    void "test cloneAssayOnly #desc"() {
        given:
        Assay assay = assayBuild.call()
        when:
        Assay clonedAssay = service.cloneAssayOnly(assay, assay.dateCreated, "me", assayNamePrefix)
        then:
        assert clonedAssay.assayName == expectedAssayName
        assert clonedAssay.assayStatus == expectedAssayStatus
        assert clonedAssay.readyForExtraction == assay.readyForExtraction
        assert clonedAssay.assayVersion == "1"
        assert clonedAssay.designedBy == "me"

        where:
        desc                                        | assayBuild                              | assayNamePrefix | expectedAssayName  | expectedAssayStatus
        "Cloned Assay with blank assay Name prefix" | { Assay.build(assayName: "assayName") } | ""              | "assayName"        | AssayStatus.DRAFT
        "Cloned Assay with an assay Name prefix"    | { Assay.build(assayName: "assayName") } | "Cloned "       | "Cloned assayName" | AssayStatus.DRAFT
    }

    void 'test clone assay for editing'() {
        given:
        Assay assay = Assay.build(assayType: AssayType.TEMPLATE)
        AssayContext context = AssayContext.build(assay: assay, contextName: "alpha")
        AssayContextItem contextItem = AssayContextItem.build(assayContext: context)
        AssayDocument.build(assay: assay)
        SpringSecurityUtils.metaClass.'static'.ifAnyGranted = { String role ->
            return true
        }
        Role.metaClass.'static'.findByAuthority={String authority ->
              new Role()
        }
        when:
        Assay newAssay = service.cloneAssayForEditing(assay, assay.designedBy);

        then:
        // test assay props are good
        assay != newAssay
        assert "Clone of ${assay.assayName}" == newAssay.assayName
        assert AssayStatus.DRAFT == newAssay.assayStatus
        assert assay.assayType != newAssay.assayType
        assert newAssay.assayType == AssayType.REGULAR
        assert assay.designedBy == newAssay.designedBy
        assert newAssay.assayVersion == "1"
        assert ReadyForExtraction.NOT_READY == newAssay.readyForExtraction

        // test assay documents are good
        assert newAssay.assayDocuments.isEmpty()

        // test assay context is good
        newAssay.assayContexts.size() == 1
        AssayContext newContext = newAssay.assayContexts.first()
        newContext != context
        newContext.contextName == context.contextName
        newContext.contextType == context.contextType

        // test assay context items are good
        AssayContextItem newContextItem = newContext.assayContextItems.first()
        newContextItem != contextItem
        newContextItem.attributeType == contextItem.attributeType
    }

    void 'test assay clone'() {
        setup:
        Assay assay = Assay.build()
        AssayContext context = AssayContext.build(assay: assay, contextName: "alpha")
        AssayContextItem contextItem = AssayContextItem.build(assayContext: context)
        AssayDocument document = AssayDocument.build(assay: assay)
        Experiment experiment = Experiment.build(assay: assay)
        ExperimentMeasure experimentMeasure = ExperimentMeasure.build(experiment: experiment)
        AssayContextExperimentMeasure assayContextMeasure = AssayContextExperimentMeasure.build(assayContext: context, experimentMeasure: experimentMeasure)

        when:
        Assay newAssay = service.cloneAssay(assay).assay;

        then:
        //Experiment newExperiment = newAssay.experiments.first()
        // test assay props are good
        assay != newAssay
        assay.assayName == newAssay.assayName
        assay.assayStatus == newAssay.assayStatus
        assay.assayType == newAssay.assayType
        assay.designedBy == newAssay.designedBy
        newAssay.assayVersion == "1"
        assay.readyForExtraction == newAssay.readyForExtraction

        // test assay documents are good
        newAssay.assayDocuments.size() == 1
        AssayDocument newDocument = newAssay.assayDocuments.first()
        newDocument.documentName == document.documentName
        newDocument.documentContent == document.documentContent
        newDocument.documentType == document.documentType

        // test assay context is good
        newAssay.assayContexts.size() == 1
        AssayContext newContext = newAssay.assayContexts.first()
        newContext != context
        newContext.contextName == context.contextName
        newContext.contextType == context.contextType

        // test assay context items are good
        AssayContextItem newContextItem = newContext.assayContextItems.first()
        newContextItem != contextItem
        newContextItem.attributeType == contextItem.attributeType

        // test all measure properties are good
        /*newExperiment.experimentMeasures.size() == 1
        ExperimentMeasure newExperimentMeasure = newExperiment.experimentMeasures.first()
        newExperimentMeasure != experimentMeasure
        experimentMeasure.resultType == newExperimentMeasure.resultType
        experimentMeasure.statsModifier == newExperimentMeasure.statsModifier
        experimentMeasure.parentChildRelationship == newExperimentMeasure.parentChildRelationship

        // test assay context measure props
        newContext.assayContextExperimentMeasures.size() == 1
        AssayContextExperimentMeasure newAssayContextExperimentMeasure = newContext.assayContextExperimentMeasures.first()
        newAssayContextExperimentMeasure != assayContextMeasure
        newAssayContextExperimentMeasure.experimentMeasure == newExperimentMeasure
        newAssayContextExperimentMeasure.assayContext == newContext */
    }
}
