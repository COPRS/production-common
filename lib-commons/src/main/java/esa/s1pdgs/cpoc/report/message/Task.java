package esa.s1pdgs.cpoc.report.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.Reporting.Event;

@JsonInclude(Include.NON_NULL)
public class Task {
	private String uid;
	private String name;
	private Event event;
	
	@JsonProperty("data_rate_mebibytes_sec")
	private double rate;
	
	@JsonProperty("data_volume_mebibytes")
	private double volume;
	
	private String satellite;
	
	public Task() {
	}
	
	public Task(final String uid, final String name, final Event event) {
		this.uid = uid;
		this.name = name;
		this.event = event;
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(final String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(final Event event) {
		this.event = event;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(final double rate) {
		this.rate = rate;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(final double volume) {
		this.volume = volume;
	}

	public String getSatellite() {
		return satellite;
	}

	public void setSatellite(final String satellite) {
		this.satellite = satellite;
	}
}
