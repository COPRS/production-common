package esa.s1pdgs.cpoc.mdc.worker.extraction.model;

import java.util.Objects;

/**
 * File descriptor for Sentinel 3 mission files
 * 
 * Based on Sentinel 3 PDGS File Naming Convention.
 * This file descriptor is usable for all products in the S3 mission
 * 
 * @author Julian Kaping
 *
 */
public class S3FileDescriptor extends AbstractFileDescriptor {
	
	/*
	 * only relevant for instrument products (17 underscores for auxiliary)
	 * 
	 * products disseminated in stripes: DDDD_CCC_LLL_____
	 * products disseminated in frames:  DDDD_CCC_LLL_FFFF
	 * products disseminated in tiles:   ttttttttttttttttt
	 * 
	 * DDDD: orbit duration
	 * CCC: cycle number at starting time
	 * LLL: relative orbit number within the cycle
	 * FFFF: elapsed time in seconds from the ascending node (frame starting time)
	 * ttttttttttttttttt: tile identifier (ex. GLOBAL___________)
	 */
	protected String instanceId;
	
	/*
	 * 3 character string, indicating the product generating centre
	 */
	protected String generatingCentre;
	
	/*
	 * 8 characters, underscores if not relevant
	 * 
	 * P_XX_NNN
	 * 
	 * P: platform (O, F, D, R)
	 * XX: timeliness (NR, ST, NT, SN, NS, NN, AL)
	 * NNN: free text for indicatng baseline collection or data usage 
	 */
	protected String classId;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(final String instanceId) {
		this.instanceId = instanceId;
	}

	public String getGeneratingCentre() {
		return generatingCentre;
	}

	public void setGeneratingCentre(final String generatingCentre) {
		this.generatingCentre = generatingCentre;
	}

	public String getClassId() {
		return classId;
	}

	public void setClassId(final String classId) {
		this.classId = classId;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(instanceId, generatingCentre, classId, extension, filename, keyObjectStorage, missionId,
				mode, productClass, productFamily, productName, productType, relativePath, satelliteId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		final S3FileDescriptor other = (S3FileDescriptor) obj;
		return Objects.equals(instanceId, other.instanceId) && Objects.equals(generatingCentre, other.generatingCentre)
				&& Objects.equals(classId, other.classId) && extension == other.extension
				&& Objects.equals(filename, other.filename) && Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(missionId, other.missionId) && Objects.equals(mode, other.mode)
				&& Objects.equals(productClass, other.productClass) && productFamily == other.productFamily
				&& Objects.equals(productName, other.productName) && Objects.equals(productType, other.productType)
				&& Objects.equals(relativePath, other.relativePath) && Objects.equals(satelliteId, other.satelliteId);
	}

	@Override
	public String toString() {
		return "S3FileDescriptor [instanceId=" + instanceId + ", generatingCentre=" + generatingCentre + ", classId="
				+ classId + ", productType=" + productType + ", productClass=" + productClass + ", relativePath="
				+ relativePath + ", filename=" + filename + ", extension=" + extension + ", productName=" + productName
				+ ", missionId=" + missionId + ", satelliteId=" + satelliteId + ", keyObjectStorage=" + keyObjectStorage
				+ ", productFamily=" + productFamily + ", mode=" + mode + "]";
	}
}
