package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class DisseminationJob extends AbstractMessage {
	
	private List<DisseminationSource> disseminationSources = new ArrayList<>();
	
	public DisseminationJob() {
		super();
		allowedActions = Collections.singletonList(AllowedAction.RESTART);
	}
	
	public void addDisseminationSource(ProductFamily productFamily, String keyObjectStorage) {
		this.disseminationSources.add(new DisseminationSource(productFamily, keyObjectStorage));
	}
	
	public List<DisseminationSource> getDisseminationSources() {
		return this.disseminationSources;
	}
	
	public void setDisseminationSources(List<DisseminationSource> disseminationSources) {
		this.disseminationSources = disseminationSources;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, productFamily, uid,
				allowedActions, demandType, debug, retryCounter);
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
		final DisseminationJob other = (DisseminationJob) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname) 
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(uid, other.uid)
				&& productFamily == other.productFamily
				&& Objects.equals(allowedActions, other.getAllowedActions())
		        && demandType == other.demandType
		        && debug == other.debug
		        && retryCounter == other.retryCounter
		        && Objects.equals(disseminationSources, other.disseminationSources);
	}

	@Override
	public String toString() {
		return "DisseminationJob [disseminationSources=" + disseminationSources + ", productFamily=" + productFamily
				+ ", keyObjectStorage=" + keyObjectStorage + ", uid=" + uid + ", creationDate=" + creationDate
				+ ", hostname=" + hostname + ", allowedActions=" + allowedActions + ", demandType=" + demandType
				+ ", retryCounter=" + retryCounter + ", debug=" + debug + "]";
	}
}
