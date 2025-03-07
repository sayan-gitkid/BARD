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

package bard.core.rest.spring

import bard.core.rest.spring.compounds.TargetClassInfo
import bard.core.rest.spring.util.Target
import grails.plugin.cache.Cacheable

class SunburstCacheService extends AbstractRestService {
    def transactional = false
    TargetRestService targetRestService

    final Map<String, List<TargetClassInfo>> targets = [:]

    //@CachePut(value = 'target', key = '#targetClassInfo.id')
    void save(TargetClassInfo targetClassInfo) {
         List<TargetClassInfo> targetClassInfos = targets.get(targetClassInfo.accessionNumber)
        if(targetClassInfos == null){
            targetClassInfos = new ArrayList<TargetClassInfo>()
        }
        if(!targetClassInfos.contains(targetClassInfo)){
            targetClassInfos.add(targetClassInfo)
            targets.put(targetClassInfo.accessionNumber, targetClassInfos)
        }
     }

    @Cacheable(value = 'target')
    List<TargetClassInfo> getTargetClassInfo(String accessionNumber) {
        List<TargetClassInfo> targetClassInfo = this.targets.get(accessionNumber)
        if (!targetClassInfo) {
            log.info("Not found ${accessionNumber} going to REST-API")
            Target target = null
            try{
                target = this.targetRestService.getTargetByAccessionNumber(accessionNumber)
            }catch (Exception) {
                log.info("No information available for ${accessionNumber}")
                return null
            }
            targetClassInfo = Target.constructTargetInformation(target)
             this.targets.put(accessionNumber,targetClassInfo)
        }
        return targetClassInfo
    }

    @Override
    String getResource() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getSearchResource() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getResourceContext() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }
}
