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

package esa.s1pdgs.cpoc.common.errors.obs;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Exception concerning the object storage
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ObsUnknownObjectException extends ObsException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -3680895691846942569L;

	/**
	 * 
	 * @param key
	 * @param bucket
	 * @param message
	 */
	public ObsUnknownObjectException(final ProductFamily family, final String key) {
		super(ErrorCode.OBS_UNKOWN_OBJ, family, key, "Object not found");
	}

}
