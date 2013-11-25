package molspreadsheet

import bard.core.rest.spring.util.StructureSearchParams
import bardqueryapi.BardUtilitiesService
import bardqueryapi.IQueryService
import bardqueryapi.InetAddressUtil
import de.andreasschmitt.export.ExportService
import org.codehaus.groovy.grails.commons.GrailsApplication
import querycart.QueryCartService

import javax.servlet.http.HttpServletResponse

@Mixin(InetAddressUtil)
class MolSpreadSheetController {
    MolecularSpreadSheetService molecularSpreadSheetService
    ExportService exportService
    QueryCartService queryCartService
    GrailsApplication grailsApplication  //inject GrailsApplication
    RetainSpreadsheetService retainSpreadsheetService
    BardUtilitiesService bardUtilitiesService
    IQueryService queryService


    def index() {
        render(view: 'molecularSpreadSheet', model: [transpose: params.transpose, norefresh: params.norefresh, ChangeNorm: params.ChangeNorm, cid: params.cid, pid: params.pid, showActive: params.showActive] )
    }

    def showExperimentDetails(Long pid, Long cid, Boolean transpose, Boolean showActive) {
        render(view: 'molecularSpreadSheet', model: [cid: cid, pid: pid, transpose: transpose, showActive:showActive])
    }

    def molecularSpreadSheet() {
        MolSpreadSheetData molSpreadSheetData
        Boolean transpose = (params.transpose=="true")
        Boolean noRefreshNeeded = (params.norefresh=="true")
        Boolean showActiveCompoundsOnly = (params.showActive!="false")
        Boolean disableInactiveCheckbox = true
        String assayNormalizationSwap = params.ChangeNorm ?: "0"
        try {
            List<Long> cids = []
            List<Long> pids = []
            List<Long> adids = []
            if (params.cid || params.pid || params.adid) {
                cids = params.cid ? params.list('cid').collect {String it -> it.toLong()} : []
                pids = params.pid ? [params.pid.toLong()] : []
                adids = params.adid ? [params.adid.toLong()] : []
            }
            else if (queryCartService.weHaveEnoughDataToMakeASpreadsheet()) {
                cids = queryCartService.retrieveCartCompoundIdsFromShoppingCart()
                pids = queryCartService.retrieveCartProjectIdsFromShoppingCart()
                adids = queryCartService.retrieveCartAssayIdsFromShoppingCart()
            }
            if (noRefreshNeeded && (retainSpreadsheetService.molSpreadSheetData!=null)){
                molSpreadSheetData = retainSpreadsheetService.molSpreadSheetData
            } else {
                molSpreadSheetData = molecularSpreadSheetService.retrieveExperimentalDataFromIds(cids, adids, pids,showActiveCompoundsOnly)
            }
            retainSpreadsheetService.molSpreadSheetData = molSpreadSheetData
            if (molSpreadSheetData) {
                if (params?.format && params.format != "html") {
                    response.contentType = grailsApplication.config.grails.mime.types[params.format]
                    response.setHeader("Content-disposition", "attachment; filename=molecularSpreadSheet.${params.extension}")
                    LinkedHashMap<String, Object> map = molecularSpreadSheetService.prepareForExport(molSpreadSheetData)
                    exportService.export(params.format, response.outputStream, map["data"], map["fields"], map["labels"], [:], [:])
                    return
                }
                if (molSpreadSheetData.molSpreadsheetDerivedMethod == MolSpreadsheetDerivedMethod.Compounds_NoAssays_NoProjects) {
                    disableInactiveCheckbox = false
                }
                if (transpose) {
                    render(template: 'tSpreadSheet', model: [molSpreadSheetData: molSpreadSheetData,disableInactiveCheckbox:disableInactiveCheckbox,showActive:  showActiveCompoundsOnly])
                } else {
                    render(template: 'spreadSheet', model: [molSpreadSheetData: molSpreadSheetData,assayNormalizationSwap:assayNormalizationSwap,disableInactiveCheckbox:disableInactiveCheckbox,showActive:  showActiveCompoundsOnly])
                }
            } else {
                render(template: 'spreadSheet', model: [molSpreadSheetData: new MolSpreadSheetData(),assayNormalizationSwap:assayNormalizationSwap,disableInactiveCheckbox:disableInactiveCheckbox,showActive:  showActiveCompoundsOnly])
            }
        } catch (Exception ee) {
            String errorMessage = "Could not generate SpreadSheet for current Query Cart Contents"
            flash.message = errorMessage
            log.error(errorMessage + getUserIpAddress(bardUtilitiesService.username), ee)
            return response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "${flash.message}")
        }

    }

    def probeSarTable(Long pid, Long cid, Boolean transpose, Double threshold) {
        if (!cid) {
            String errorMessage = "A non-empty CID parameter is required to generate this spreadsheet"
            flash.message = errorMessage
            return response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "${flash.message}")
        }
        if (threshold > 1) {
            threshold = threshold/100
        }
        Map resultMap = queryService.structureSearch(cid.intValue(), StructureSearchParams.Type.Similarity, threshold, [], 40, 0, -1)
        List<Long> cidList = resultMap.compoundAdapters*.getId()
        // put the probe CID first in the spreadsheet
        cidList.remove(cid)
        cidList.add(0,cid)
        render(view: 'molecularSpreadSheet', model: [cid: cidList, pid: pid, transpose: transpose])
    }

    def list = {
        // eventually we will perform a sort here and then return something useful, but for now we redirect
        String server = request.requestURL - request.requestURI
        String contextPath = request.contextPath
        String base = "${server}${contextPath}" //e.g., 'http://localhost:8080/bardwebclient'
        redirect(base: base, action: 'index')
    }
}