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

package esa.s1pdgs.cpoc.metadata.client;

public enum MetadataCatalogRestPath {

	EDRS_SESSION("edrsSession"), METADATA("metadata"), L0_SLICE("l0Slice"), L0_ACN("l0Acn"), L1_SLICE("l1Slice"), L1_ACN("l1Acn"), LEVEL_SEGMENT("level_segment"),
	S3_METADATA("s3metadata");

	private final String path;

	private MetadataCatalogRestPath(String path) {
		this.path = path;
	}

	public String path() {
		return this.path;
	}
}
