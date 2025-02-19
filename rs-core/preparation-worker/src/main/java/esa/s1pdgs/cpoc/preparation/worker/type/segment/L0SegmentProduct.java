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

package esa.s1pdgs.cpoc.preparation.worker.type.segment;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.AbstractProduct;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;

public class L0SegmentProduct extends AbstractProduct {	
	static final String RFC_TYPE = "RF_RAW__0S";
	
	private static final String DATATAKE_ID = "datatakeId";
	private static final String PRODUCT_SENSING_CONSOLIDATION = "productSensingConsolidation";
	private static final String CONSOLIDATION = "consolidation";
	private static final String INSERTION_TIME = "insertionTime";
	
	public static final List<String> POLARISTATIONS = Arrays.asList("SH","SV","VH","VV","HV","HH");

	public L0SegmentProduct(final AppDataJobProductAdapter product) {
		super(product);
	}

	public static L0SegmentProduct of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static L0SegmentProduct of(final AppDataJobProduct product) {
		return new L0SegmentProduct(
				new AppDataJobProductAdapter(product)
		);
	}
	
	private static boolean isRfc(final String productType) {
		return RFC_TYPE.equals(productType);
	}
	
	private static Predicate<LevelSegmentMetadata> typeFilterFor(final String type) {
		if (type.equals("RF_RAW__0S")) {
			return p -> isRfc(p.getProductType());
		}
		// allow everything but rfc type (default segment handling)
		return p -> !isRfc(p.getProductType());
	}
	
	public final boolean isRfc() {
		return isRfc(product.getProductType());
	}
	
	public final void setAcquistion(final String swathType) {
		product.setStringValue("acquistion", swathType);		
	}
	
	public final void setStartTime(final String start) {
		product.setStartTime(start);
	}
	
	public final void setStopTime(final String stop) {
		product.setStopTime(stop);
	}
	
	public final String getAcquistion() {
		return product.getStringValue("acquistion");
	}

	public final void setDataTakeId(final String datatakeId) {
		product.setStringValue("dataTakeId", datatakeId);		
	}

	public final String getDataTakeId() {
		return product.getStringValue("dataTakeId");
	}

	public final void addSegmentMetadata(final LevelSegmentMetadata metadata) {
		// Ignore RFC/NON-RFC files mutually exclusively
		if (!typeFilterFor(product.getProductType()).test(metadata)) {
			return;
		}		
		final List<AppDataJobFile> res = product.getProductsFor(metadata.getPolarisation());
		
		// Extract t0PdgsDate if possible to determine when all inputs where ready
		Date t0 = null;
		if (metadata.getAdditionalProperties().containsKey("t0PdgsDate")) {
			t0 = DateUtils.toDate(metadata.getAdditionalProperties().get("t0PdgsDate"));
		}
		
		final AppDataJobFile segment = new AppDataJobFile(
				metadata.getProductName(), 
				metadata.getKeyObjectStorage(), 				
				TaskTableAdapter.convertDateToJobOrderFormat(metadata.getValidityStart()),
				TaskTableAdapter.convertDateToJobOrderFormat(metadata.getValidityStop()),
				t0,
				toMetadataMap(metadata)
		);

		if(isRfc()) {
			/*
			 * s1pro-2175:
			 * For RFC products, the start time can differ for the same datatake, so do not mix 
			 * them together in the same job!
			 */
			final String startDate = DateUtils.convertToAnotherFormat(
					segment.getStartDate(),
					JobOrderTimeInterval.DATE_FORMATTER,
					AbstractMetadata.METADATA_DATE_FORMATTER
					
			);			
			if (!res.contains(segment) && product.getStartTime().equals(startDate)) {
				mergeSegmentInto(metadata, res, segment);
			}

		} else {
			if (!res.contains(segment)) {
				mergeSegmentInto(metadata, res, segment);
			}
		}
		
	}

	private void mergeSegmentInto(final LevelSegmentMetadata metadata, final List<AppDataJobFile> res, final AppDataJobFile segment) {
		// Take only latest segment
		final List<AppDataJobFile> updated = merge(res, segment, metadata.getPolarisation());
		product.setProductsFor(metadata.getPolarisation(), updated);
	}

	private List<AppDataJobFile> merge(
			final List<AppDataJobFile> existingFiles, 
			final AppDataJobFile newElement,
			final String polarisation
    ) {
		final List<AppDataJobFile> updated = new ArrayList<>();
		boolean added = false;
		for (final AppDataJobFile existing : existingFiles) {			
			if (isNewerVersionOf(newElement, existing, polarisation)) {
				updated.add(newElement);
				added = true;
			}
			else if (isNewerVersionOf(existing, newElement, polarisation)) {
				updated.add(existing);
				added = true;
			}
			else {
				updated.add(existing);
			}			
		}	
		if (!added) {
			updated.add(newElement);
		}
		return updated;		
	}
		
	private boolean isNewerVersionOf(
			final AppDataJobFile newElement, 
			final AppDataJobFile existing,
			final String polarisation
	) {
		final LevelSegmentMetadata newMeta = toMetadataObject(newElement, polarisation);
		final LevelSegmentMetadata oldMeta = toMetadataObject(existing, polarisation);

		/*
		 * Other elements should already be equal when this method is called
		 * 
		 * s1pro-2175:
		 * for partial segments, the validity stop time can differ, In order to compare
		 * them with a newer complete product, do not compare the validity stop time
		 * here!
		 */
		return Objects.equals(newMeta.getValidityStart(), oldMeta.getValidityStart())
				&& DateUtils.parse(newMeta.getInsertionTime()).isAfter(DateUtils.parse(oldMeta.getInsertionTime()));
	}
	


	public final Map<String,List<LevelSegmentMetadata>> segmentsForPolaristions() {
		final Map<String,List<LevelSegmentMetadata>> result = new HashMap<>();
		
		for (final String polarisation : POLARISTATIONS) {	
			final List<LevelSegmentMetadata> meta = new ArrayList<>();
			for (final AppDataJobFile file : product.getProductsFor(polarisation)) {		
				meta.add(toMetadataObject(file, polarisation));				
			}
			result.put(polarisation, meta);
		}
		return result;	
	}
	
	private final List<AppDataJobTaskInputs> overridingInputs = new ArrayList<>();
	
	public final void overridingInputs(final List<AppDataJobTaskInputs> overridingInputs) {
		this.overridingInputs.addAll(overridingInputs);
	}
	
	@Override
	public List<AppDataJobTaskInputs> overridingInputs() {
		return overridingInputs;
	}

	private Map<String,String> toMetadataMap(final LevelSegmentMetadata metadata) {
		final Map<String,String> result = new LinkedHashMap<>();
		result.put(DATATAKE_ID, String.valueOf(metadata.getDatatakeId()));
		result.put(CONSOLIDATION, String.valueOf(metadata.getConsolidation()));
		result.put(PRODUCT_SENSING_CONSOLIDATION, String.valueOf(metadata.getProductSensingConsolidation()));
		result.put(INSERTION_TIME, metadata.getInsertionTime());
		return result;
	}
	
	private LevelSegmentMetadata toMetadataObject(final AppDataJobFile file, final String polarisation) {
		final LevelSegmentMetadata meta = new LevelSegmentMetadata();
		meta.setDatatakeId(file.getMetadata().get(DATATAKE_ID));
		meta.setConsolidation(file.getMetadata().get(CONSOLIDATION));
		meta.setProductSensingConsolidation(file.getMetadata().get(PRODUCT_SENSING_CONSOLIDATION));
		meta.setKeyObjectStorage(file.getKeyObs());
		meta.setPolarisation(polarisation);
		meta.setProductName(file.getFilename());
		
		final String startMetadataFormat = DateUtils.convertToAnotherFormat(
				file.getStartDate(),
				JobOrderTimeInterval.DATE_FORMATTER,
				AbstractMetadata.METADATA_DATE_FORMATTER
				
		);
		final String stopMetadataFormat = DateUtils.convertToAnotherFormat(
				file.getEndDate(),
				JobOrderTimeInterval.DATE_FORMATTER,
				AbstractMetadata.METADATA_DATE_FORMATTER
				
		);		
		meta.setValidityStart(startMetadataFormat);
		meta.setValidityStop(stopMetadataFormat);
		meta.setInsertionTime(file.getMetadata().get(INSERTION_TIME));
		
		if (file.getT0PdgsDate() != null) {
			meta.addAdditionalProperty("t0PdgsDate", DateUtils.formatToMetadataDateTimeFormat(
					file.getT0PdgsDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
		}
		return meta;	
	}
	
	
}
