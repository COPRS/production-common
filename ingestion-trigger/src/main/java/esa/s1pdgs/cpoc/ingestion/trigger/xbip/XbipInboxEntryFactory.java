package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

@Component
public class XbipInboxEntryFactory implements InboxEntryFactory {	
	public static final Pattern SESSION_PATTERN = Pattern.compile("^([a-z_]{4}/)?"
			+ "([0-9a-z_]{2})([0-9a-z_]{1})/([0-9a-z_]+)/(ch[0|_]?[1-2]/)?"
			+ "(DCS_[0-9]{2}_([a-zA-Z0-9_]*)_ch([12])_(DSDB|DSIB).*\\.(raw|aisp|xml))$", 
			Pattern.CASE_INSENSITIVE
	);	

	@Override
	public InboxEntry newInboxEntry(final URI inboxURL, final Path path, final int productAt, final Date lastModified, final long size) {
		final InboxEntry inboxEntry = new InboxEntry();
		final Path relativePath = Paths.get(inboxURL.getPath()).relativize(path);
		inboxEntry.setName(productName(relativePath.toString()));
		inboxEntry.setRelativePath(relativePath.toString());
		inboxEntry.setPickupURL(inboxURL.toString());
		inboxEntry.setLastModified(lastModified);
		inboxEntry.setSize(size);
		return inboxEntry;
	}
	
	private final String productName(final String relativePath) {		
		// FIXME ok, this is a pretty dirty workaround here:
		// since we don't know the directory depth itself as it may vary, we can only heuristically try 
		// to determine the session name. This is also based on the assumption that xbip is only used with session files	
		final Matcher matchi = SESSION_PATTERN.matcher(relativePath);
		
		// since this is so dirty here, we wanna fail early if something goes wrong so we simply throw and excepion here		
		if (!matchi.matches()) {
			throw new IllegalArgumentException(String.format("Could not detect sessionName for %s", relativePath));
		}		
		final String sessionName = matchi.group(4);		
		return relativePath.substring(relativePath.indexOf(sessionName));
	}
}
