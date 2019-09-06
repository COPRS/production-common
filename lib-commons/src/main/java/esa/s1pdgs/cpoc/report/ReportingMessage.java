package esa.s1pdgs.cpoc.report;

public class ReportingMessage {
	private final String message;
	private final Object[] args;
	private final long transferAmount;
	
	public ReportingMessage(long transferAmount, String message, Object... args) {
		this.message = message;
		this.args = args;
		this.transferAmount = transferAmount;
	}
	
	public ReportingMessage(String message, Object... args) {
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
