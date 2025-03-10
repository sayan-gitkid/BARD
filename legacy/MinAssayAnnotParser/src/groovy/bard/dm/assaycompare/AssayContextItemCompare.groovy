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

package bard.dm.assaycompare

import bard.db.registration.AssayContextItem
import bard.db.registration.AttributeType
import bard.dm.Log
import org.apache.commons.lang3.StringUtils
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: dlahr
 * Date: 9/23/12
 * Time: 6:55 PM
 * To change this template use File | Settings | File Templates.
 */
class AssayContextItemCompare {
    /**
     * precision to use when comparing doubles (fraction of values)
     */
    final double eps = 1e-4

    private final Logger logger

    public AssayContextItemCompare(Logger logger = Log.logger) {
        this.logger = logger
    }

    public ContextItemComparisonResultEnum compareContextItems(AssayContextItem aci1, AssayContextItem aci2) {
        logger.trace("\t\t\t\taci1 aci2 ${aci1.id} ${aci2.id}")

        //first check if they have the same attribute
        if (aci1.attributeElement.equals(aci2.attributeElement)) {
            //test for presence of value in aci1 first and independently of aci2 so that other tests are not performed if
            //one is tried

            if (aci1.attributeType.equals(AttributeType.Free)) { //if one is free then check the other to be type free
                if (aci1.attributeType.equals(aci2.attributeType)) {
                    return ContextItemComparisonResultEnum.ExactMatch
                }
            } else if (aci1.valueElement) { //attempt to compare values if they are present as elements
                if (aci1.valueElement.equals(aci2.valueElement)) {
                    return ContextItemComparisonResultEnum.ExactMatch
                }
            } else if (aci1.extValueId) { //attempt to compare values if they are present as external references
                if (aci1.extValueId.equals(aci2.extValueId)) {
                    return ContextItemComparisonResultEnum.ExactMatch
                }
            } else if (aci1.valueNum) { //attempt to compare values if they are present as number
                if (aci2.valueNum) {
                    //compare qualifiers, if present, associated with the valueNum
                    boolean qualifierMatch
                    if ((!aci1.qualifier) && (!aci2.qualifier)) { //if both qualifier's are null, they match
                        qualifierMatch = true
                    } else if (aci1.qualifier && aci1.qualifier.equals(aci2.qualifier)) {//if aci1 qualifier is not null, then if it equals aci2 qualifier they are equal
                        qualifierMatch = true
                    } else {
                        qualifierMatch = false
                    }

                    if (qualifierMatch && matchByEps(aci1.valueNum, aci2.valueNum)) { // if the qualifiers match and the double's match within EPS precision
                        return ContextItemComparisonResultEnum.EpsMatch
                    }
                }
            } else if (aci1.valueMax && aci1.valueMin) { //attempt to compare values as number range (max & min)
                if (aci1.valueMin && aci2.valueMin) {
                    if(matchByEps(aci1.valueMin, aci2.valueMin) && matchByEps(aci1.valueMax, aci2.valueMax)) {
                        return ContextItemComparisonResultEnum.EpsMatch
                    }
                }
            } else if (aci1.valueMax) { //attempt to compare values as max limit
                if (aci2.valueMax) {
                    if (matchByEps(aci1.valueMax, aci2.valueMax)) {
                        return ContextItemComparisonResultEnum.EpsMatch
                    }
                }
            } else if (aci1.valueMin) { //attempt to compare values as min limit
                if (aci2.valueMin) {
                    if (matchByEps(aci1.valueMin, aci2.valueMin)) {
                        return ContextItemComparisonResultEnum.EpsMatch
                    }
                }
            //always do the valueDisplay comparison last!  everything else takes priority in comparison.  Only compare this if all other options have failed
            } else if (StringUtils.isNotBlank(aci1.valueDisplay) && StringUtils.isNotBlank(aci2.valueDisplay)
                    && aci1.valueDisplay.trim().equalsIgnoreCase(aci2.valueDisplay.trim())) { //attempt to compare values as value display (aka free text)
                return ContextItemComparisonResultEnum.ExactMatch
            }
        }

        return ContextItemComparisonResultEnum.DoesNotMatch
    }


    private boolean matchByEps(double d1, double d2) {
        final double lower = d1*(1.0 - eps)
        final double upper = d1*(1.0 + eps)

        return (lower <= d2 && upper >= d2)
    }
}
