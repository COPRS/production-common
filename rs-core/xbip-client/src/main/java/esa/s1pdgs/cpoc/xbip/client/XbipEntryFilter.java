package esa.s1pdgs.cpoc.xbip.client;

@FunctionalInterface
public interface XbipEntryFilter {
	public static final XbipEntryFilter ALLOW_ALL 	= x -> true;
	public static final XbipEntryFilter ALLOW_NONE 	= x -> false;
	
	boolean accept(XbipEntry entry);
}
