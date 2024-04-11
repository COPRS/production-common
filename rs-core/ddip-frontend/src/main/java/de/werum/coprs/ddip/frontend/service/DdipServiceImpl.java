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

package de.werum.coprs.ddip.frontend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.werum.coprs.ddip.frontend.config.DdipProperties;

@Service
public class DdipServiceImpl implements DdipService {

	private final DdipProperties ddipProperties;

	@Autowired
	public DdipServiceImpl(final DdipProperties ddipProperties) {

		this.ddipProperties = ddipProperties;
	}

	@Override
	public String getDdipVersion() {
		return this.ddipProperties.getVersion();
	}

}
