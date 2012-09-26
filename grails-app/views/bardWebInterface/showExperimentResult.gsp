<%--
  Created by IntelliJ IDEA.
  User: gwalzer
  Date: 9/21/12
  Time: 10:55 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page import="org.springframework.context.annotation.Primary; bard.core.ExperimentValues; bard.core.AssayValues" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="logoSearchCartAndFooter"/>
    <title>BARD : Experiment Result : ${params?.id}</title>
</head>

<body>
<div class="row-fluid">
    <table class="table table-condensed">
        <thead>
        <tr>
            <th>SID</th>
            <th>CID</th>
            <th>Structure</th>
            <th>Readout</th>
            <th>Outcome</th>
            <g:if test="${role && (role != ExperimentValues.ExperimentRole.Primary)}">
                <th>Curve</th>
            </g:if>
        </tr>
        </thead>
        <g:each in="${spreadSheetActivities}" var="experimentData">
            <tr>
                <td><a href="http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?sid=${experimentData.sid}">${experimentData.sid}</a></td>
                <td><a href="${createLink(controller: 'bardWebInterface', action: 'showCompound', params: [cid:experimentData.cid])}">${experimentData.cid}</a></td>
                <td style="min-width: 180px;">
                    <img alt="SID: ${experimentData.sid}" title="SID: ${experimentData.sid}"
                         src="${createLink(controller: 'chemAxon', action: 'generateStructureImage', params: [cid: experimentData.cid, width: 180, height: 150])}"/>
                </td>
                <td>
                    <g:each in="${0..(experimentData.hillCurveValue.size() - 1)}" var="i">
                        ${experimentData.hillCurveValue.response[i]} @ ${experimentData.hillCurveValue.conc[i]}
                        <br/>
                    </g:each>
                </td>
                <td>${experimentData.hillCurveValue.id}</td>
                <td>
                    <g:if test="${role && (role != ExperimentValues.ExperimentRole.Primary)}">
                        <img alt="" title=""
                             src="${createLink(controller: 'doseResponseCurve', action: 'doseResponseCurve', params: [sinf: experimentData.hillCurveValue.sInf, s0: experimentData.hillCurveValue.s0, ac50: experimentData.hillCurveValue.slope, hillSlope: experimentData.hillCurveValue.coef, concentrations: experimentData.hillCurveValue.conc, activities: experimentData.hillCurveValue.response])}"/>
                        <br/><br/>

                        <p>AC50 = ${experimentData.hillCurveValue.slope}</p>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </table>

    <div class="pagination">
        <g:paginate total="${total ? total : 0}" params='[id: "${params?.id}"]'/>
    </div>
</div>
</body>
</html>