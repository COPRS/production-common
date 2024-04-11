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

package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * 
 */
public class MetadataIgnoredFileException extends AbstractCodedException {

	private static final long serialVersionUID = 6432844848252714971L;

	/**
	 * 
	 */
	private static final String MESSAGE = "File/folder %s will be ignored";

	/**
	 * 
	 * @param productName
	 * @param ignoredName
	 */
	public MetadataIgnoredFileException(final String ignoredName) {
		super(ErrorCode.METADATA_IGNORE_FILE, String.format(MESSAGE, ignoredName));
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}
}
