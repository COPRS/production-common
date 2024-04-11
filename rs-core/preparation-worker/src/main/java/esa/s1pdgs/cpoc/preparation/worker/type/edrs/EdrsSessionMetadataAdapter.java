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

package esa.s1pdgs.cpoc.preparation.worker.type.edrs;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;

public class EdrsSessionMetadataAdapter {
	private static final EdrsSessionMetadata NULL = new EdrsSessionMetadata() {
		@Override
		public String getKeyObjectStorage() {
			return null;
		}		
	};
	
	private final EdrsSessionMetadata channel1;
	private final EdrsSessionMetadata channel2;
	private final Map<String,EdrsSessionMetadata> raws1;
	private final Map<String,EdrsSessionMetadata> raws2;

	public EdrsSessionMetadataAdapter(
			final EdrsSessionMetadata channel1, 
			final EdrsSessionMetadata channel2,
			final Map<String,EdrsSessionMetadata> raws1, 
			final Map<String,EdrsSessionMetadata> raws2
	) {
		this.channel1 = channel1;
		this.channel2 = channel2;
		this.raws1 = raws1;
		this.raws2 = raws2;
	}

	public static final EdrsSessionMetadataAdapter parse(final List<EdrsSessionMetadata> metadata) {
		EdrsSessionMetadata channel1 = null;
		EdrsSessionMetadata channel2 = null;
		final Map<String,EdrsSessionMetadata> raws1 = new TreeMap<>();
		final Map<String,EdrsSessionMetadata> raws2 = new TreeMap<>();
		
		for (final EdrsSessionMetadata met : metadata) {
			if (met.getProductType().equals(EdrsSessionFileType.RAW.name())) {
				if (met.getChannelId() == 1) {
					raws1.put(met.getProductName(), met);
				}
				else {
					raws2.put(met.getProductName(), met);
				}
			} 
			else if (met.getProductType().equals(EdrsSessionFileType.SESSION.name())) {
				if (met.getChannelId() == 1) {
					channel1 = met;
				}
				else {
					channel2 = met;
				}
			}
		}
		// FIXME add assertions & error handling
		return new EdrsSessionMetadataAdapter(channel1, channel2, raws1, raws2);		
	}
	
	public final EdrsSessionMetadata getChannel1() {
		return channel1;
	}
	public final EdrsSessionMetadata getChannel2() {
		return channel2;
	}
		
	public final List<AppDataJobFile> availableRaws1() {
		return raws(raws1.keySet(), raws1);
	}
	
	public final List<AppDataJobFile> availableRaws2() {
		return raws(raws2.keySet(), raws2);
	}	
	
	public final List<AppDataJobFile> raws1() {
		return raws(channel1.getRawNames(), raws1);
	}
	
	public final List<AppDataJobFile> raws2() {
		return raws(channel2.getRawNames(), raws2);
	}	
	
	private final List<AppDataJobFile> raws(final Collection<String> names, final Map<String,EdrsSessionMetadata> raws) {
    	return names.stream()
    			.map(s -> 
    			{ 
    				// Save t0PdgsDate for further computations on AppDataJobFile
    				Date metT0 = null;
    				String t0PdgsDate = raws.getOrDefault(s, NULL).getAdditionalProperties().get("t0PdgsDate");
    				if (t0PdgsDate != null) {
    					metT0 = DateUtils.toDate(t0PdgsDate);
    				}
    				
    				return new AppDataJobFile(s, raws.getOrDefault(s, NULL).getKeyObjectStorage(), metT0);
    			})
                .collect(Collectors.toList());
    }
}
