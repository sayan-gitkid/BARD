package bardqueryapi

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import grails.test.mixin.TestFor

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(BardWebInterfaceController)
class CartProjectSpec  extends Specification {

    void setup() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void "test shopping cart project element"() {
        when:
        CartProject cartProject = new CartProject(projectName: "my project name")
        assertNotNull(cartProject)

        then:
        assert cartProject.projectName=='my project name'
        assertNull cartProject.shoppingItem
    }
}
