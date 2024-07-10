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

package esa.s1pdgs.cpoc.preparation.worker.type.pdu;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.AbstractProduct;

public class PDUProduct extends AbstractProduct {

	public static final String FRAME_NUMBER = "frameNumber";
	public static final String PDU_TIME_INTERVALS = "PDUTimeIntervals";
	
	public static final PDUProduct of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final PDUProduct of(final AppDataJobProduct product) {
		return new PDUProduct(new AppDataJobProductAdapter(product));
	}
	
	/**
	 * List of additional inputs, that were set during the main input search
	 */
	private List<AppDataJobTaskInputs> additionalInputs;
	

	public PDUProduct(final AppDataJobProductAdapter product) {
		super(product);
	}
	
	public final void setFrameNumber(final int frameNumber) {
		product.setIntegerValue(FRAME_NUMBER, frameNumber);
	}
	
	public final int getFrameNumber() {
		return product.getIntegerValue(FRAME_NUMBER);
	}
	
	public void setAdditionalInputs(final List<AppDataJobTaskInputs> additionalInputs) {
		this.additionalInputs = additionalInputs;
	}
	
	public final void setStartTime(final String start) {
		product.setStartTime(start);
	}
	
	public final void setStopTime(final String stop) {
		product.setStopTime(stop);
	}
	
	public final void setPDUTimeIntervals(final String pduTimeIntervals) {
		product.setStringValue(PDU_TIME_INTERVALS, pduTimeIntervals);
	}
	
	public final String getPDUTimeIntervals() {
		return product.getStringValue(PDU_TIME_INTERVALS);
	}
	
	@Override
	public List<AppDataJobTaskInputs> overridingInputs() {
		return additionalInputs;
	}
}
