package com.simplicite.commons.Training;

/**
 * Shared code TrnConfigException
 */
public class TrnConfigException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String additional;

    public TrnConfigException(String msg) {
        super(msg);
        additional = "";
    }

    public TrnConfigException(String msg, String additional) {
        super(msg);
        this.additional = additional;
    }

    @Override
    public String getMessage() {
        String generatedAdditional = !additional.isEmpty() ? " : " + additional : "";
        return "TRN_CONFIG format error : " + super.getMessage() + generatedAdditional;
    }
}
