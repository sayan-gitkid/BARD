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

<%@ page import="bard.db.people.Role" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="basic"/>
    <title>Team : ${roleInstance?.displayName}</title>
    <r:require
            modules="core,bootstrap,xeditable,editRole"/>
</head>

<body>
<r:script disposition='head'>
    function submitTeamRoleForm(teamRole) {
        var teamRoleForm = document.getElementById("modifyTeamRoles");
        var checkboxes = teamRoleForm.elements["checkboxes"];
        if (checkboxes != null) {
            var checkedBoxes = 0;
            for (var i = 0; i < checkboxes.length; i++) {
                if (checkboxes[i].checked)
                    checkedBoxes++;
            }
            if (checkedBoxes > 0) {
                teamRoleForm.elements["teamRole"].value = teamRole;
                teamRoleForm.submit();
            }
            else {
                alert('No member has been selected. Please select one or more team members to be able to set role')
            }
        }
        else {
            alert('Please add a team member to be able to set role')
        }
    }
</r:script>
<div class="container-fluid">
    <g:hiddenField name="version" id="versionId" value="${roleInstance?.version}"/>
    <div class="row-fluid">
        <div class="span3"></div>

        <div class="span9">
            <dl class="dl-horizontal">
                <dt><g:message code="role.authority.label" default="Name"/>:</dt>
                <dd>
                    <span
                            class="role"
                            data-toggle="manual"
                            id="authorityId"
                            data-inputclass="input-xxlarge"
                            data-type="textarea"
                            data-value="${roleInstance?.authority}"
                            data-pk="${roleInstance?.id}"
                            data-url="${request.contextPath}/role/editAuthority"
                            data-original-title="Edit Name">${roleInstance?.authority}</span>
                    <a href="#" class="icon-pencil documentPencil ${editable}" title="Click to edit name"
                       data-id="authorityId"></a>
                </dd>

                <dt><g:message code="role.displayName.label" default="Display Name"/>:</dt>
                <dd>
                    <span
                            class="role"
                            data-toggle="manual"
                            id="displayNameId"
                            data-inputclass="input-xxlarge"
                            data-type="textarea"
                            data-value="${roleInstance?.displayName}"
                            data-pk="${roleInstance?.id}"
                            data-url="${request.contextPath}/role/editDisplayName"
                            data-original-title="Edit Display Name">${roleInstance?.displayName}</span>
                    <a href="#" class="icon-pencil documentPencil ${editable}" title="Click to edit display name"
                       data-id="displayNameId"></a>
                </dd>
                <dt><g:message code="default.dateCreated.label"/>:</dt>
                <dd><g:formatDate date="${roleInstance?.dateCreated}" format="MM/dd/yyyy"/></dd>
                <dt><g:message code="default.lastUpdated.label"/>:</dt>
                <dd id="lastUpdatedId"><g:formatDate date="${roleInstance?.lastUpdated}" format="MM/dd/yyyy"/></dd>
                <dt><g:message code="default.modifiedBy.label"/>:</dt>
                <dd id="modifiedById"><g:renderModifiedByEnsureNoEmail entity="${roleInstance}"/></dd>
            </dl>
        </div>
    </div>

    <div class="row-fluid">
        <div class="span12">
            <g:if test="${flash.success}">
                <div class="alert alert-success"><button type="button" class="close"
                                                         data-dismiss="alert">&times;</button><strong>${flash.success}</strong>
                </div>
            </g:if>
            <g:if test="${flash.error}">
                <div class="alert alert-error"><button type="button" class="close"
                                                       data-dismiss="alert">&times;</button><strong>${flash.error}</strong>
                </div>
            </g:if>
        </div>
    </div>

    <div class="row-fluid">
        <div class="span12">
            <g:if test="${isTeamManager || isAdmin}">
                <div class="row-fluid">
                    <div class="span3">

                        <div class="btn-group">
                            <button class="btn">Actions</button>
                            <button class="btn dropdown-toggle" data-toggle="dropdown">
                                <span class="caret"></span>
                            </button>
                            <ul class="dropdown-menu">
                                <li class="dropdown-submenu"><a tabindex="-1" href="#">Add to role</a>
                                    <ul class="dropdown-menu">
                                        <li><a onclick="submitTeamRoleForm('Member');">Member</a>
                                        <li><a onclick="submitTeamRoleForm('Manager');">Manager</a>
                                        <li></li>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                    </div>

                    <div class="span9">

                        <g:form id="addUserToTeam" name="addUserToTeam" action="addUserToTeam"
                                controller="role">
                            <div class="input-append">
                                <g:hiddenField class="" id="roleId" name="roleId"
                                               value="${roleInstance?.id}"/>
                                <g:textField name="email" value="" placeholder="Email address"
                                             required="required"/>
                                <input type="submit" class="btn btn-primary" value="Add to team">
                            </div>
                        </g:form>

                    </div>
                </div>

            </g:if>
            <table class="table table-striped table-hover table-bordered">
                <caption><strong>Team Members</strong></caption>
                <thead>
                <tr>
                    <g:if test="${isTeamManager || isAdmin}"><th></th></g:if>
                    <th data-sort="string-ins">Name</th>
                    <th data-sort="string-ins">Email Address</th>
                    <th data-sort="string-ins">Role</th>
                </tr>
                </thead>
                <tbody>
                <g:form id="modifyTeamRoles" name="modifyTeamRoles" action="modifyTeamRoles" controller="role">
                    <g:hiddenField id="roleId" name="roleId" value="${roleInstance?.id}"/>
                    <g:hiddenField id="teamRole" name="teamRole" value=""/>
                    <g:each in="${teamMembers}" var="member">
                        <tr>
                            <g:if test="${isTeamManager || isAdmin}"><td><g:checkBox id="checkboxes" name="checkboxes"
                                                                          value="${member.id}" checked=""/></td></g:if>
                            <td>${member.person.fullName}</td>
                            <td>${member.person.emailAddress}</td>
                            <g:if test="${member.teamRole.id.equals("Member")}">
                                <td><span class="label">${member.teamRole.id}</span></td>
                            </g:if>
                            <g:elseif test="${member.teamRole.id.equals("Manager")}">
                                <td><span class="label label-success">${member.teamRole.id}</span></td>
                            </g:elseif>
                            <g:else>
                                <td><span class="label label-info">${member.teamRole.id}</span></td>
                            </g:else>
                        </tr>
                    </g:each>
                </g:form>
                </tbody>
            </table>
            <br/>
        </div>
    </div>
</div>
</body>
</html>
