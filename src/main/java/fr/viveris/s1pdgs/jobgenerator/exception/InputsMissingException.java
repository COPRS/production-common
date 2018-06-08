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

	/**
	 * Missing metadata:
	 * <li>key = input description</li>
	 * <li>value = "" or reason</li>
	 */
	private final Map<String, String> missingMetadata;

	/**
	 * Constructor
	 * @param missingData
	 */
	public InputsMissingException(final Map<String, String> missingData) {
		super(ErrorCode.MISSING_INPUT, "Missing inputs");
		this.missingMetadata = new HashMap<>();
		if (!CollectionUtils.isEmpty(missingData)) {
			missingData.forEach((k,v) -> {
				this.missingMetadata.put(k, v);
			});
		}
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		String ret = "";
		if (!CollectionUtils.isEmpty(missingMetadata)) {
			for (String input : this.missingMetadata.keySet()) {
				String reason = this.missingMetadata.get(input);
				if (StringUtils.isEmpty(reason)) {
					ret = ret + "[input " + input + "]";
				} else {
					ret = ret + "[input " + input + "] [reason " + reason + "]";
				}
			}
		}
		ret = ret + "[msg " + getMessage() + "]";
		return ret;
	}

	/**
	 * @return the missingMetadata
	 */
	public Map<String, String> getMissingMetadata() {
		return missingMetadata;
	}
	
}
