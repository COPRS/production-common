package esa.s1pdgs.cpoc.ipf.preparation.worker.model.pdu;

/**
 * Type of PDU Generation
 * 
 * Frames are numbered, and orbit based. The combination of relativeOrbitNumber
 * and FrameNumber should therefore always result in the same region.
 * 
 * Stripes can either be dump or orbit based and aren't numbered. Most of the
 * time they are used to combine multiple products into one bigger product.
 * 
 * Tiles are defined regions on the planet that have an unique identifier (ex.
 * EUROPE)
 * 
 * @author Julian Kaping
 *
 */
public enum PDUType {
	FRAME, STRIPE, TILE;
}
