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

package esa.s1pdgs.cpoc.xml.model.joborder;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * 
 *
 */
@XmlRootElement(name = "File_Name")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrderInputFile {

	/**
	 * Filename
	 */
	@XmlValue
	private String filename;

	/**
	 * KEy in object storage
	 */
	private String keyObjectStorage;

	/**
	 * Default constructor
	 */
	public JobOrderInputFile() {
		super();
	}

	/**
	 * @param filename
	 * @param keyObjectStorage
	 */
	public JobOrderInputFile(final String filename, final String keyObjectStorage) {
		this();
		this.filename = filename;
		this.keyObjectStorage = keyObjectStorage;
	}

	/**
	 * @param filename
	 * @param keyObjectStorage
	 */
	public JobOrderInputFile(final JobOrderInputFile obj) {
		this(obj.getFilename(), obj.getKeyObjectStorage());
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public void setFilename(final String filename) {
		this.filename = filename;
	}

	/**
	 * @return the keyObjectStorage
	 */
	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	/**
	 * @param keyObjectStorage
	 *            the keyObjectStorage to set
	 */
	public void setKeyObjectStorage(final String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{filename: %s, keyObjectStorage: %s}", filename, keyObjectStorage);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(filename, keyObjectStorage);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			JobOrderInputFile other = (JobOrderInputFile) obj;
			ret = Objects.equals(filename, other.filename) && Objects.equals(keyObjectStorage, other.keyObjectStorage);
		}
		return ret;
	}

}
