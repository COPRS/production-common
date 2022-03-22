package esa.s1pdgs.cpoc.mdc.worker.extraction.model;

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
