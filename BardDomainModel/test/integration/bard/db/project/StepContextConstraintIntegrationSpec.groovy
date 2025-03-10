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

package bard.db.project

import bard.db.model.AbstractContextConstraintIntegrationSpec
import org.junit.Before
import spock.lang.Unroll

import static test.TestUtils.assertFieldValidationExpectations
import bard.db.project.ProjectStep
import bard.db.project.StepContext

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 11/6/12
 * Time: 5:59 PM
 * To change this template use File | Settings | File Templates.
 */
@Unroll
class StepContextConstraintIntegrationSpec extends AbstractContextConstraintIntegrationSpec {

    @Before
    @Override
    void doSetup() {
        domainInstance = StepContext.buildWithoutSave()
    }

    void "test projectStep constraints #desc projectStep: '#valueUnderTest'"() {
        final String field = 'projectStep'

        when: 'a value is set for the field under test'
        domainInstance[(field)] = valueUnderTest.call()
        domainInstance.validate()

        then: 'verify valid or invalid for expected reason'
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        and: 'verify the domain can be persisted to the db'
        if (valid) {
            domainInstance == domainInstance.save(flush: true)
        }

        where:
        desc             | valueUnderTest        | valid | errorCode
        'null not valid' | {null}                | false | 'nullable'
        'valid step'     | {ProjectStep.build()} | true  | null
    }
}
