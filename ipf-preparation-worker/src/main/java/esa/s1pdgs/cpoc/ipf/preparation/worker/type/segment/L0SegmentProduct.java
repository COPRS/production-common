package esa.s1pdgs.cpoc.ipf.preparation.worker.type.segment;

import java.util.ArrayList;
import java.util.Arrays;
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
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProduct;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;

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

	public static final L0SegmentProduct of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final L0SegmentProduct of(final AppDataJobProduct product) {
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
		
		final AppDataJobFile segment = new AppDataJobFile(
				metadata.getProductName(), 
				metadata.getKeyObjectStorage(), 
				metadata.getValidityStart(),
				metadata.getValidityStop(),
				toMetadataMap(metadata)
		);
		
		/*
		 * s1pro-2175:
		 * For RFC products, the start time can differ for the same datatake, so do not mix them together in the same job!
		 */
		if (!res.contains(segment) && product.getStartTime().equals(segment.getStartDate())) {				
			// Take only latest segment			
			final List<AppDataJobFile> updated = merge(res, segment, metadata.getPolarisation());			
			product.setProductsFor(metadata.getPolarisation(), updated);	
		}
	}	
	
	private final List<AppDataJobFile> merge(
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

	private final Map<String,String> toMetadataMap(final LevelSegmentMetadata metadata) {
		final Map<String,String> result = new LinkedHashMap<>();
		result.put(DATATAKE_ID, String.valueOf(metadata.getDatatakeId()));
		result.put(CONSOLIDATION, String.valueOf(metadata.getConsolidation()));
		result.put(PRODUCT_SENSING_CONSOLIDATION, String.valueOf(metadata.getProductSensingConsolidation()));
		result.put(INSERTION_TIME, metadata.getInsertionTime());
		return result;
	}
	
	private final LevelSegmentMetadata toMetadataObject(final AppDataJobFile file, final String polarisation) {
		final LevelSegmentMetadata meta = new LevelSegmentMetadata();
		meta.setDatatakeId(file.getMetadata().get(DATATAKE_ID));
		meta.setConsolidation(file.getMetadata().get(CONSOLIDATION));
		meta.setProductSensingConsolidation(file.getMetadata().get(PRODUCT_SENSING_CONSOLIDATION));
		meta.setKeyObjectStorage(file.getKeyObs());
		meta.setPolarisation(polarisation);
		meta.setProductName(file.getFilename());
		meta.setValidityStart(file.getStartDate());
		meta.setValidityStop(file.getEndDate());
		meta.setInsertionTime(file.getMetadata().get(INSERTION_TIME));
		return meta;	
	}
}
