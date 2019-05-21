package esa.s1pdgs.cpoc.report;

public interface Reporting {
	public interface Factory {

		Factory product(String family, String productName);

		Reporting newReporting(int step);

	}

	void reportStart(String comment);

	void reportDebug(String comment, Object... objects);

	void reportStop(String comment);
	
	void reportStopWithTransfer(String comment, long transferAmount);

	void reportError(String comment, Object... objects);

}