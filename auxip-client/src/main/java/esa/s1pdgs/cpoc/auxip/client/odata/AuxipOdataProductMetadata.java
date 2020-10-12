package esa.s1pdgs.cpoc.auxip.client.odata;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import esa.s1pdgs.cpoc.auxip.client.AuxipProductMetadata;

public class AuxipOdataProductMetadata implements AuxipProductMetadata {
	
	private UUID id;
	private String productName;
	private LocalDateTime creationDate;
	
	private final List<String> parsingErrors = new ArrayList<>();
	
	// --------------------------------------------------------------------------
	
	@Override
	public UUID getId() {
		return this.id;
	}

	@Override
	public String getProductName() {
		return this.productName;
	}

	@Override
	public LocalDateTime getCreationDate() {
		return this.creationDate;
	}
	
	@Override
	public List<String> getParsingErrors() {
		return this.parsingErrors;
	}

	// --------------------------------------------------------------------------

	public void setId(UUID id) {
		this.id = id;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public void addParsingError(String parsingError) {
		this.parsingErrors.add(parsingError);
	}

}
