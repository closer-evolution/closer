package uk.ac.qub.eeecs.closer.exceptions;

import uk.ac.qub.eeecs.closer.main.VCSType;

import java.util.Arrays;

/**
 * @author : Jordan
 * @created : 04/01/2021, Monday
 * @description : This is the description
 **/
public enum CloserErrorCode implements Error {

    MISSING_COMMAND_lINE_OPTION(1000, "Required command line options not provided. See additionalDetails for missing options."),
    MISSING_COMMAND_lINE_OPTION_ARGUMENT(1001, "Command line option requires additional arguments. See additionalDetails for missing arguments."),
    CLOSER_TYPE_PARSING_ERROR(1002, "Type argument does not match one of the supported VCS types. Supported types are "+ Arrays.toString(VCSType.values())),
    CLOSER_CANNOT_READ_FILE(1003, "File specified in arguments does not exist or cannot be read. See additionalDetails for location details."),
    CANNOT_WRITE_OUTPUT_FILE(1004, "File specified in arguments for output cannot be written to. See additionalDetails for location details."),
    CANNOT_OVERWRITE_OUTPUT_FILE(1005, "File specified in arguments for output cannot be overwritten. See additionalDetails for location details."),
    CANNOT_OVERWRITE_AND_APPEND_TO_OUTPUT_FILE(106, "Both overwrite and append parameters cannot be sent together. Only one or none is appropriate, if none" +
            " are sent then a new output file will be created at the specified location, erring if the file exists."),
    INTERNAL_CLOSER_EXCEPTION(1007,
            "Internal Error Occurred, Please contact tool owner for assistance."),
    DATE_PARSING_ERROR(1008, "Date format in VCS logging output does not meet the required format.");


    private final int errorCode;
    private final String errorReason;

    CloserErrorCode(int errorCode, String errorReason) {
        this.errorCode = errorCode;
        this.errorReason = errorReason;
    }

    @Override
    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "{errorCode=" + errorCode + ", errorReason='" + errorReason + "'}";
    }
}
