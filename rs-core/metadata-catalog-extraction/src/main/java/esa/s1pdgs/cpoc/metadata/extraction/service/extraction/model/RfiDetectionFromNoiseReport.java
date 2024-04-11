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

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "rfiDetectionFromNoiseReport")
@XmlAccessorType(XmlAccessType.NONE)
public class RfiDetectionFromNoiseReport {
	
	
	@XmlElement(name = "rfiDetected")
	private boolean rfiDetected;

	public boolean isRfiDetected() {
		return rfiDetected;
	}

	public void setRfiDetected(boolean rfiDetected) {
		this.rfiDetected = rfiDetected;
	}

	@Override
	public int hashCode() {
		return Objects.hash(rfiDetected);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RfiDetectionFromNoiseReport other = (RfiDetectionFromNoiseReport) obj;
		return rfiDetected == other.rfiDetected;
	}

	@Override
	public String toString() {
		return "RfiDetectionFromNoiseReport [rfiDetected=" + rfiDetected + "]";
	}

}
