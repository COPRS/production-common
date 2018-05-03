package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Exception concerning the object storage
 * @author Cyrielle Gailliard
 *
 */
public class ObsUnknownObjectException extends AbstractCodedException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -3680895691846942569L;
	
	private String key;
	
	private String bucket;

	public ObsUnknownObjectException(String key, String bucket, String message) {
		super(ErrorCode.OBS_UNKOWN_OBJ, message);
		this.key = key;
		this.bucket = bucket;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the bucket
	 */
	public String getBucket() {
		return bucket;
	}

	public String getLogMessage() {
		return String.format("[bucket %s] [key %s] [msg %s]", this.bucket, this.key, getMessage());
	}

}
