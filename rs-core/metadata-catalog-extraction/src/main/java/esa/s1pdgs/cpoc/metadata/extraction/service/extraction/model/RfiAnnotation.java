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

import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "rfi")
@XmlAccessorType(XmlAccessType.NONE)
public class RfiAnnotation {
	
	
	@XmlElementWrapper(name = "rfiDetectionFromNoiseReportList")
	@XmlElement(name = "rfiDetectionFromNoiseReport")
	private List<RfiDetectionFromNoiseReport> detectionFromNoiseReports;

	public List<RfiDetectionFromNoiseReport> getDetectionFromNoiseReports() {
		return detectionFromNoiseReports;
	}

	public void setDetectionFromNoiseReports(List<RfiDetectionFromNoiseReport> detectionFromNoiseReports) {
		this.detectionFromNoiseReports = detectionFromNoiseReports;
	}

	@Override
	public int hashCode() {
		return Objects.hash(detectionFromNoiseReports);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RfiAnnotation other = (RfiAnnotation) obj;
		return Objects.equals(detectionFromNoiseReports, other.detectionFromNoiseReports);
	}

	@Override
	public String toString() {
		return "RfiAnnotation [detectionFromNoiseReports=" + detectionFromNoiseReports + "]";
	}
	
}
