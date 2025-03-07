%{-- Copyright (c) 2014, The Broad Institute
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
 --}%

<%@ page import="bard.db.enums.ContextType; bard.db.model.AbstractContextOwner; bard.db.enums.Status; bard.db.registration.DocumentKind; bard.db.project.*" %>

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span3 bs-docs-sidebar heading-numbering">
            <ul class="nav nav-list bs-docs-sidenav twitterBootstrapAffixNavBar">
                <li><a href="#summary-header"><i class="icon-chevron-right"></i>Overview</a></li>
                <li><a href="#annotations-header"><i class="icon-chevron-right"></i>Annotations</a></li>
                <li><a href="#experiment-and-step-header"><i class="icon-chevron-right"></i>Experiments and steps</a>
                </li>
                <li><a href="#documents-header"><i class="icon-chevron-right"></i>Documents</a>
                    <ul class="nav nav-list bs-docs-sidenav" style="padding-left: 0; margin: 0;">
                        <li><a href="#documents-description-header"><i class="icon-chevron-right"></i>Descriptions</a>
                        </li>
                        <li><a href="#documents-protocol-header"><i class="icon-chevron-right"></i>Protocols</a></li>
                        <li><a href="#documents-comment-header"><i class="icon-chevron-right"></i>Comments</a></li>
                        <li><a href="#documents-publication-header"><i class="icon-chevron-right"></i>Publications</a>
                        </li>
                        <li><a href="#documents-urls-header"><i class="icon-chevron-right"></i>External URLS</a></li>
                        <li><a href="#documents-other-header"><i class="icon-chevron-right"></i>Others</a></li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="span9">
            <h2>Project: ${instance.name}
                <g:if test="${projectAdapter?.hasProbes()}">
                    <span class="badge badge-info">Probe</span>
                </g:if>
                <small>(Project ID: ${instance.id})</small>
            </h2>
            <br/>
            <g:render template="../layouts/templates/askAQuestion" model="['entity': 'Project']"/>
            <g:if test="${projectAdapter != null}">
                <g:saveToCartButton id="${instance.id}"
                                    name="${bardqueryapi.JavaScriptUtility.cleanup(projectAdapter?.name)}"
                                    type="${querycart.QueryItemType.Project}"/>
            </g:if>

            <section id="summary-header">
                <div class="page-header">
                    <h3 class="sect">Overview</h3>
                </div>

                <div class="row-fluid">
                    <g:render template='editSummary'
                              model="['project': instance, canedit: editable, projectOwner: projectOwner]"/>
                </div>

                <div class="row-fluid">
                    <g:render template="projectReferences" model="[project: instance, editable: editable]"/>
                </div>

            </section>

            <g:if test="${editable == 'canedit'}">
                <g:link controller="externalReference" action="create"
                        params="[ownerClass: instance.class.simpleName, ownerId: instance.id]"
                        class="btn">Add an External Reference</g:link>
            </g:if>

            <g:if test="${projectAdapter?.hasProbes()}">
                <section id="probe-info">
                    <h3>Probes</h3>
                    <ul class="thumbnails">
                        <g:each var="probe" in="${projectAdapter?.probes}" status="i">
                            <li class="span4">
                                <div class="thumbnail">
                                    <g:compoundOptions cid="${probe.cid}" sid="${probe.cid}" smiles="${probe?.smiles}"
                                                       name="${probe.probeId}"
                                                       imageHeight="200" imageWidth="300"/>
                                    <div class="caption">
                                        <h3>Probe ML#: ${probe.probeId}</h3>
                                        <ul>
                                            <li><a href="${probe.url}">Download probe report from Molecular Library BookShelf</a>
                                            </li>
                                            <li><g:link controller="bardWebInterface" action="showCompound"
                                                        params="[cid: probe.cid]">Show Compound Details in BARD</g:link></li>
                                            <li><a href="http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=${probe.cid}"
                                                   target="_blank">View CID ${probe.cid} in PubChem</a></li>
                                            <li><g:link controller="molSpreadSheet" action="showExperimentDetails"
                                                        params="[cid: probe.cid, pid: projectAdapter.id, transpose: true]"
                                                        data-placement="top"
                                                        class="projectTooltip"
                                                        rel="tooltip"
                                                        data-original-title="">Show Experimental Details</g:link></li>
                                            <li><g:form class="form-inline" controller="molSpreadSheet"
                                                        action="probeSarTable"
                                                        params="[cid: probe.cid, pid: projectAdapter.id, transpose: false]">
                                                <g:submitButton name="Show results" class="btn btn-link"
                                                                value="Show results"/>
                                                for similar compounds tested in this project using
                                                <div class="input-append">
                                                    <g:field class="input-mini" name="threshold" value="90"
                                                             type="number" min="0" max="100" step="1" size="4"
                                                             maxlength="3" required="required"/>
                                                    <span class="add-on">%</span>
                                                </div> tanimoto
                                            </g:form></li>
                                        </ul>
                                    </div>
                                </div>
                            </li>
                        </g:each>
                    </ul>
                </section>
            </g:if>

            <br/>

            <section id="annotations-header">
                <h3 class="sect">Annotations
                    <g:link target="dictionary" controller="element" action="showTopLevelHierarchyHelp"><i
                            class="icon-question-sign"></i></g:link>
                </h3>
                <g:render template="/common/guidance" model="[guidanceList: instance.guidance]"/>
                <div class="row-fluid">
                    <div id="cardHolderAssayComponents" class="span12">
                        <g:render template="/context/currentCard"
                                  model="[contextOwner: instance, currentCard: instance.groupUnclassified(),
                                          subTemplate : contextItemSubTemplate, renderEmptyGroups: false]"/>
                    </div>
                </div>

            </section>

            <g:if test="${projectAdapter?.biology}">
                <section id="biology-info">
                    <h3>Biology
                        <g:link target="dictionary" controller="element" action="showTopLevelHierarchyHelp"><i
                                class="icon-question-sign"></i></g:link>
                    </h3>

                    <g:render template="../bardWebInterface/biology" model="['biology': projectAdapter.biology]"/>
                </section>
            </g:if>

            <section id="experiment-and-step-header">
                <h3 class="sect">Experiments and steps</h3>

                <div class="row-fluid">
                    <g:render template='/project/editstep'
                              model="['instanceId': instance.id, canedit: editable]"/>
                    <g:render template="showstep"
                              model="['experiments': instance.projectExperiments,
                                      'pegraph'    : pexperiment, 'instanceId': instance.id, canedit: editable]"/>
                </div>
            </section>

            <br/>

            <g:if test="${experiments}">
                <section id="experiments-info">
                    <h3>Experiments</h3>

                    <g:displayExperimentsGroupedByAssay assays="${assays}" experiments="${experiments}"
                                                        experimentTypes="${projectAdapter.experimentTypes}"/>
                </section>
            </g:if>

            <g:render template="/document/documents"
                      model="[documentKind: DocumentKind.ProjectDocument, owningEntity: instance, canedit: editable]"/>
        </div>
    </div>
</div>
