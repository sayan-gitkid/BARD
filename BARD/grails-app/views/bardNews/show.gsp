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

<%@ page import="bard.db.util.BardNews" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'bardNews.label', default: 'BardNews')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-bardNews" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                               default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>

<div id="show-bardNews" class="content scaffold-show" role="main">
    <h1><g:message code="default.show.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <ol class="property-list bardNews">

        <g:if test="${bardNewsInstance?.entryId}">
            <li class="fieldcontain">
                <span id="entryId-label" class="property-label"><g:message code="bardNews.entryId.label"
                                                                           default="Entry Id"/></span>

                <span class="property-value" aria-labelledby="entryId-label"><g:fieldValue bean="${bardNewsInstance}"
                                                                                           field="entryId"/></span>

            </li>
        </g:if>

        <g:if test="${bardNewsInstance?.entryDateUpdated}">
            <li class="fieldcontain">
                <span id="entryDateUpdated-label" class="property-label"><g:message
                        code="bardNews.entryDateUpdated.label" default="Entry Date Updated"/></span>

                <span class="property-value" aria-labelledby="entryDateUpdated-label"><g:formatDate
                        date="${bardNewsInstance?.entryDateUpdated}"/></span>

            </li>
        </g:if>

        <g:if test="${bardNewsInstance?.title}">
            <li class="fieldcontain">
                <span id="title-label" class="property-label"><g:message code="bardNews.title.label"
                                                                         default="Title"/></span>

                <span class="property-value" aria-labelledby="title-label"><g:fieldValue bean="${bardNewsInstance}"
                                                                                         field="title"/></span>

            </li>
        </g:if>

        <g:if test="${bardNewsInstance?.link}">
            <li class="fieldcontain">
                <span id="link-label" class="property-label"><g:message code="bardNews.link.label"
                                                                        default="Link"/></span>

                <span class="property-value" aria-labelledby="link-label"><g:fieldValue bean="${bardNewsInstance}"
                                                                                        field="link"/></span>

            </li>
        </g:if>

        <g:if test="${bardNewsInstance?.authorName}">
            <li class="fieldcontain">
                <span id="authorName-label" class="property-label"><g:message code="bardNews.authorName.label"
                                                                              default="Author Name"/></span>

                <span class="property-value" aria-labelledby="authorName-label"><g:fieldValue bean="${bardNewsInstance}"
                                                                                              field="authorName"/></span>

            </li>
        </g:if>

        <g:if test="${bardNewsInstance?.authorEmail}">
            <li class="fieldcontain">
                <span id="authorEmail-label" class="property-label"><g:message code="bardNews.authorEmail.label"
                                                                               default="Author Email"/></span>

                <span class="property-value" aria-labelledby="authorEmail-label"><g:fieldValue
                        bean="${bardNewsInstance}" field="authorEmail"/></span>

            </li>
        </g:if>

        <g:if test="${bardNewsInstance?.authorUri}">
            <li class="fieldcontain">
                <span id="authorUri-label" class="property-label"><g:message code="bardNews.authorUri.label"
                                                                             default="Author Uri"/></span>

                <span class="property-value" aria-labelledby="authorUri-label"><g:fieldValue bean="${bardNewsInstance}"
                                                                                             field="authorUri"/></span>

            </li>
        </g:if>

        <g:if test="${bardNewsInstance?.lastUpdated}">
            <li class="fieldcontain">
                <span id="lastUpdated-label" class="property-label"><g:message code="bardNews.lastUpdated.label"
                                                                               default="Last Updated"/></span>

                <span class="property-value" aria-labelledby="lastUpdated-label"><g:formatDate
                        date="${bardNewsInstance?.lastUpdated}"/></span>

            </li>
        </g:if>

        <g:if test="${bardNewsInstance?.modifiedBy}">
            <li class="fieldcontain">
                <span id="modifiedBy-label" class="property-label"><g:message code="bardNews.modifiedBy.label"
                                                                              default="Modified By"/></span>

                <span class="property-value" aria-labelledby="modifiedBy-label"><g:renderModifiedByEnsureNoEmail entity="${bardNewsInstance}" /></span>

            </li>
        </g:if>

        <g:if test="${bardNewsInstance?.dateCreated}">
            <li class="fieldcontain">
                <span id="dateCreated-label" class="property-label"><g:message code="bardNews.dateCreated.label"
                                                                               default="Date Created"/></span>

                <span class="property-value" aria-labelledby="dateCreated-label"><g:formatDate
                        date="${bardNewsInstance?.dateCreated}"/></span>

            </li>
        </g:if>

        <g:if test="${bardNewsInstance?.datePublished}">
            <li class="fieldcontain">
                <span id="datePublished-label" class="property-label"><g:message code="bardNews.datePublished.label"
                                                                                 default="Date Published"/></span>

                <span class="property-value" aria-labelledby="datePublished-label"><g:formatDate
                        date="${bardNewsInstance?.datePublished}"/></span>

            </li>
        </g:if>

    </ol>
    <g:form>
        <fieldset class="buttons">
            <g:hiddenField name="id" value="${bardNewsInstance?.id}"/>
            <g:link class="edit" action="edit" id="${bardNewsInstance?.id}"><g:message code="default.button.edit.label"
                                                                                       default="Edit"/></g:link>
            <g:actionSubmit class="delete" action="delete"
                            value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                            onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
        </fieldset>
    </g:form>
</div>
</body>
</html>
