package esa.s1pdgs.cpoc.ebip.client;

@FunctionalInterface
public interface EdipEntryFilter {
	public static final EdipEntryFilter ALLOW_ALL 	= x -> true;
	public static final EdipEntryFilter ALLOW_NONE 	= x -> false;
	
	boolean accept(EdipEntry entry);
}
