package esa.s1pdgs.cpoc.inbox.fs;

import java.nio.file.Path;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.inbox.config.InboxPathInformation;
import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;

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
