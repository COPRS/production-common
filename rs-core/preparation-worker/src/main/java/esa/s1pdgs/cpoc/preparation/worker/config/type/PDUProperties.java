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

package esa.s1pdgs.cpoc.preparation.worker.config.type;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.preparation.worker.model.pdu.PDUReferencePoint;
import esa.s1pdgs.cpoc.preparation.worker.model.pdu.PDUType;

/**
 * Additional settings used to configure the PDU type adapter
 * 
 * @author Julian Kaping
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "pdu")
public class PDUProperties {

	public static class PDUTypeProperties {

		/**
		 * Length of frames or stripes
		 */
		private double lengthInS;

		/**
		 * Offset for stripes of reference ORBIT
		 */
		private double offsetInS = 0.0;
		
		/**
		 * Minimum length threshold for PDUs
		 */
		private double minPDULengthThreshold = 0.0;

		/**
		 * Reference point for stripes (dump start [DUMP] or anx time [ORBIT])
		 */
		private PDUReferencePoint reference = PDUReferencePoint.ORBIT;

		/**
		 * Type of PDUs that should be generated
		 */
		private PDUType type = PDUType.FRAME;

		/**
		 * Threshold for the gap handler to determine if two intervals should be
		 * handled as continuous
		 */
		private double gapThreshholdInS = 0.0;
		
		/**
		 * map for dynamic process parameters which are not part of the metadata (ex.
		 * facilityName)
		 */
		private Map<String, String> dynProcParams = new HashMap<>();

		public double getLengthInS() {
			return lengthInS;
		}

		public void setLengthInS(double lengthInS) {
			if (lengthInS <= 0.0) {
				throw new IllegalArgumentException("lengthInS has to be greater than 0");
			}
			this.lengthInS = lengthInS;
		}

		public double getOffsetInS() {
			return offsetInS;
		}

		public void setOffsetInS(double offsetInS) {
			this.offsetInS = offsetInS;
		}

		public PDUReferencePoint getReference() {
			return reference;
		}

		public void setReference(PDUReferencePoint reference) {
			this.reference = reference;
		}

		public PDUType getType() {
			return type;
		}

		public void setType(PDUType type) {
			this.type = type;
		}
		
		public double getGapThreshholdInS() {
			return gapThreshholdInS;
		}

		public void setGapThreshholdInS(double gapThreshholdInS) {
			this.gapThreshholdInS = gapThreshholdInS;
		}

		public Map<String, String> getDynProcParams() {
			return dynProcParams;
		}

		public void setDynProcParams(Map<String, String> dynProcParams) {
			this.dynProcParams = dynProcParams;
		}

		public double getMinPDULengthThreshold() {
			return minPDULengthThreshold;
		}

		public void setMinPDULengthThreshold(double minPDULengthThreshold) {
			this.minPDULengthThreshold = minPDULengthThreshold;
		}
	}

	private Map<String, PDUTypeProperties> config;

	public Map<String, PDUTypeProperties> getConfig() {
		return config;
	}

	public void setConfig(Map<String, PDUTypeProperties> config) {
		this.config = config;
	}
}
