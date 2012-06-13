package common.tests

import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.custommonkey.xmlunit.DifferenceListener
import org.custommonkey.xmlunit.DifferenceConstants
import org.custommonkey.xmlunit.Difference

/**
 * This class will not be packaged into a war file. We only need it for test purposes
 * See comments on top of XmlTestSamples.groovy
 */
class XmlTestAssertions {
    public static void assertResults(final String expectedResults, final String generatedResults) {
        XMLUnit.setIgnoreWhitespace(true)
        Diff xmlDiff = new Diff(expectedResults, generatedResults)
        assert true == xmlDiff.similar()
    }
    /**
     * If you would like to ignore some attributes
     * For instance a LinkGenerator generates links without the port number in
     * an integration tests, but does so in a functional test
     * You might call this method to ignore the difference in the links generated
     * @param expectedResults
     * @param generatedResults
     */
    public static void assertResultsWithOverrideAttributes(final String expectedResults, final String generatedResults) {
        XMLUnit.setIgnoreWhitespace(true)
        Diff xmlDiff = new Diff(expectedResults, generatedResults)
        xmlDiff.overrideDifferenceListener(new DifferenceListener() {
            public int differenceFound(Difference difference) {
                if (difference.getId()
                        == DifferenceConstants.ATTR_VALUE_ID) {
                    return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
            }

            public void skippedComparison(org.w3c.dom.Node control, org.w3c.dom.Node test) {

            }
        });
        assert true == xmlDiff.similar()
    }
}
