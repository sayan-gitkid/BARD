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
import bard.db.people.Role
import bard.db.project.ExperimentCommand
import bard.db.project.ExperimentController
import bard.db.registration.Assay
import grails.buildtestdata.mixin.Build
import grails.plugins.springsecurity.SpringSecurityService
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import spock.lang.Specification
import spock.lang.Unroll
import util.BardUser

import static java.util.Calendar.YEAR
import static test.TestUtils.assertFieldValidationExpectations

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ExperimentController)
@Build([Assay, Role, Element])
@Unroll
class ExperimentCommandUnitSpec extends Specification {
    SpringSecurityService springSecurityService


    def setup() {
        this.springSecurityService = Mock(SpringSecurityService)

        SpringSecurityUtils.metaClass.'static'.ifAnyGranted = { String role ->
            return true
        }
        Element.metaClass.'static'.findByIdOrLabel ={ Long id,String label->
            return Element.build()

        }
    }


    void "test validate assayId #desc"() {
        given:
        Role role = Role.build(authority: "ROLE_TEAM_A")
        Element element = Element.build()
        ExperimentCommand experimentCommand = new ExperimentCommand(
                experimentName: "Some Name", ownerRole: role.authority, substanceElementValue: element)
        SpringSecurityUtils.metaClass.'static'.SpringSecurityUtils.getPrincipalAuthorities = {
            return [role]
        }
        when:
        experimentCommand[(field)] = valueUnderTest.call()?.id
        experimentCommand.validate()
        then:
        assertFieldValidationExpectations(experimentCommand, field, valid, errorCode)
        where:
        desc                | valueUnderTest    | valid | errorCode  | field
        'assay id is null'  | { null }          | false | 'nullable' | "assayId"
        'assay Id is valid' | { Assay.build() } | true  | null       | "assayId"
    }

    void "test validate ownerRole #desc"() {
        given:
        String role = null
        ExperimentCommand experimentCommand = new ExperimentCommand(experimentName: "Some Name", assayId: Assay.build().id)
        SpringSecurityUtils.metaClass.'static'.SpringSecurityUtils.getPrincipalAuthorities = {
            return [role]
        }
        when:
        experimentCommand[(field)] = role
        experimentCommand.validate()
        then:
        assertFieldValidationExpectations(experimentCommand, field, valid, errorCode)
        where:
        desc                 | valid | errorCode  | field
        'owner Role is null' | false | 'nullable' | "ownerRole"
    }

    void "test validate ownerRole - success - #desc"() {
        given:
        Role role = Role.build(authority: "ROLE_TEAM_A")
        Element element = Element.build()
        ExperimentCommand experimentCommand = new ExperimentCommand(experimentName: "Some Name",
                assayId: Assay.build().id, substanceElementValue: element)
        SpringSecurityUtils.metaClass.'static'.SpringSecurityUtils.getPrincipalAuthorities = {
            return [role]
        }
        when:
        experimentCommand[(field)] = role.authority
        experimentCommand.validate()
        then:
        assertFieldValidationExpectations(experimentCommand, field, valid, errorCode)
        where:
        desc                  | valid | errorCode | field
        'owner Role is valid' | true  | null      | "ownerRole"
    }

    void "test validate experiment name and description #desc"() {
        given:
        Role role = Role.build(authority: "ROLE_TEAM_A")
        Assay assay = Assay.build()
        Element element = Element.build()
        ExperimentCommand experimentCommand =
                new ExperimentCommand(experimentName: "Some Name", ownerRole: role.authority, assayId: assay.id, substanceElementValue: element)
        SpringSecurityUtils.metaClass.'static'.SpringSecurityUtils.getPrincipalAuthorities = {
            return [role]
        }
        when:
        experimentCommand[(field)] = valueUnderTest
        experimentCommand.validate()
        then:
        assertFieldValidationExpectations(experimentCommand, field, valid, errorCode)
        where:
        desc                       | valueUnderTest     | valid | errorCode  | field
        'Experiment Name is null'  | null               | false | 'nullable' | "experimentName"
        'Experiment Name is blank' | '   '              | false | 'blank'    | "experimentName"
        'Experiment Name is valid' | 'Some Name'        | true  | null       | "experimentName"

        'Description is null'      | null               | true  | null       | "description"
        'Description is blank'     | '   '              | false | 'blank'    | "description"
        'Description is valid'     | 'Some Description' | true  | null       | "description"
    }

    void "test validate date fields #desc"() {
        given:
        Role role = Role.build(authority: "ROLE_TEAM_A")
        Assay assay = Assay.build()
        Element element = Element.build()
        ExperimentCommand experimentCommand = new ExperimentCommand(substanceElementValue: element, experimentName: "Some Name", ownerRole: role.authority, assayId: assay.id)
        SpringSecurityUtils.metaClass.'static'.SpringSecurityUtils.getPrincipalAuthorities = {
            return [role]
        }
        when:
        experimentCommand[(field)] = valueUnderTest
        experimentCommand.validate()
        then:
        assertFieldValidationExpectations(experimentCommand, field, valid, errorCode)
        where:
        desc                       | valueUnderTest | valid | errorCode                        | field
        'Run Date From is null'    | null           | true  | null                             | "runDateFrom"
        'Run Date From is blank'   | '   '          | true  | null                             | "runDateFrom"
        'Run Date From is invalid' | "My Date"      | false | 'experimentCommand.date.invalid' | "runDateFrom"
        'Run Date From is valid'   | '04/25/2012'   | true  | null                             | "runDateFrom"

        'Run Date To is null'      | null           | true  | null                             | "runDateTo"
        'Run Date To is blank'     | '   '          | true  | null                             | "runDateTo"
        'Run Date To is invalid'   | "My Date"      | false | 'experimentCommand.date.invalid' | "runDateTo"
        'Run Date To is valid'     | '04/25/2012'   | true  | null                             | "runDateTo"
    }

    void "test copyFromCmdToDomain"() {
        given:
        Date dateToTest = new Date()
        def nextYear = dateToTest[YEAR] + 1
        dateToTest.set(year: nextYear)

        Role role = Role.build(authority: "ROLE_TEAM_A")
        Assay assay = Assay.build()
        String experimentName = "Name"
        String description = "description"
        String runDateFrom = Experiment.dateFormat.format(new Date())
        String runDateTo = Experiment.dateFormat.format(new Date())
        ExperimentCommand experimentCommand =
            new ExperimentCommand(experimentName: experimentName, ownerRole: role.authority,
                    assayId: assay.id, description: description, runDateFrom: runDateFrom, runDateTo: runDateTo)
        experimentCommand.springSecurityService = this.springSecurityService
        Experiment experiment = new Experiment()
        when:
        experimentCommand.copyFromCmdToDomain(experiment)

        then:

        1 * experimentCommand.springSecurityService.getPrincipal() >> { new BardUser(username: "Stuff", authorities: [role]) }

        assert experiment.assay == experimentCommand.assay
        assert experiment.ownerRole.authority == experimentCommand.ownerRole
        assert experiment.description == experimentCommand.description
        assert experiment.experimentName == experimentCommand.experimentName
        assert Experiment.dateFormat.format(experiment.runDateFrom) == experimentCommand.runDateFrom
        assert Experiment.dateFormat.format(experiment.runDateTo) == experimentCommand.runDateTo

    }

}
