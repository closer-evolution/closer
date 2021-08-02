package uk.ac.qub.eeecs.closer.main;

import uk.ac.qub.eeecs.closer.exceptions.CloserErrorCode;
import uk.ac.qub.eeecs.closer.exceptions.CloserException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Jordan
 * @created : 16/01/2021, Saturday
 * @description : Class for validation of program arguments separately from the main method
 **/
public class ArgumentValidator {

    public static void validateVCSTypes(VCSType inputType, String sourceTypeString,
                                        VCSType outputType, String targetTypeString){
        if (inputType == null || outputType == null){
            List<String> invalidTypes = new ArrayList<>();
            if (inputType == null){
                invalidTypes.add(sourceTypeString);
            }
            if (outputType == null){
                invalidTypes.add(targetTypeString);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("incorrectParameters", invalidTypes);
            throw new CloserException(CloserErrorCode.CLOSER_TYPE_PARSING_ERROR, map);
        }
    }

    public static void validateOutputMode(boolean append, boolean overwrite) {
        //Cannot be in both append and overwrite modes at the same time
        if (append && overwrite){
            throw new CloserException(CloserErrorCode.CANNOT_OVERWRITE_AND_APPEND_TO_OUTPUT_FILE);
        }
    }
}
