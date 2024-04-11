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

package esa.s1pdgs.cpoc.mqi.model.queue;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Exchanged object for the product category LevelReports.
 * 
 * @author Viveris Technologies
 */
public class LevelReportDto extends AbstractMessage {
	private String content;

	/**
	 * Default constructor
	 */
	public LevelReportDto() {
		super();
	}

	/**
	 * @param productName
	 * @param content
	 */
	public LevelReportDto(final String productName, final String content, final ProductFamily family) {
		super(family, productName);
		this.content = content;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(final String content) {
		this.content = content;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LevelReportDto other = (LevelReportDto) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LevelReportDto [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", storagePath=" + storagePath + ", creationDate=" + creationDate + ", podName=" + podName
				+ ", content=" + content + ", uid=" + uid + ", rsChainVersion=" + rsChainVersion + "]";
	}
}
