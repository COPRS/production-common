package esa.s1pdgs.cpoc.ipf.preparation.worker.type.segment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobSegmentFile;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProduct;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;

public class L0SegmentProduct extends AbstractProduct {	
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

	public final void setAcquistion(final String swathType) {
		product.setStringValue("acquistion", swathType);		
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
		final List<AppDataJobFile> res = product.getProductsFor(metadata.getPolarisation());
		
		final AppDataJobSegmentFile segment = new AppDataJobSegmentFile(
				metadata.getProductName(), 
				metadata.getKeyObjectStorage(), 
				metadata.getValidityStart(),
				metadata.getValidityStop(),
				metadata
		);
		if (!res.contains(segment)) {			
			res.add(segment);
			product.setProductsFor(metadata.getPolarisation(), res);	
		}
	}	
	
	public final Map<String,List<LevelSegmentMetadata>> segmentsForPolaristions() {
		final Map<String,List<LevelSegmentMetadata>> result = new HashMap<>();
		
		for (final String polarisation : POLARISTATIONS) {	
			final List<LevelSegmentMetadata> meta = new ArrayList<>();
			for (final AppDataJobFile file : product.getProductsFor(polarisation)) {
				final AppDataJobSegmentFile segmentFile = (AppDataJobSegmentFile) file;
				meta.add(segmentFile.getMetadata());				
			}
			result.put(polarisation, meta);
		}
		return result;	
	}
}
