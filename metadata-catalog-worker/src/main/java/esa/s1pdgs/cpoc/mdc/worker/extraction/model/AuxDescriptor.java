package esa.s1pdgs.cpoc.mdc.worker.extraction.model;

import java.util.Objects;

/**
 * Class describing a configuration file (AUX and MPL)
 * 
 * @author Cyrielle Gailliard
 *
 */
public class AuxDescriptor extends AbstractFileDescriptor {
	@Override
	public int hashCode() {
		return Objects.hash(extension, filename, keyObjectStorage, missionId, mode, productClass, productFamily,
				productName, productType, relativePath, satelliteId);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AuxDescriptor other = (AuxDescriptor) obj;
		return extension == other.extension && Objects.equals(filename, other.filename)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(missionId, other.missionId) && Objects.equals(mode, other.mode)
				&& Objects.equals(productClass, other.productClass) && productFamily == other.productFamily
				&& Objects.equals(productName, other.productName) && Objects.equals(productType, other.productType)
				&& Objects.equals(relativePath, other.relativePath) && Objects.equals(satelliteId, other.satelliteId);
	}

	@Override
	public String toString() {
		return "AuxDescriptor [productType=" + productType + ", productClass=" + productClass + ", relativePath="
				+ relativePath + ", filename=" + filename + ", extension=" + extension + ", productName=" + productName
				+ ", missionId=" + missionId + ", satelliteId=" + satelliteId + ", keyObjectStorage=" + keyObjectStorage
				+ ", productFamily=" + productFamily + ", mode=" + mode + "]";
	}
}
