package fr.viveris.s1pdgs.jobgenerator.model;

/**
 * Status of a job generation
 * @author Cyrielle 
 *
 */
public enum GenerationStatusEnum {
	/**
	 * 
	 */
	NOT_READY,
	
	/**
	 * The first verifications (specific to the level) are right
	 */
	PRIMARY_CHECK,
	
	/**
	 * The second verifications (input search and presence) are right
	 */
	READY
}
