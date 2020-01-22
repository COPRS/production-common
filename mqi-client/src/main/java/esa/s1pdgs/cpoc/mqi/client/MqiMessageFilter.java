package esa.s1pdgs.cpoc.mqi.client;

import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public class MqiMessageFilter implements MessageFilter {

	private ProductFamily productFamily;
	private String matchRegex;

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(final ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public String getMatchRegex() {
		return matchRegex;
	}

	public void setMatchRegex(final String matchRegex) {
		this.matchRegex = matchRegex;
	}

	@Override
	public final boolean accept(final AbstractMessage message) {
		// evaluate only if the filter is specified for the given family
		if (productFamily == message.getProductFamily()) {
			return Pattern.matches(matchRegex, message.getKeyObjectStorage());
		}		
		return true;
	}
}
