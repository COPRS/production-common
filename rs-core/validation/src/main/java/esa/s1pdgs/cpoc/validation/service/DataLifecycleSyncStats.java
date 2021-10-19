package esa.s1pdgs.cpoc.validation.service;

public class DataLifecycleSyncStats {

	private long errors = 0;
	private long ignored = 0;
	private long unchanged = 0;
	private long newCreated = 0;
	private long familyUpdated = 0;
	private long pathUpdated = 0;

	public void incrErrors() {
		++errors;
	}
	
	public void incrIgnored() {
		++ignored;
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

	public long getErrors() {
		return errors;
	}
	
	public long getIgnored() {
		return ignored;
	}

	public long getUnchanged() {
		return unchanged;
	}

	public long getNewCreated() {
		return newCreated;
	}

	public long getFamilyUpdated() {
		return familyUpdated;
	}

	public long getPathUpdated() {
		return pathUpdated;
	}
	
	@Override
	public String toString() {
		return "DataLifecycleSyncStats [errors=" + errors + ", unchanged=" + unchanged + ", ignored=" + ignored
				+ ", newCreated=" + newCreated + ", familyUpdated=" + familyUpdated + ", pathUpdated=" + pathUpdated + "]";
	}

}
