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

package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.util.Collections;
import java.util.List;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

public class IngestionResult {
	public static final IngestionResult NULL = new IngestionResult(Collections.emptyList(), 0L);
	
	private final List<Product<CatalogJob>> ingestedProducts;
	private final long transferAmount;
	
	public IngestionResult(final List<Product<CatalogJob>> ingestedProducts, final long transferAmount) {
		this.ingestedProducts = ingestedProducts;
		this.transferAmount = transferAmount;
	}

	public List<Product<CatalogJob>> getIngestedProducts() {
		return ingestedProducts;
	}

	public long getTransferAmount() {
		return transferAmount;
	}
	
	@Override
	public String toString() {
		return String.format("transferAmount: %s, ingestedProducts: %s", transferAmount, ingestedProducts);
	}
}
