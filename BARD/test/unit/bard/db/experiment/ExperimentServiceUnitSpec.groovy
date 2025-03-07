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

package bard.db.experiment

import bard.db.dictionary.Element
import bard.db.enums.HierarchyType
import bard.db.enums.Status
import bard.db.registration.Assay
import bard.db.registration.AssayContext
import bard.db.registration.AssayContextItem
import bard.db.registration.AssayDocument
import bardqueryapi.TableModel
import bardqueryapi.experiment.ExperimentBuilder
import grails.buildtestdata.mixin.Build
import grails.plugins.springsecurity.SpringSecurityService
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
@Build([Element, Assay, Experiment, AssayContextExperimentMeasure, AssayContext, ExperimentMeasure, AssayContextItem, AssayDocument])
@Mock([Element, Assay, Experiment, ExperimentMeasure, AssayContext, AssayContextExperimentMeasure, AssayContextItem, AssayDocument])
@TestMixin(ServiceUnitTestMixin)
@TestFor(ExperimentService)
public class ExperimentServiceUnitSpec extends Specification {

    void "test update ExperimentMeasure"() {
        given:
        Experiment experiment = Experiment.build()
        ExperimentMeasure experimentMeasure = ExperimentMeasure.build(experiment: experiment, parentChildRelationship: HierarchyType.SUPPORTED_BY)
        def oldValue = experimentMeasure[(field)]
        experimentMeasure[(field)] = valueUnderTest.call()
        when:
        service.updateExperimentMeasure(experimentMeasure.id, experimentMeasure, [])
        then:
        assert experimentMeasure[(field)] != oldValue

        where:
        desc                    | valueUnderTest                    | field
        "change stats modifier" | { Element.build() }               | "statsModifier"
        "change result type"    | { Element.build() }               | "resultType"
        "change hierarchy"      | { HierarchyType.CALCULATED_FROM } | "parentChildRelationship"
        "change priority"       | { true }                          | "priorityElement"

    }

    void "test add AssayContextExperimentMeasures"() {
        given:
        List<Long> assayContextIds = []
        for (int i = 0; i < expectedResult; i++) {
            assayContextIds.add(AssayContext.build().id)
        }
        ExperimentMeasure experimentMeasure = ExperimentMeasure.build()
        when:
        service.addAssayContextExperimentMeasures(assayContextIds, experimentMeasure)
        then:
        assert experimentMeasure.assayContextExperimentMeasures.size() == expectedResult

        where:
        desc                       | expectedResult
        "With a single context id" | 1
        "With 2 context ids"       | 2
        "With no context ids"      | 0

    }

    void "test create New Experiment Measure"() {
        given:
        SpringSecurityService springSecurityService = Mock(SpringSecurityService)
        service.springSecurityService = springSecurityService
        Experiment experiment = Experiment.build()
        Long experimentId = experiment.id
        ExperimentMeasure parent = ExperimentMeasure.build(experiment: experiment)
        Element resultType = Element.build()
        Element statsModifier = Element.build()
        final HierarchyType hierarchyType = HierarchyType.CALCULATED_FROM
        when:
        ExperimentMeasure experimentMeasure = service.createNewExperimentMeasure(experimentId, parent, resultType, statsModifier, [], hierarchyType, false)
        then:
        assert experimentMeasure
        assert experimentMeasure.parent == parent
        assert !experimentMeasure.priorityElement
        assert experimentMeasure.statsModifier == statsModifier
        assert experimentMeasure.experiment == experiment
        assert experimentMeasure.resultType == resultType


    }

    void 'test preview experiment'() {
        given:
        service.experimentBuilder = Mock(ExperimentBuilder)
        service.resultsExportService = Mock(ResultsExportService)
        Experiment experiment = Experiment.build()
        when:
        TableModel createdTableModel = service.previewResults(experiment.id)
        then:
        service.resultsExportService.readResultsForSubstances(_) >> { [new JsonSubstanceResults()] }
        service.experimentBuilder.buildModelForPreview(_, _) >> { new TableModel() }
        assert createdTableModel
    }

    void 'test splitting experiment from assay'() {
        given:
        service.assayService = Mock(AssayService)
        Assay assay = Assay.build()
        SpringSecurityUtils.metaClass.'static'.ifAnyGranted = { String role ->
            return true
        }
        Element resultType = Element.build()
        Element statsModifier = Element.build()

        AssayContext context = AssayContext.build(assay: assay, contextName: "alpha")
        Experiment experiment = Experiment.build(assay: assay)
        ExperimentMeasure experimentMeasure = ExperimentMeasure.build(experiment: experiment, resultType: resultType, statsModifier: statsModifier)
        AssayContextExperimentMeasure.build(assayContext: context, experimentMeasure: experimentMeasure)

        Assay newAssay = new Assay(dateCreated: assay.dateCreated,
                designedBy: assay.designedBy,
                assayStatus: assay.assayStatus,
                readyForExtraction: assay.readyForExtraction,
                ownerRole: assay.ownerRole
        )

        when:
        service.splitExperimentsFromAssay(assay.id, [experiment])

        then:
        service.assayService.cloneAssay(_) >> {
            return [assay: newAssay, measureOldToNew: [:]]
        }
        experiment.assay != assay // the assay is different
        experimentMeasure.resultType == resultType
        experimentMeasure.statsModifier == statsModifier


    }

    void "test update Experiment Status #desc"() {
        given:
        final Experiment experiment = Experiment.build(experimentName: 'experimentName20', experimentStatus: oldStatus)
        final Status newExperimentStatus = newStatus
        Experiment.metaClass.isDirty = { String field -> false }
        when:
        final Experiment updatedExperiment = service.updateExperimentStatus(experiment.id, newExperimentStatus)
        then:
        assert newExperimentStatus == updatedExperiment.experimentStatus
        where:
        desc                                  | oldStatus          | newStatus
        "Change from draft to Approved"       | Status.DRAFT       | Status.APPROVED
        "Change from draft to Provisional"    | Status.DRAFT       | Status.PROVISIONAL
        "Change from Provisional to Approved" | Status.PROVISIONAL | Status.APPROVED

    }

    void "test update Run From Date"() {
        given:
        final Experiment experiment = Experiment.build(experimentName: 'experimentName20')
        final Date runFromDate = new Date()
        when:
        final Experiment updatedExperiment = service.updateRunFromDate(experiment.id, runFromDate)
        then:
        assert runFromDate == updatedExperiment.runDateFrom
    }

    void "test update Run To Date"() {
        given:
        final Experiment experiment = Experiment.build(experimentName: 'experimentName20')
        final Date runToDate = new Date()
        when:
        final Experiment updatedExperiment = service.updateRunToDate(experiment.id, runToDate)
        then:
        assert runToDate == updatedExperiment.runDateTo
    }

    void "test update hold until Date"() {
        given:
        final Experiment experiment = Experiment.build(experimentName: 'experimentName20')
        final Date holdUntilDate = new Date()
        when:
        final Experiment updatedExperiment = service.updateHoldUntilDate(experiment.id, holdUntilDate)
        then:
        assert holdUntilDate == updatedExperiment.holdUntilDate
    }

    void "test update description"() {
        given:
        final Experiment experiment = Experiment.build(experimentName: 'experimentName20', description: "description1")
        final String newDescription = "Description333"
        when:
        final Experiment updatedExperiment = service.updateExperimentDescription(experiment.id, newDescription)
        then:
        assert newDescription == updatedExperiment.description
    }

    void "test update experiment name"() {
        given:
        final Experiment experiment = Experiment.build(experimentName: 'experimentName20', description: "description1111")
        final String newExperimentName = "ExperimentName333"
        when:
        final Experiment updatedExperiment = service.updateExperimentName(experiment.id, newExperimentName)
        then:
        assert newExperimentName == updatedExperiment.experimentName
    }
}
