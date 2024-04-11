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

package esa.s1pdgs.cpoc.metadata.extraction;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

public class Utils {

	public static final CatalogJob newCatalogJob(final String name, final String keyObs, final ProductFamily family,
			final String mode) {
		return newCatalogJob(name, keyObs, family, mode, null);
	}

	public static final CatalogJob newCatalogJob(final String name, final String keyObs, final ProductFamily family) {
		return newCatalogJob(name, keyObs, family, null);
	}

	public static final CatalogJob newCatalogJob(final String name, final String keyObs, final ProductFamily family,
			final String mode, final String relativePath) {
		final CatalogJob job = new CatalogJob();
		job.setMetadataProductName(name);
		job.setKeyObjectStorage(keyObs);
		job.setProductFamily(family);
		job.setMetadataMode(mode);
		job.setMetadataRelativePath(relativePath);
		return job;
	}

	public static final void copyFolder(final Path src, final Path dest) throws Exception {
		Files.walk(src).forEach(source -> copy(source, dest.resolve(src.relativize(source))));
	}

	private static final void copy(final Path source, final Path dest) {
		try {
			Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
