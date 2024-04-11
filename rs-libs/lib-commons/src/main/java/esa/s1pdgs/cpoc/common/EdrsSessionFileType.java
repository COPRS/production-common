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

import java.io.File;

/**
 * Enumeration for ERDS session file type
 * @author Viveris Technologies
 *
 */
public enum EdrsSessionFileType {
	RAW, SESSION;
	
	/**
	 * Determinate value from an extension
	 * @param extension
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static EdrsSessionFileType valueFromExtension(final FileExtension extension) throws IllegalArgumentException {
		EdrsSessionFileType ret;
		switch (extension) {
		case XML:
			ret = SESSION;
			break;
		case RAW:
		case AISP:
			ret = RAW;
			break;
		default:
			throw new IllegalArgumentException("Cannot retrieve ERDS session file type from extension " + extension);
		}
		return ret;
	}
	
	public static EdrsSessionFileType ofFilename(final String filename) throws IllegalArgumentException {
		final String name = new File(filename).getName();		
		final String suffix = name.substring(name.lastIndexOf('.') + 1);
		
		return valueFromExtension(FileExtension.valueOfIgnoreCase(suffix));		
	}
}
