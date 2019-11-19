package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import java.nio.file.Path;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.InboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxPathInformation;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

@Component
public class FilesystemInboxEntryFactory implements InboxEntryFactory {

	@Override
	public InboxEntry newInboxEntry(InboxPathInformation inboxPathInformation, Path entryRelativePath,
			Path inboxDirectoryPath) {

		InboxEntry inboxEntry = new InboxEntry();
		inboxEntry.setName(entryRelativePath.toFile().getName());
		inboxEntry.setRelativePath(entryRelativePath.toString());
		inboxEntry.setPickupPath(inboxDirectoryPath.toString());
		inboxEntry.setUrl(inboxDirectoryPath.resolve(entryRelativePath).toString());
		inboxEntry.setMissionId(inboxPathInformation.getMissionId());
		inboxEntry.setSatelliteId(inboxPathInformation.getSatelliteId());
		inboxEntry.setStationCode(inboxPathInformation.getStationCode());

		return inboxEntry;
	}
}
