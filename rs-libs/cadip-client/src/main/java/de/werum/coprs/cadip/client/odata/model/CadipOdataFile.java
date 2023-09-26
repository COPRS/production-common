package de.werum.coprs.cadip.client.odata.model;

import java.time.LocalDateTime;
import java.util.UUID;

import de.werum.coprs.cadip.client.model.CadipFile;

public class CadipOdataFile implements CadipFile {

	public static final String ENTITY_SET_NAME = "Files";
	
	public static final String ID_ATTRIBUTE = "Id";
	public static final String NAME_ATTRIBUTE = "Name";
	public static final String SESSION_ID_ATTRIBUTE = "SessionId";
	public static final String CHANNEL_ATTRIBUTE = "Channel";
	public static final String BLOCK_NUMBER_ATTRIBUTE = "BlockNumber";
	public static final String FINAL_BLOCK_ATTRIBUTE = "FinalBlock";
	public static final String PUBLICATION_DATE_ATTRIBUTE = "PublicationDate";
	public static final String EVICTION_DATE_ATTRIBUTE = "EvictionDate";
	public static final String SIZE_ATTRIBUTE = "Size";
	public static final String RETRANSFER_ATTRIBUTE = "Retransfer";
	
	private UUID id;
	private String name;
	private String sessionId;
	private Long channel;
	private Long blockNumber;
	private Boolean finalBlock;
	private LocalDateTime publicationDate;
	private LocalDateTime evictionDate;
	private Long size;
	private Boolean retransfer;

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public Long getChannel() {
		return channel;
	}

	@Override
	public Long getBlockNumber() {
		return blockNumber;
	}

	@Override
	public Boolean getFinalBlock() {
		return finalBlock;
	}

	@Override
	public LocalDateTime getPublicationDate() {
		return publicationDate;
	}

	@Override
	public LocalDateTime getEvictionDate() {
		return evictionDate;
	}

	@Override
	public Long getSize() {
		return size;
	}

	@Override
	public Boolean getRetransfer() {
		return retransfer;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setChannel(Long channel) {
		this.channel = channel;
	}

	public void setBlockNumber(Long blockNumber) {
		this.blockNumber = blockNumber;
	}

	public void setFinalBlock(Boolean finalBlock) {
		this.finalBlock = finalBlock;
	}

	public void setPublicationDate(LocalDateTime publicationDate) {
		this.publicationDate = publicationDate;
	}

	public void setEvictionDate(LocalDateTime evictionDate) {
		this.evictionDate = evictionDate;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public void setRetransfer(Boolean retransfer) {
		this.retransfer = retransfer;
	}
	
	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder("CadipOdataFile [");
		sb.append("id=").append(id).append(", ");
		sb.append("name=").append(name).append(", ");
		sb.append("sessionId=").append(sessionId).append(", ");
		sb.append("channel=").append(channel).append(", ");
		sb.append("blockNumber=").append(blockNumber).append(", ");
		sb.append("finalBlock=").append(finalBlock).append(", ");
		sb.append("publicationDate=").append(publicationDate).append(", ");
		sb.append("evictionDate=").append(evictionDate).append(", ");
		sb.append("size=").append(size).append(", ");
		sb.append("retransfer=").append(retransfer);
		sb.append("]");
		
		return sb.toString();
	}
}
