<%--
  Created by IntelliJ IDEA.
  User: dlahr
  Date: 3/30/13
  Time: 2:44 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>TID Issues</title>
</head>
<body>
<h1>TID's identified with duplicate result-types in result_map</h1>
<h2>(causing problems with result loading)</h2>

<table border="1" cellpadding="10" cellspacing="1">
    <tr>
        <g:each in="${headers}" var="header">
            <th>${header}</th>
        </g:each>
    </tr>


    <g:each in="${tidIssueList}" var="row">
        <tr>
            <g:each in="${row}" var="entry">
                <td>${entry}</td>
            </g:each>
        </tr>
    </g:each>
</table>
</body>
</html>