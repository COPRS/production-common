package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.util.Collections;
import java.util.List;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public class IngestionResult {
	public static final IngestionResult NULL = new IngestionResult(Collections.emptyList(), 0L);
	
	private final List<Product<AbstractMessage>> ingestedProducts;
	private final long transferAmount;
	
	public IngestionResult(List<Product<AbstractMessage>> ingestedProducts, long transferAmount) {
		this.ingestedProducts = ingestedProducts;
		this.transferAmount = transferAmount;
	}

	public List<Product<AbstractMessage>> getIngestedProducts() {
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
