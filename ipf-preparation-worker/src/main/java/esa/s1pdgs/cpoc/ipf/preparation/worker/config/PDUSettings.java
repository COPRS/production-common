package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.pdu.PDUType;

/**
 * Additional settings used to configure the PDU type adapter
 * 
 * @author Julian Kaping
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "pdu")
public class PDUSettings {

	public static class PDUTypeSettings {
		
		/**
		 * Length of frames or stripes
		 */
		private long lengthInS;
		
		/**
		 * Type of PDUs that should be generated
		 */
		private PDUType type;
		
		public long getLengthInS() {
			return lengthInS;
		}

		public void setLengthInS(long lengthInS) {
			this.lengthInS = lengthInS;
		}

		public PDUType getType() {
			return type;
		}

		public void setType(PDUType type) {
			this.type = type;
		}
	}
	
	private Map<String, PDUTypeSettings> config;

	public Map<String, PDUTypeSettings> getConfig() {
		return config;
	}

	public void setPdu(Map<String, PDUTypeSettings> config) {
		this.config = config;
	}
}
