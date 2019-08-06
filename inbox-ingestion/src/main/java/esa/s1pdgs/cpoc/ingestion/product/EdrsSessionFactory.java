package esa.s1pdgs.cpoc.ingestion.product;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.ingestion.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;

// FIXME
public class EdrsSessionFactory implements ProductFactory<EdrsSessionDto> {
		
	// copied from old implementation.
    private final static String PATTERN_STR =
    		"^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";

    /**
     * Pattern
     */
    protected final Pattern pattern = Pattern.compile(PATTERN_STR, Pattern.CASE_INSENSITIVE);

	@Override
	public List<Product<EdrsSessionDto>> newProducts(File file, final ObsAdapter obsAdapter) throws ProductException {
		throw new UnsupportedOperationException();
	}
	
//  
//	public Product<E> newProduct(final ProductFamily family, final String path) throws ProductException {
//				
//		if (family == ProductFamily.EDRS_SESSION) {
//	        final Matcher matcher = pattern.matcher(path);
//
//	        if (!matcher.matches()) {
//	            throw new ProductException(
//	            		String.format("%s File %s does not match pattern %s", family, path, PATTERN_STR)
//	            );
//	        }
//
//	        // Ignore the IIF files
//	        if (matcher.group(11).toLowerCase().contains("iif_")) {
//	        	 throw new ProductException(
//		            		String.format("%s File %s does not match pattern %s", family, path, PATTERN_STR)
//		            );
//	        	
//	        	
//	            throw new IngestorFilePathException(relativePath, family,
//	                    "IIF file");
//	        }
//
//	        // "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";
//	        FileDescriptor descriptor = new FileDescriptor();
//	        descriptor.setRelativePath(relativePath);
//	        descriptor.setProductName(matcher.group(9));
//	        descriptor.setExtension(
//	                FileExtension.valueOfIgnoreCase(matcher.group(12)));
//	        descriptor.setProductType(EdrsSessionFileType
//	                .valueFromExtension(descriptor.getExtension()));
//	        descriptor.setChannel(Integer.parseInt(matcher.group(7)));
//	        descriptor.setKeyObjectStorage(relativePath);
//	        descriptor.setMissionId(matcher.group(1));
//	        descriptor.setSatelliteId(matcher.group(2));
//	        descriptor.setHasToBePublished(true);
//			
//		}
//		
//		
//		
//		
//		
//		return new Product<>();
//	}

}
