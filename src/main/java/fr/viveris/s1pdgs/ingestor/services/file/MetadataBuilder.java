package fr.viveris.s1pdgs.ingestor.services.file;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerException;

import org.json.JSONException;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.ingestor.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileType;
import fr.viveris.s1pdgs.ingestor.model.dto.KafkaMetadataDto;

/**
 * Class to build metadata for configuration and ERDS session files
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class MetadataBuilder {

	/**
	 * Build metadata from configuration files
	 * 
	 * @param descriptor
	 * @param file
	 * @return
	 */
	// TODO (throw an exceptionif error)
	public KafkaMetadataDto buildConfigFileMetadata(ConfigFileDescriptor descriptor, File file) {
		/*String info = String.format(
				"{'productName': %s, 'productClass': %s, 'productType': %s, 'missionId': %s, 'satelliteId': %s, 'keyObjectStorage': %s}",
				descriptor.getProductName(), descriptor.getProductClass(), descriptor.getProductType(), descriptor.getMissionId(), descriptor.getSatelliteId(),
				descriptor.getKeyObjectStorage());*/
		KafkaMetadataDto metadata = new KafkaMetadataDto();
		metadata.setAction("CREATE");
		metadata.setMetadata(null);
		ExtractMetadata extractor = new ExtractMetadata();
		if(descriptor.getProductType().equals("AUX_OBMEMC")) {
			try {
				metadata.setMetadata(extractor.processAUXXMLFile(descriptor, file));
			} catch (IOException | URISyntaxException | TransformerException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(descriptor.getProductType().equals("MPL_ORBPRE") || descriptor.getProductType().equals("MPL_ORBSCT")) {
			try {
				metadata.setMetadata(extractor.processMPLEOFFile(descriptor, file));
			} catch (IOException | URISyntaxException | TransformerException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (descriptor.getProductType().equals("AUX_RESORB")) {
			try {
				metadata.setMetadata(extractor.processAUXEOFFile(descriptor, file));
			} catch (IOException | URISyntaxException | TransformerException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			try {
				metadata.setMetadata(extractor.processAUXMANIFESTFile(descriptor, file));
			} catch (IOException | URISyntaxException | TransformerException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return metadata;
	}

	/**
	 * Build metadata for ERDS session files
	 * 
	 * @param descriptor
	 * @param file
	 * @return
	 */
	// TODO (throw an exceptionif error)
	public KafkaMetadataDto buildErdsSessionFileMetadata(ErdsSessionFileDescriptor descriptor, File file) {
		/*String info = String.format(
				"{ 'sessionIdentifier': %s, 'productName': %s, 'productType': %s, 'channel': %d, 'missionId': %s, 'satelliteId': %s, 'keyObjectStorage': %s}",
				descriptor.getSessionIdentifier(), descriptor.getProductName(), descriptor.getProductType(),
				descriptor.getChannel(), descriptor.getMissionId(), descriptor.getSatelliteId(),
				descriptor.getKeyObjectStorage());*/
		KafkaMetadataDto metadata = new KafkaMetadataDto();
		metadata.setAction("CREATE");
		metadata.setMetadata(null);
		ExtractMetadata extractor = new ExtractMetadata();
		if(descriptor.getProductType() == ErdsSessionFileType.RAW) {
			try {
				metadata.setMetadata(extractor.processRAWFile(descriptor));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(descriptor.getProductType() == ErdsSessionFileType.SESSION) {
			try {
				metadata.setMetadata(extractor.processSESSIONFile(descriptor));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return metadata;
	}
}
