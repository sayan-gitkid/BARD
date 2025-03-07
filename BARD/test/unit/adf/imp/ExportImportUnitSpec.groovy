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

package adf.imp

import adf.exp.JsonTransform
import adf.imp.Batch
import adf.imp.DatasetParser
import bard.db.dictionary.Element
import bard.db.dictionary.ElementHierarchy
import bard.db.dictionary.ElementService
import bard.db.experiment.JsonResult
import bard.db.experiment.JsonResultContextItem
import bard.db.experiment.JsonSubstanceResults
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * Created by ddurkin on 3/20/14.
 */
@Build([Element])
@Mock([Element, ElementHierarchy])
@TestMixin(GrailsUnitTestMixin)
class ExportImportUnitSpec extends Specification {

    def findByLabel(List<JsonResult> results, String label) {
        def matches = results.findAll {
            return it.resultType == label
        }

        if(matches.size() > 0) {
            return matches
        } else {
            // if we didn't find anything, try the children
            def r = []
            results.each {
                r.addAll(findByLabel(it.related, label))
            }
            return r
        }
    }

    void linkElements(Element parent, Element child) {
        ElementHierarchy link = new ElementHierarchy()
        parent.addToParentHierarchies(link)
        child.addToChildHierarchies(link)
    }

    void testRoundTripExportImport() {
        setup:
        Element pubchemOutcome = Element.build(label: "PubChem outcome")
        Element pubchemActivityScore = Element.build(label: "PubChem activity score")
        Element ac50 = Element.build(label: "AC50")
        Element pAC50 = Element.build(label: "pAC50")
        Element hillCoefficient = Element.build(label: "Hill coefficient");
        Element hillS0 = Element.build(label: "Hill s0")
        Element hillSinf = Element.build(label: "Hill sinf")
        Element percentActivity = Element.build(label: "percent activity")
        Element maximum = Element.build(label: "maximum")
        Element numberOfPoints = Element.build(label: "number of points")
        Element screeningConc = Element.build(label: "screening concentration (molar)")
        Element resultType = Element.build(label: "result type")
        [pubchemOutcome, pubchemActivityScore, ac50, pAC50, hillCoefficient, hillS0, hillSinf, percentActivity].each {
            linkElements(resultType, it)
        }

        when:
        // export (JSON -> tabular)
        JsonTransform jsonWip = new JsonTransform()
        jsonWip.transform(new File('test/resources/exp-1462-head.json.gz'), "out/dataset_")

        // import (tabular -> JSON)
        DatasetImporter importer = new DatasetImporter()
        DatasetParser imp = new DatasetParser(["out/dataset_1.txt", "out/dataset_2.txt"], { e -> ElementService.isChildOf(e, resultType)})
        Map resultsBySid = [:]
        importer.eachJsonSubstanceResult(imp) { result ->
            resultsBySid[result.sid] = result
        }

        then:
        Element.findByLabel("PubChem outcome") >> pubchemOutcome
        Element.findByLabel("PubChem activity score") >> pubchemActivityScore
        Element.findByLabel("AC50") >> ac50
        Element.findByLabel("pAC50") >> pAC50
        Element.findByLabel("Hill coefficient") >> hillCoefficient
        Element.findByLabel("Hill s0") >> hillS0
        Element.findByLabel("Hill sinf") >> hillSinf
        Element.findByLabel("percent activity") >> percentActivity
        Element.findByLabel("maximum") >> maximum
        Element.findByLabel("number of points") >> numberOfPoints
        Element.findByLabel("screening concentration (molar)") >> screeningConc

        when:
        def ac50results = findByLabel(resultsBySid[842963L].rootElem, "AC50")

        then:
        // make sure that percent activity is a child of AC50
        ac50results.size() == 1
        List<JsonResult> percentActivityResults = findByLabel(ac50results[0].related, "percent activity")
        percentActivityResults.size() == 9
        JsonResult firstPercentActivity = percentActivityResults.first()

        // make sure we have the context item for concentration
        firstPercentActivity.contextItems.size() == 1
        JsonResultContextItem concentration = firstPercentActivity.contextItems.first()
        concentration.attribute == "screening concentration (molar)"
        concentration.valueNum != null
    }
}
