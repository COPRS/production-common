package esa.s1pdgs.cpoc.report;

public class ReportingMessage {
	private final String message;
	private final Object[] args;
	private final long transferAmount;
	
	public ReportingMessage(final long transferAmount, final String message, final Object... args) {
		this.message = message;
		this.args = args;
		this.transferAmount = transferAmount;
	}
	
	public ReportingMessage(final String message, final Object... args) {
		this(0L,message,args);
	}

	public String getMessage() {
		return message;
	}

	public Object[] getArgs() {
		return args;
	}

	public long getTransferAmount() {
		return transferAmount;
	}
}
