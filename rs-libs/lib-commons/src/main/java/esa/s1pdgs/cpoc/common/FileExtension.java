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

package esa.s1pdgs.cpoc.common;

import java.util.Locale;

/**
 * Enumeration for file extension
 *
 */
public enum FileExtension {
	XML, SAFE, EOF, RAW, XSD, DAT, AISP, ISIP, SEN3, UNKNOWN;

	/**
	 * Determinate value from an extension
	 * 
	 * @param extension
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static FileExtension valueOfIgnoreCase(final String extension) throws IllegalArgumentException {
		FileExtension ret;
		try {
			String extensionUC = extension.toUpperCase(Locale.getDefault());
			ret = valueOf(extensionUC);
		} catch (IllegalArgumentException e) {
			ret = UNKNOWN;
		}
		return ret;
	}
}
