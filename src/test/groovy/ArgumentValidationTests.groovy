import uk.ac.qub.eeecs.closer.exceptions.CloserErrorCode
import uk.ac.qub.eeecs.closer.exceptions.CloserException
import uk.ac.qub.eeecs.closer.main.Main
import spock.lang.Unroll

/**
 * @author : Jordan
 * @created : 09/01/2021, Saturday
 * @description : Tests to validate the command line inputs and appropriate errors
 * */
class ArgumentValidationTests extends CloserSpecification{

    @Unroll
    def "Test to show that if the missing parameters are handled and error appropriately"(String[] arguments, List<String> missingArguments){
        when: "Parameters are sent with a combination of the required ones missing"
            Main.mainWithoutExceptionHandling(arguments)
        then: "Appropriate error message is thrown with details as to what has been missed"
            CloserException ex = thrown()
            ex.errorCode == CloserErrorCode.MISSING_COMMAND_lINE_OPTION
            ex.additionalDetails == ["missingOptions" : missingArguments]
        where: "These are the argument combinations"
            arguments                                  | missingArguments
            ["test 1"]                                 | ["i", "t", "u"]
            ["test 2", "-u", "GIT"]                    | ["i", "t"]
            ["test 3", "-t", "GIT"]                    | ["i", "u"]
            ["test 4", "-i", "file.txt"]               | ["t", "u"]
            ["test 5", "-u", "GIT", "-t", "GIT"]       | ["i"]
            ["test 6", "-i", "file.txt", "-u", "GIT"]  | ["t"]
            ["test 7", "-i", "file.txt", "-t", "GIT"]  | ["u"]
    }

    @Unroll
    def "Test to show that if the missing arguments are handled and error appropriately"(String[] arguments, String option, String missingArguments){
        when: "Parameters are sent with a combination of the required parameter missing"
            Main.mainWithoutExceptionHandling(arguments)
        then: "Appropriate error message is thrown with details as to what has been missed"
            CloserException ex = thrown()
            ex.errorCode == CloserErrorCode.MISSING_COMMAND_lINE_OPTION_ARGUMENT
            ex.additionalDetails == ["option" : option, "missingArguments": missingArguments]
        where: "These are the argument combinations"
            arguments                                                        | option | missingArguments
            ["test 1", "-i", "-t", "GIT", "-u", "GIT"]                       | "i"    | "FILE"
            ["test 1", "-i", "file.txt", "-t", "-u", "GIT"]                  | "t"    | "TYPE"
            ["test 3", "-i", "file.txt", "-t", "GIT", "-u"]                  | "u"    | "TYPE"
            ["test 3", "-i", "file.txt", "-t", "GIT", "-u", "GIT", "-o"]     | "o"    | "FILE"
    }

    def "Test to show that if the input file cannot be opened or does not exist the appropriate error is thrown"(){
        when: "Parameters are used for a file that cannot be read"
            String[] arguments = ["test 3", "-i", "file.txt", "-t", "GIT", "-u", "GIT"]
            Main.mainWithoutExceptionHandling(arguments)
        then: "Exception is thrown detailing which file cannot be read"
            CloserException ex = thrown()
            ex.errorCode == CloserErrorCode.CLOSER_CANNOT_READ_FILE
            ex.additionalDetails == ["fileLocation": "file.txt"]
    }
}
