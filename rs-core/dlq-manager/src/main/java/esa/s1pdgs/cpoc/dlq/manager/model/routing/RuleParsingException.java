package esa.s1pdgs.cpoc.dlq.manager.model.routing;

public class RuleParsingException extends RuntimeException {
	public RuleParsingException() {
        super();
    }

    public RuleParsingException(String s) {
        super(s);
    }

    public RuleParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuleParsingException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = -1258465483141397281L;
}
