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

import bard.db.dictionary.Element
import bard.db.experiment.AssayContextExperimentMeasure
import bard.db.experiment.Experiment
import bard.db.experiment.ExperimentMeasure
import grails.buildtestdata.mixin.Build
import grails.plugins.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(MeasureTreeService)
@Build([Assay, Element, ExperimentMeasure, Experiment,AssayContext, AssayContextExperimentMeasure])
@Mock([Assay, Element, ExperimentMeasure,Experiment, AssayContext, AssayContextExperimentMeasure])
@TestMixin(GrailsUnitTestMixin)
@Unroll
class MeasureTreeServiceSpec extends Specification {

    void 'test rendering json measurements'() {
        given: 'a measurement with a child'
        SpringSecurityService securityService = Mock(SpringSecurityService)

        ExperimentMeasure child = ExperimentMeasure.build(resultType: Element.build(label: "child"))
        ExperimentMeasure parent = ExperimentMeasure.build(resultType: Element.build(label: "parent"), childMeasures: [child] as Set)
        child.parent = parent
        Assay assay = Assay.build()
        Experiment experiment = Experiment.build(assay: assay, experimentMeasures: [parent, child] as Set)
        when: 'we render this as json'
        List result = service.createMeasureTree(experiment, "username")

        then:
        result == [[key:2, title:"parent", children:[[key:1, title:"child", children:[], expand:true, relationship:null,measureId:1,addClass:"", username:"username"]],
                expand:true, relationship:null, measureId:2, addClass:"", username:"username"]]
    }

    private static String sortedRootMeasuresAB = '''[
                                                    [key:2, title:a, children:[], expand:true,relationship:null,measureId:2,addClass:, username:],
                                                    [key:1, title:b, children:[], expand:true,relationship:null,measureId:1,addClass:, username:]
                                                   ]'''
    private static String sortedRootMeasuresABC = '''[
                                                       [key:3, title:a, children:[], expand:true,relationship:null,measureId:3,addClass:, username:],
                                                       [key:2, title:B, children:[], expand:true,relationship:null,measureId:2,addClass:, username:],
                                                       [key:1, title:c, children:[], expand:true,relationship:null,measureId:1,addClass:, username:]
                                                      ]'''


    void 'test sorting of root measures in json measurements #measureLabels'() {
        given: 'a measurement with a child'
        Assay assay = Assay.build()
        Experiment experiment = Experiment.build(assay:assay)
        for (String label in measureLabels) {
            experiment.addToExperimentMeasures(ExperimentMeasure.build(resultType: Element.build(label: label)))
        }

        when: 'we render this as json'
        def result = service.createMeasureTree(experiment, null)

        then:

        result.toString().replaceAll(/\s/, '') == expectedJson.replaceAll(/\s/, '')

        where:
        measureLabels   | expectedJson
        ['b', 'a']      | sortedRootMeasuresAB
        ['c', 'B', 'a'] | sortedRootMeasuresABC
    }

    private static String sortedChildMeasuresAB = '''[
                                                        [key:1,
                                                        title:parent,
                                                        children:[
                                                                    [key:3,title:a,children:[], expand:true,relationship:null,measureId:3,addClass:, username:],
                                                                    [key:2,title:b,children:[], expand:true,relationship:null, measureId:2,addClass:, username:]
                                                                   ],
                                                                   expand:true,relationship:null, measureId:1,addClass:, username:
                                                         ]
                                                      ]'''
    private static String sortedChildMeasuresABC = '''[
                                                        [key:1,
                                                        title:parent,
                                                        children:[
                                                                    [key:4,title:a,children:[], expand:true,relationship:null,measureId:4, addClass:, username:],
                                                                    [key:3,title:B,children:[], expand:true,relationship:null,measureId:3, addClass:, username:],
                                                                    [key:2,title:c,children:[], expand:true,relationship:null,measureId:2, addClass:, username:]
                                                                   ], expand:true,relationship:null,measureId:1, addClass:, username:
                                                         ]
                                                      ]'''

    void 'test sorting of child measures in json measurements #measureLabels'() {
        given:
        ExperimentMeasure parent = ExperimentMeasure.build(resultType: Element.build(label: "parent"))
        Assay assay = Assay.build()
        Experiment experiment = Experiment.build(assay: assay)
        for (String label in measureLabels) {
            parent.addToChildMeasures(ExperimentMeasure.build(resultType: Element.build(label: label)))
        }
        experiment.addToExperimentMeasures(parent)


        when: 'we render this as json'
        def result = service.createMeasureTree(experiment, null)

        then:

        result.toString().replaceAll(/\s/, '') == expectedJson.replaceAll(/\s/, '')

        where:
        measureLabels   | expectedJson
        ['b', 'a']      | sortedChildMeasuresAB
        ['c', 'B', 'a'] | sortedChildMeasuresABC
    }
}
