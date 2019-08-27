package esa.s1pdgs.cpoc.report;

public interface Reporting {
	public interface Factory {

		Factory product(String family, String productName);

		Reporting newReporting(int step);

	}

	void begin(String comment);

	void intermediate(String comment, Object... objects);

	void end(String comment);
	
	void endWithTransfer(String comment, long transferAmount);

	void error(String comment, Object... objects);

}