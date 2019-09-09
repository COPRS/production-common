package esa.s1pdgs.cpoc.report;

public class ReportingMessage {
	private final String message;
	private final Object[] args;
	private final long transferAmount;
	private final ReportingInput input;
	private final ReportingOutput output;
	
	ReportingMessage(ReportingInput input, ReportingOutput output, long transferAmount, String message, Object... args) {
		this.message = message;
		this.args = args;
		this.transferAmount = transferAmount;
		this.input = input;
		this.output = output;
	}
	
	public ReportingMessage(ReportingInput input, String message, Object... args) {
		this(input, ReportingOutput.NULL, 0L, message, args);
	}
	
	public ReportingMessage(ReportingOutput output, long transferAmount, String message, Object... args) {
		this(ReportingInput.NULL, output, transferAmount, message, args);
	}
	
	public ReportingMessage(long transferAmount, String message, Object... args) {
		this(ReportingInput.NULL, ReportingOutput.NULL, transferAmount, message, args);
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

	public ReportingInput getInput() {
		return input;
	}

	public ReportingOutput getOutput() {
		return output;
	}
}
