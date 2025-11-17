package dev.v22.utils.create;

public class CreatorException extends Exception {
    private Cause cause;

    public CreatorException(Cause cause) {
        super(cause.name());
        this.cause = cause;
    }

    public enum Cause {
        NO_CREATOR_FOUND,
        INVALID_CREATOR,
    }
}
