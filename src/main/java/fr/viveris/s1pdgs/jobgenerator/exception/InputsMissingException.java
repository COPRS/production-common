package fr.viveris.s1pdgs.jobgenerator.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Generic exception concerning the metadata
 * 
 * @author Cyrielle Gailliard
 *
 */
public class InputsMissingException extends AbstractCodedException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 6588566901653710376L;

	private Map<String, String> missingMetadata;

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public InputsMissingException(Map<String, String> missingData) {
		super(ErrorCode.MISSING_INPUT, "Missing inputs");
		this.missingMetadata = new HashMap<>();
		if (!CollectionUtils.isEmpty(missingData)) {
			missingData.forEach((k,v) -> {
				this.missingMetadata.put(k, v);
			});
		}
	}

	@Override
	public String getLogMessage() {
		String r = "";
		if (!CollectionUtils.isEmpty(missingMetadata)) {
			for (String input : this.missingMetadata.keySet()) {
				String reason = this.missingMetadata.get(input);
				if (StringUtils.isEmpty(reason)) {
					r += "[input " + input + "]";
				} else {
					r += "[input " + input + "] [reason " + reason + "]";
				}
			}
		}
		r += "[msg " + getMessage() + "]";
		return r;
	}

	/**
	 * @return the missingMetadata
	 */
	public Map<String, String> getMissingMetadata() {
		return missingMetadata;
	}
	
}
