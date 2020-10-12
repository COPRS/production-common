package esa.s1pdgs.cpoc.auxip.client;

@FunctionalInterface
public interface AuxipProductFilter {
	public static final AuxipProductFilter ALLOW_ALL 	= x -> true;
	public static final AuxipProductFilter ALLOW_NONE 	= x -> false;
	
	boolean accept(AuxipProductMetadata entry);
}
