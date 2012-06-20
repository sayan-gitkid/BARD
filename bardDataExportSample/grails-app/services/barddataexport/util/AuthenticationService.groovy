package barddataexport.util

import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.validation.routines.InetAddressValidator
import org.codehaus.groovy.grails.plugins.codecs.MD5BytesCodec
import java.security.MessageDigest

class AuthenticationService {

    def grailsApplication

    Boolean authenticate(HttpServletRequest request) {
        InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance()

        String apiKey = grailsApplication.config.barddataexport.externalapplication.apiKey.hashed
        String requestApiKey = request.getHeader('APIKEY')
        if (apiKey != requestApiKey)
            return false

        List<String> ipAddressWhiteList = grailsApplication.config.barddataexport.externalapplication.ipAddress.whiteList as List<String>
        String remoteIpAddress = request.getRemoteAddr()
        assert inetAddressValidator.isValid(remoteIpAddress)

        for (String ipAddress in ipAddressWhiteList) {
//            assert inetAddressValidator.isValid(ipAddress)
            assert ipAddress.split(/\./).size() == 4

            Boolean match = doIpAddressesMatch(remoteIpAddress, ipAddress)
            if (match) {
                //TODO
                //log the request into a file.
                return true
            }
        }

        return false
    }

    Boolean doIpAddressesMatch(String rhs, String lhs) {
        String[] rhsStringArray = rhs.split(/\./)
        String[] lhsStringArray = lhs.split(/\./)

        for (int i in 0..3) {
            if (rhsStringArray[i] == '*' || lhsStringArray[i] == '*')
                continue

            Integer lhsIntVal = new Integer(lhsStringArray[i])
            Integer rhsIntVal = new Integer(rhsStringArray[i])
            if (rhsIntVal != lhsIntVal)
                return false
        }

        return true
    }
}
