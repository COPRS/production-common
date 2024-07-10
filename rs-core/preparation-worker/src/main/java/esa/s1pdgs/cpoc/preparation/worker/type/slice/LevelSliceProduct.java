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

package esa.s1pdgs.cpoc.preparation.worker.type.slice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobPreselectedInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.AbstractProduct;

public class LevelSliceProduct extends AbstractProduct {
	private static final String SLICE = "slice";
	private static final String ACN = "acn";
	
	private final List<AppDataJobPreselectedInput> preselectedInputs = new ArrayList<>();
	
	public LevelSliceProduct(final AppDataJobProductAdapter product) {
		super(product);
	}

	public static final LevelSliceProduct of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final LevelSliceProduct of(final AppDataJobProduct product) {
		return new LevelSliceProduct(
				new AppDataJobProductAdapter(product)
		);
	}


	public final void setInsConfId(final int instrumentConfigurationId) {
		product.setInsConfId(instrumentConfigurationId);		
	}

	public final void setNumberSlice(final int numberSlice) {
		product.setIntegerValue("numberSlice", numberSlice);
	}
	
	public final int getNumberSlice() {
		return product.getIntegerValue("numberSlice");
	}

	public final void setDataTakeId(final String datatakeId) {
		product.setStringValue("dataTakeId", datatakeId);			
	}

	public final void setTotalNbOfSlice(final int numberOfSlices) {
		product.setIntegerValue("totalNbOfSlice", numberOfSlices);		
	}
	
	public final int getTotalNbOfSlice() {
		return product.getIntegerValue("totalNbOfSlice");
	}

	public final void setSegmentStartDate(final String validityStart) {
		product.setStringValue("segmentStartDate", validityStart);		
	}

	public final void setSegmentStopDate(final String validityStop) {
		product.setStringValue("segmentStopDate", validityStop);
	}

	public final void setAcquisition(final String swathType) {
		product.setStringValue("acquistion", swathType);	
	}

	public final String getAcquisition() {
		return product.getStringValue("acquistion");
	}
	
	public final void setPolarisation(final String polarisation) {
		product.setStringValue("polarisation", polarisation);
	}
	
	public final String getPolarisation() {
		return product.getStringValue("polarisation");
	}

	public final String getSegmentStartDate() {
		return product.getStringValue("segmentStartDate");
	}

	public final String getSegmentStopDate() {
		return product.getStringValue("segmentStopDate");
	}

	public final void addSlice(final L0SliceMetadata file) {
		final List<AppDataJobFile> slices = product.getProductsFor(SLICE);
		
		// Extract t0PdgsDate if possible to determine when all inputs where ready
		Date t0 = null;
		if (file.getAdditionalProperties().containsKey("t0PdgsDate")) {
			t0 = DateUtils.toDate(file.getAdditionalProperties().get("t0PdgsDate"));
		}
		
		final AppDataJobFile slice = new AppDataJobFile(
				file.getProductName(), 
				file.getKeyObjectStorage(), 
				TaskTableAdapter.convertDateToJobOrderFormat(file.getValidityStart()),
				TaskTableAdapter.convertDateToJobOrderFormat(file.getValidityStop()),
				t0
		);
		if (!slices.contains(slice)) {
			slices.add(slice);
			product.setProductsFor(SLICE, slices);
		}	
	}

	public final void addAcn(final L0AcnMetadata file) {
		final List<AppDataJobFile> acns = product.getProductsFor(ACN);
		
		// Extract t0PdgsDate if possible to determine when all inputs where ready
		Date t0 = null;
		if (file.getAdditionalProperties().containsKey("t0PdgsDate")) {
			t0 = DateUtils.toDate(file.getAdditionalProperties().get("t0PdgsDate"));
		}
		
		final AppDataJobFile acn = new AppDataJobFile(
				file.getProductName(), 
				file.getKeyObjectStorage(), 
				TaskTableAdapter.convertDateToJobOrderFormat(file.getValidityStart()),
				TaskTableAdapter.convertDateToJobOrderFormat(file.getValidityStop()),
				t0
		);
		if (!acns.contains(acn)) {
			acns.add(acn);
			product.setProductsFor(ACN, acns);
		}
	}
	
	public final List<AppDataJobFile> getSlices() {
		return product.getProductsFor(SLICE);
	}
	
	public final List<AppDataJobFile> getAcns() {
		return product.getProductsFor(ACN);
	}
	
	public final String getTimeliness() {
		return product.getStringValue("timeliness", "");
	}
	
    public final void addPreselectedInputs(final AppDataJobPreselectedInput preselectedInput) {
    	this.preselectedInputs.add(preselectedInput);
    }

	@Override
	public List<AppDataJobPreselectedInput> preselectedInputs() {
		return preselectedInputs;
	}	
}
