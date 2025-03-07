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

//Path to finding all the stats modifier descriptors

$(document).ready(function () {
    var COMMON_DESCRIPTOR_PATH = bardAppContext + "/ontologyJSon/";
    var STATS_MODIFIER_DESCRIPTORS = COMMON_DESCRIPTOR_PATH + "getStatsModifierDescriptors?startOfFullPath=project management> experiment> result detail> endpoint statistic>";
    var RESPONSE_END_POINT_DESCRIPTORS = COMMON_DESCRIPTOR_PATH + "getAttributeDescriptors?startOfFullPath=result type> response endpoint> percent response>";
    var RESULT_TYPE_DESCRIPTORS = COMMON_DESCRIPTOR_PATH + "getAttributeDescriptors?startOfFullPath=result type";
    var CONCENTRATION_END_POINT_DESCRIPTORS = COMMON_DESCRIPTOR_PATH + "getAttributeDescriptors?startOfFullPath=result type> concentration endpoint>";


    var initialResultTypeValue = null;
    var initialStatsModifierValue = null;


    //We want to make the text boxes active when you are in edit mode
    var handleResultTypeEditing = function (data) {
        initialResultTypeValue = data.text;

        //We need to get the current value if you came to this page to edit a measure.
        //We need to use the value to update the text around the hierarchy drop down
        var initialParentMeasure = $("#parentExperimentMeasureId :selected").text();
        var initialHierarchy = $("#parentChildRelationshipId :selected").text();

        updateLabelOnHierarchySelectBox(initialResultTypeValue, initialStatsModifierValue, initialParentMeasure, initialHierarchy);

    };

    var handleConcentrationResultTypeEditing = function (data) {
        var t = data.text;
    };
    var handleResponseResultTypeEditing = function (data) {
        var t = data.text;
    };
    var handleStatsModifierEditing = function (data) {
        initialStatsModifierValue = data.text;
        var initialParentMeasure = $("#parentExperimentMeasureId :selected").text();
        var initialHierarchy = $("#parentChildRelationshipId :selected").text();

        //We repeat this function call here, so that text around the hierarchy select box gets populated
        //to handle the case where the current measure contains a stats modifier
        updateLabelOnHierarchySelectBox(initialResultTypeValue, initialStatsModifierValue, initialParentMeasure, initialHierarchy);

    };
    /**
     *
     *  Common page handlers
     *
     */

        //handle parent measures
    $('#parentExperimentMeasureId').on('change', function () {
        updateLabelDivForResultType();

    });

    /****
     *
     * Result Type Page Handlers
     */
        //handle hierarchy changes

    $('#parentChildRelationshipId').on('change', function () {
        updateLabelDivForResultType();

    });

    //handle result type for result type page
    $('#resultTypeId').on('change', function () {
        var selectedData = $("#resultTypeId").select2("data");
        $('#resultTypeDescription').val(selectedData.description);
        updateLabelDivForResultType();
    });
    //get result types
    var resultTypeSelect2 = new DescriptorSelect2('#resultTypeId', 'Search for result type', {results: []});
    resultTypeSelect2.initSelect2({results: []});
    $.ajax(RESULT_TYPE_DESCRIPTORS, {
        success: function (data) {
            resultTypeSelect2.initSelect2(data, handleResultTypeEditing);
        },
        error: handleAjaxError()
    });

    $('#resultTypeId').on("select2-highlight", function (e) {
        resultTypeSelect2.updateSelect2DescriptionPopover(e.choice);
    });

    //Handle stats modifiers
    //handle changes on stats modifiers on result type page
    $('#statsModifierId').on('change', function (e) {
        var selectedData = $("#statsModifierId").select2("data");
        $('#statsModifierDescription').val(selectedData.description);
        updateLabelDivForResultType();
    });
    var statsModifierSelect2 = new DescriptorSelect2('#statsModifierId', 'Search for stats modifiers', {results: []});
    statsModifierSelect2.initSelect2({results: []});
    $.ajax(STATS_MODIFIER_DESCRIPTORS, {
        success: function (data) {
            statsModifierSelect2.initSelect2(data, handleStatsModifierEditing);
        },
        error: handleAjaxError()
    });

    $('#statsModifierId').on("select2-highlight", function (e) {
        statsModifierSelect2.updateSelect2DescriptionPopover(e.choice);
    });

    /****
     *
     *
     *  Dose response page handlers
     *
     *
     */

        //handle concentration result types on dose response page
    $('#concentrationResultTypeId').on('change', function (e) {
        var selectedData = $("#concentrationResultTypeId").select2("data");
        $('#concentrationResultTypeDescription').val(selectedData.description);
    });

    var concentrationResultTypeSelect2 = new DescriptorSelect2('#concentrationResultTypeId', 'Search for result type', {results: []});
    concentrationResultTypeSelect2.initSelect2({results: []});
    $.ajax(CONCENTRATION_END_POINT_DESCRIPTORS, {
        success: function (data) {
            concentrationResultTypeSelect2.initSelect2(data, handleConcentrationResultTypeEditing);
        },
        error: handleAjaxError()
    });

    $('#concentrationResultTypeId').on("select2-highlight", function (e) {
        concentrationResultTypeSelect2.updateSelect2DescriptionPopover(e.choice);
    });


    //handle response types on dose response page
    $('#responseResultTypeId').on('change', function (e) {
        var selectedData = $("#responseResultTypeId").select2("data");
        $('#responseResultTypeDescription').val(selectedData.description);
    });
    //get response result types  for dose
    var responseResultTypeSelect2 = new DescriptorSelect2('#responseResultTypeId', 'Search for result type', {results: []});
    responseResultTypeSelect2.initSelect2({results: []});
    $.ajax(RESPONSE_END_POINT_DESCRIPTORS, {
        success: function (data) {
            responseResultTypeSelect2.initSelect2(data, handleResponseResultTypeEditing);
        },
        error: handleAjaxError()
    });

    $('#responseResultTypeId').on("select2-highlight", function (e) {
        responseResultTypeSelect2.updateSelect2DescriptionPopover(e.choice);
    });


    $('.dictionary').tooltip();
});
function updateLabelOnHierarchySelectBox(resultType, statsModifier, parentMeasure, hierarchy) {

    if (parentMeasure == 'none' || hierarchy == 'none') {
        parentMeasure = '';
    }

    var leftDivLabel = resultType;
    if (statsModifier) {
        leftDivLabel = leftDivLabel + "(" + statsModifier + ")";
    } else {
        leftDivLabel = leftDivLabel + " ";
    }
    if (parentMeasure) {
        $('#hierarchyLabelId').html(leftDivLabel);
        $('#selectedParentLabelId').html(parentMeasure);
    } else {
        $('#hierarchyLabelId').html("");
        $('#selectedParentLabelId').html("");
    }

}
function updateLabelDivForResultType() {

    var statsModifierDiv = $("#statsModifierId").select2("data");
    var statsModifier = null;
    if (statsModifierDiv) {
        statsModifier = statsModifierDiv.text;
    }

    var resultTypeDiv = $("#resultTypeId").select2("data");
    var resultType = null;
    if (resultTypeDiv) {
        resultType = resultTypeDiv.text;
    }
    var parentMeasure = $("#parentExperimentMeasureId :selected").text();
    var hierarchy = $("#parentChildRelationshipId :selected").text();

    updateLabelOnHierarchySelectBox(resultType, statsModifier, parentMeasure, hierarchy);


}
