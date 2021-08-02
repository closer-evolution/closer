import org.junit.Rule
import org.junit.rules.TestName
import spock.lang.Specification

/**
 * @author : Jordan
 * @created : 09/01/2021, Saturday
 * @description : Standard test specification for all Closer tests
 * */
class CloserSpecification extends Specification{

    @Rule TestName name = new TestName()

    def setup(){
        System.out.println("----------------------------------------------\n" +
                "Starting test: "+name.methodName)
    }

    def cleanup(){
        System.out.println("Finished test: "+name.methodName)
    }
}
