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

package esa.s1pdgs.cpoc.appcatalog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;

/**
 * Class used for exchanging applicative data of a job
 * 
 * @author Viveris Technologies
 */
public class AppDataJob {

	// dirty workaround to have the mongodb id mapped
	private long id;

	/**
	 * Application Level: L0 or L1
	 */
	private ApplicationLevel level;

	/**
	 * Name of the pod generating the jobs
	 */
	private String pod;

	/**
	 * Global state of the job (aggregation of the states of all its job
	 * generations)
	 */
	private AppDataJobState state;

	private String taskTableName;

	private String startTime;

	private String stopTime;

	private String productName;

	/**
	 * Date when the job is created
	 */
	private Date creationDate;

	/**
	 * Date of the last modification done on the job
	 */
	private Date lastUpdateDate;
	
	/**
	 * Date when the Job will be timeout
	 */
	private Date timeoutDate;

	/**
	 * Catalog Events linked to this job
	 */
	private List<CatalogEvent> catalogEvents = new ArrayList<>();

	/**
	 * Product of this job
	 */
	private AppDataJobProduct product = new AppDataJobProduct();

	/**
	 * Additional inputs for the job, e.g. aux files
	 */
	private List<AppDataJobTaskInputs> additionalInputs;

	/**
	 * Inputs preselected by main input search, which should not be queries by
	 * AuxQuery any more
	 */
	private List<AppDataJobPreselectedInput> preselectedInputs = new ArrayList<>();

	/**
	 * List of product types that shall trigger the check for inputs
	 */
	private List<String> triggerProducts = new ArrayList<>();
	/**
	 * Generations of the job
	 */
	private AppDataJobGeneration generation;

	private UUID reportingId;

	private IpfPreparationJob prepJob;

	/**
	 * Processing group to identify AppDataJobs in the JobGenerator. Is used
	 * additionally to the tasktableName to determine if a job is suitable for a
	 * generator. Needed if two separate preparation worker use the same TaskTable
	 * with different settings (ex. timeliness)
	 */
	private String processingGroup;

	private boolean timedOut = false;

	/**
	 * generate an AppDataJob from an IpfPreparationJob
	 * 
	 * @param prepJob IpfPreparationJob to extract information for AppDataJob
	 * @return new instance of AppDataJob (not saved in database)
	 */
	public static AppDataJob fromPreparationJob(final IpfPreparationJob prepJob) {
		final AppDataJob job = new AppDataJob();
		job.setLevel(prepJob.getLevel());
		job.setPod(prepJob.getPodName());
		job.getCatalogEvents().add(prepJob.getCatalogEvent());
		job.setProduct(newProductFor(prepJob.getCatalogEvent()));
		job.setTaskTableName(prepJob.getTaskTableName());
		job.setStartTime(prepJob.getStartTime());
		job.setStopTime(prepJob.getStopTime());
		job.setProductName(prepJob.getKeyObjectStorage());
		job.setTriggerProducts(prepJob.getTriggerProducts());
		
		return job;
	}

	private static AppDataJobProduct newProductFor(final CatalogEvent event) {
		final AppDataJobProduct productDto = new AppDataJobProduct();

		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(event);
		productDto.getMetadata().put("productName", event.getProductName());
		productDto.getMetadata().put("productType", event.getMetadataProductType());
		productDto.getMetadata().put("satelliteId", eventAdapter.satelliteId());
		productDto.getMetadata().put(MissionId.FIELD_NAME, eventAdapter.missionId());
		productDto.getMetadata().put("processMode", eventAdapter.processMode());
		productDto.getMetadata().put("startTime", eventAdapter.productSensingStartDate());
		productDto.getMetadata().put("stopTime", eventAdapter.productSensingStopDate());
		productDto.getMetadata().put("timeliness", eventAdapter.timeliness());
		productDto.getMetadata().put("acquistion", eventAdapter.swathType());
		return productDto;
	}

	public AppDataJob(final long id) {
		this();
		this.id = id;
	}

	/**
	 * 
	 */
	public AppDataJob() {
		this.state = AppDataJobState.WAITING;
		this.catalogEvents = new ArrayList<>();
		this.generation = new AppDataJobGeneration();
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	/**
	 * @return the level
	 */
	public ApplicationLevel getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(final ApplicationLevel level) {
		this.level = level;
	}

	/**
	 * @return the pod
	 */
	public String getPod() {
		return pod;
	}

	/**
	 * @param pod the pod to set
	 */
	public void setPod(final String pod) {
		this.pod = pod;
	}

	/**
	 * @return the state
	 */
	public AppDataJobState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(final AppDataJobState state) {
		this.state = state;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(final Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the lastUpdateDate
	 */
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	/**
	 * @param lastUpdateDate the lastUpdateDate to set
	 */
	public void setLastUpdateDate(final Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	/**
	 * @return date, when the job will be timed out
	 */
	public Date getTimeoutDate() {
		return timeoutDate;
	}

	/**
	 * @param timeoutDate new timeout date for the job
	 */
	public void setTimeoutDate(Date timeoutDate) {
		this.timeoutDate = timeoutDate;
	}

	/**
	 * @return the catalogEvents
	 */
	public List<CatalogEvent> getCatalogEvents() {
		return catalogEvents;
	}

	/**
	 * @param catalogEvents the catalogEvents to set
	 */
	public void setCatalogEvents(final List<CatalogEvent> catalogEvents) {
		this.catalogEvents = catalogEvents;
	}

	/**
	 * @return the product
	 */
	public AppDataJobProduct getProduct() {
		return product;
	}

	/**
	 * @param product the product to set
	 */
	public void setProduct(final AppDataJobProduct product) {
		this.product = product;
	}

	/**
	 *
	 * @return additional job inputs
	 */
	public List<AppDataJobTaskInputs> getAdditionalInputs() {
		return additionalInputs;
	}

	/**
	 *
	 * @param additionalInputs additional job inputs
	 */
	public void setAdditionalInputs(final List<AppDataJobTaskInputs> additionalInputs) {
		this.additionalInputs = additionalInputs;
	}

	public List<AppDataJobPreselectedInput> getPreselectedInputs() {
		return preselectedInputs;
	}

	public void setPreselectedInputs(final List<AppDataJobPreselectedInput> preselectedInputs) {
		this.preselectedInputs = preselectedInputs;
	}

	/**
	 * @return the generations
	 */
	public AppDataJobGeneration getGeneration() {
		return generation;
	}

	/**
	 * @param generation the generation to set
	 */
	public void setGeneration(final AppDataJobGeneration generation) {
		this.generation = generation;
	}

	public UUID getReportingId() {
		return reportingId;
	}

	public void setReportingId(final UUID reportingId) {
		this.reportingId = reportingId;
	}

	public String getTaskTableName() {
		return taskTableName;
	}

	public void setTaskTableName(final String taskTableName) {
		this.taskTableName = taskTableName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(final String startTime) {
		this.startTime = startTime;
	}

	public String getStopTime() {
		return stopTime;
	}

	public void setStopTime(final String stoptime) {
		this.stopTime = stoptime;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public IpfPreparationJob getPrepJob() {
		return prepJob;
	}

	public void setPrepJob(final IpfPreparationJob prepJob) {
		this.prepJob = prepJob;
	}

	public String getProcessingGroup() {
		return processingGroup;
	}

	public void setProcessingGroup(final String processingGroup) {
		this.processingGroup = processingGroup;
	}

	public boolean getTimedOut() {
		return timedOut;
	}

	public void setTimedOut(final boolean timedOut) {
		this.timedOut = timedOut;
	}

	public List<String> getTriggerProducts() {
		return triggerProducts;
	}

	public void setTriggerProducts(List<String> triggerProducts) {
		this.triggerProducts = triggerProducts;
	}

	@Override
	public String toString() {
		return "AppDataJob [id=" + id + ", level=" + level + ", pod=" + pod + ", state=" + state + ", taskTableName="
				+ taskTableName + ", startTime=" + startTime + ", stopTime=" + stopTime + ", productName=" + productName
				+ ", creationDate=" + creationDate + ", lastUpdateDate=" + lastUpdateDate + ", catalogEvents="
				+ catalogEvents + ", product=" + product + ", additionalInputs=" + additionalInputs + ", generation="
				+ generation + ", reportingId=" + reportingId + ", prepJob=" + prepJob + ", processingGroup="
				+ processingGroup + ", timedOut=" + timedOut + ", preselectedInputs=" + preselectedInputs
				+ ", triggerProducts=" + triggerProducts + ", timeoutDate=" + timeoutDate + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(additionalInputs, creationDate, generation, id, lastUpdateDate, level, catalogEvents, pod,
				prepJob, processingGroup, product, productName, reportingId, startTime, state, stopTime, taskTableName,
				timedOut, preselectedInputs, triggerProducts, timeoutDate);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || !(AppDataJob.class.equals(obj.getClass()))) {
			return false;
		}
		final AppDataJob other = (AppDataJob) obj;
		return Objects.equals(additionalInputs, other.additionalInputs)
				&& Objects.equals(creationDate, other.creationDate) && Objects.equals(generation, other.generation)
				&& id == other.id && Objects.equals(lastUpdateDate, other.lastUpdateDate) && level == other.level
				&& Objects.equals(catalogEvents, other.catalogEvents) && timedOut == other.timedOut
				&& Objects.equals(pod, other.pod) && Objects.equals(prepJob, other.prepJob)
				&& Objects.equals(processingGroup, other.processingGroup) && Objects.equals(product, other.product)
				&& Objects.equals(productName, other.productName) && Objects.equals(reportingId, other.reportingId)
				&& Objects.equals(startTime, other.startTime) && state == other.state
				&& Objects.equals(stopTime, other.stopTime) && Objects.equals(taskTableName, other.taskTableName)
				&& Objects.equals(preselectedInputs, other.preselectedInputs)
				&& Objects.equals(triggerProducts, other.triggerProducts)
				&& Objects.equals(timeoutDate, other.timeoutDate);
	}
}
