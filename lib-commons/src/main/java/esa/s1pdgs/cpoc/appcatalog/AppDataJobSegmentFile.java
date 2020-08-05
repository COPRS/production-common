package esa.s1pdgs.cpoc.appcatalog;

import java.util.Objects;

import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;

public class AppDataJobSegmentFile extends AppDataJobFile {
	private LevelSegmentMetadata metadata = LevelSegmentMetadata.NULL;

	public AppDataJobSegmentFile() {
		super();
	}

	public AppDataJobSegmentFile(
			final String fileName, 
			final String keyObs, 
			final String startDate, 
			final String endDate,
			final LevelSegmentMetadata metadata
	) {
		super(fileName, keyObs, startDate, endDate);
		this.metadata = metadata;
	}

	public LevelSegmentMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(final LevelSegmentMetadata metadata) {
		this.metadata = metadata;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(metadata);
	}

	@Override
	public boolean equals(final Object obj) {
		if (super.equals(obj) && (obj instanceof AppDataJobSegmentFile))
		{
			final AppDataJobSegmentFile other = (AppDataJobSegmentFile) obj;			
			return Objects.equals(metadata, other.metadata);
		}
		return false;
	}

    @Override
    public String toString() {
        return String.format("{fileName: %s, keyObs: %s, startDate: %s, endDate, %s, metadata: %s}", fileName, keyObs, startDate, endDate, metadata);
    }
	
	
}
