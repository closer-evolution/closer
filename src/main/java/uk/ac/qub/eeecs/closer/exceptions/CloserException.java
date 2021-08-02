package uk.ac.qub.eeecs.closer.exceptions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Jordan
 * @created : 04/01/2021, Monday
 * @description : This is the description
 **/
public class CloserException extends RuntimeException {

    private CloserErrorCode errorCode;
    private Map<String, Object> additionalDetails = new HashMap<>();

    public CloserException(CloserErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public CloserException(CloserErrorCode errorCode, Map<String, Object> additionalDetails) {
        this.errorCode = errorCode;
        this.additionalDetails = additionalDetails;
    }

    public CloserErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(CloserErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public Map<String, Object> getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(Map<String, Object> additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    @Override
    public String toString() {
        return "CloserException{" +
                "errorCode=" + errorCode.toString() +
                ", additionalDetails=" + additionalDetails +
                '}';
    }
}
