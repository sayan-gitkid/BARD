<%
/**
 * second wizard page / tab
 *
 * @author	Jeroen Wesbeek <work@osx.eu>
 * @package AjaxFlow
 */
%>
<af:page>

<g:set var="attributeLabel" value="${ attribute?.attributeLabel }" />
<g:render template="common/itemWizardSelectionsTable" model="['attribute': attributeLabel, 'valueType': 'Not define yet', 'value': 'Not define yet']"/>

<g:hasErrors bean="${valueType}">
	<div class="alert alert-error">
		<button type="button" class="close" data-dismiss="alert">×</button>
		<g:renderErrors bean="${valueType}"/>
	</div>
</g:hasErrors>

<h1>Value type defines the restriction that is placed on the values associated with the chosen attribute:</h1>


<label class="radio">
  <input type="radio" name="valueTypeOption" value="Fixed" checked>
  <strong>Fixed</strong> - Every experiment always has the same value for the attribute "cell line equals HeLa".  These attributes define the unique quality of the assays.
</label>
<label class="radio">
  <input type="radio" name="valueTypeOption" value="List">
  <strong>List</strong> - Every experiment has one of the entries in the list for the attribute "cell line one of HeLa, CHO, MM" or ".001, .003 , .01, .03 uM, .1, ..." uM
</label>
<label class="radio">
  <input type="radio" name="valueTypeOption" value="Range">
  <strong>Range</strong> - Every experiment has a value within the provided range for the attribute "cell density between 10 and 100 cells / well"
</label>
<label class="radio">
  <input type="radio" name="valueTypeOption" value="Free">
  <strong>Free</strong> - Every experiment must provide a value for the attribute, but there is no restriction on that value. Generally used for numbers, but sometimes for text (e.g. cell names) "cell density specified by experiment"
</label>

<input type="hidden" id="pageNumber" name="pageNumber" value="${ page }"/>

%{-- 
<p>
	You can also quickly jump to page 4 if you want...
	<af:ajaxButton name="toPageFour" value="to page 4!" afterSuccess="onPage();" class="prevnext" />
</p>
 --}%
</af:page>
