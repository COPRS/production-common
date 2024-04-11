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

package esa.s1pdgs.cpoc.appstatus;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import esa.s1pdgs.cpoc.common.ProductCategory;

public interface AppStatus {
	
	public static final AppStatus NULL = new AppStatus() {		
		@Override public final Status getStatus() {return Status.NULL;}		
		@Override public final Map<ProductCategory, Status> getSubStatuses() { return Collections.emptyMap(); }
		@Override public final void addSubStatus(final Status subStatus) {}
		@Override public final void setWaiting() {}		
		@Override public final void setStopping() {}		
		@Override public final void setShallBeStopped(final boolean shallBeStopped) {}		
		@Override public final void setProcessing(final long processingMsgId) {}
		@Override public final void setError(final String type) {}
		@Override public final boolean isShallBeStopped() {return false;}
		@Override public final long getProcessingMsgId() { return Status.PROCESSING_MSG_ID_UNDEFINED; }
		@Override public final boolean isProcessing(final String category, final long messageId) {return false;}
		@Override public final void forceStopping() {}
		@Override public boolean getKubernetesReadiness() { return false; }
	};
	
	default boolean isInterrupted() {
		return Thread.currentThread().isInterrupted();
	}
	
	default void sleep(final long millis) throws InterruptedException {
		Thread.sleep(millis);
	}

	/**
	 * @return the status of the application
	 */
	Status getStatus();

	/**
	 * @return the status per category
	 */
	Map<ProductCategory, Status> getSubStatuses();

	/**
	 * @param statusPerCategory
	 */
	void addSubStatus(Status subStatus) throws IllegalArgumentException;
	
	/**
	 * @return the processingMsgId
	 */
	long getProcessingMsgId();

	/**
	 * Set application as waiting
	 */
	void setWaiting();

	/**
	 * Set application as processing
	 */
	void setProcessing(long processingMsgId);

	/**
	 * Set application as stopping
	 */
	void setStopping();

	/**
	 * Set application as error
	 */
	void setError(String type);

	/**
	 * @return the shallBeStopped
	 */
	boolean isShallBeStopped();

	/**
	 * @param shallBeStopped
	 *            the shallBeStopped to set
	 */
	void setShallBeStopped(boolean shallBeStopped);

	/**
	 * @param category
	 * @param messageId
	 * @return is job processing
	 */
	boolean isProcessing(String category, long messageId) throws UnsupportedOperationException, NoSuchElementException, IllegalArgumentException;

	/**
	 * @return kubernetes readiness
	 */
	boolean getKubernetesReadiness();
	
	/**
	 * Stop the application if someone asks for forcing stop
	 */
	void forceStopping();

}