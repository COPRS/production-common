package esa.s1pdgs.cpoc.validation.service;

public class DataLifecycleSyncStats {

	private int errors = 0;
	private int unchanged = 0;
	private int newCreated = 0;
	private int familyUpdated = 0;
	private int pathUpdated = 0;

	public void incrErrors() {
		++errors;
	}

	public void incrUnchanged() {
		++unchanged;
	}

	public void incrNewCreated() {
		++newCreated;
	}

	public void incrFamilyUpdated() {
		++familyUpdated;
	}

	public void incrPathUpdated() {
		++pathUpdated;
	}

	public int getErrors() {
		return errors;
	}

	public int getUnchanged() {
		return unchanged;
	}

	public int getNewCreated() {
		return newCreated;
	}

	public int getFamilyUpdated() {
		return familyUpdated;
	}

	public int getPathUpdated() {
		return pathUpdated;
	}

	@Override
	public String toString() {
		return "DataLifecycleSyncStats [errors=" + errors + ", unchanged=" + unchanged + ", newCreated="
				+ newCreated + ", familyUpdated=" + familyUpdated + ", pathUpdated=" + pathUpdated + "]";
	}

}
