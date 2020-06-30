package esa.s1pdgs.cpoc.mqi.client;

import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public class MqiMessageFilter implements MessageFilter {

	private static final Logger LOG = LogManager.getLogger(MqiMessageFilter.class);

	private ProductFamily productFamily;
	private String allowRegex;
	private String disallowRegex;

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(final ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public String getAllowRegex() {
		return allowRegex;
	}

	public void setAllowRegex(final String allowRegex) {
		this.allowRegex = allowRegex;
	}

	public String getDisallowRegex() {
		return disallowRegex;
	}

	public void setDisallowRegex(final String disallowRegex) {
		this.disallowRegex = disallowRegex;
	}

	@Override
	public final boolean accept(final AbstractMessage message) {

		if (productFamily == null) {
			LOG.warn("productFamily not set, filter is disabled");
			return true;
		}

		if (productFamily != message.getProductFamily()) {
			return true;
		}

		if (allowRegex == null && disallowRegex == null) {
			LOG.warn("allowRegex and disallowRegex not set, filter is disabled");
			return true;
		}

		if (allowRegex == null) {
			if (Pattern.matches(disallowRegex, message.getKeyObjectStorage())) {
				return false;
			} else {
				return true;
			}
		}

		if (disallowRegex == null) {
			if (Pattern.matches(allowRegex, message.getKeyObjectStorage())) {
				return true;
			} else {
				return false;
			}
		}

		if (Pattern.matches(allowRegex, message.getKeyObjectStorage())
				&& !Pattern.matches(disallowRegex, message.getKeyObjectStorage())) {
			return true;
		} else {
			return false;
		}
	}
}
