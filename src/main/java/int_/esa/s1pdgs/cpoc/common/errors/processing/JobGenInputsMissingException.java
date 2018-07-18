package int_.esa.s1pdgs.cpoc.common.errors.processing;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import int_.esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * Generic exception concerning the metadata
 * 
 * @author Viveris Technologies
 */
public class JobGenInputsMissingException extends AbstractCodedException {

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
     * 
     * @param missingData
     */
    public JobGenInputsMissingException(final Map<String, String> missingData) {
        super(ErrorCode.MISSING_INPUT, "Missing inputs");
        this.missingMetadata = new HashMap<>();
        if (!CollectionUtils.isEmpty(missingData)) {
            missingData.forEach((k, v) -> {
                this.missingMetadata.put(k, v);
            });
        }
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        StringBuffer buffer = new StringBuffer("");
        if (!CollectionUtils.isEmpty(missingMetadata)) {
            for (String input : this.missingMetadata.keySet()) {
                String reason = this.missingMetadata.get(input);
                if (StringUtils.isEmpty(reason)) {
                    buffer = buffer.append("[input " + input + "]");
                } else {
                    buffer = buffer.append(
                            "[input " + input + "] [reason " + reason + "]");
                }
            }
        }
        buffer.append("[msg " + getMessage() + "]");
        return buffer.toString();
    }

    /**
     * @return the missingMetadata
     */
    public Map<String, String> getMissingMetadata() {
        return missingMetadata;
    }

}
