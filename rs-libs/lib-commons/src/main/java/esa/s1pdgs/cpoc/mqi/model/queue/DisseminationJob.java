/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((disseminationSources == null) ? 0 : disseminationSources.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DisseminationJob other = (DisseminationJob) obj;
		if (disseminationSources == null) {
			if (other.disseminationSources != null)
				return false;
		} else if (!disseminationSources.equals(other.disseminationSources))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DisseminationJob [disseminationSources=" + disseminationSources + ", productFamily=" + productFamily
				+ ", keyObjectStorage=" + keyObjectStorage + ", storagePath=" + storagePath + ", uid=" + uid
				+ ", creationDate=" + creationDate + ", podName=" + podName + ", allowedActions=" + allowedActions
				+ ", demandType=" + demandType + ", retryCounter=" + retryCounter + ", debug=" + debug + ", rsChainVersion=" + rsChainVersion + "]";
	}
}
