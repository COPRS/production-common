package fr.viveris.s1pdgs.jobgenerator.model;

/**
 * Class describing an EDRS session
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSession {

	/**
	 * Raws names of channel 1
	 */
	private EdrsSessionFile channel1;

	/**
	 * Raws names of channel 2
	 */
	private EdrsSessionFile channel2;

	/**
	 * Date of last message consumption
	 */
	private long lastTimestampMessageReception;

	/**
	 * Default constructor
	 */
	public EdrsSession() {
		lastTimestampMessageReception = System.currentTimeMillis();
	}

	/**
	 * @return the rawsChannel1
	 */
	public EdrsSessionFile getChannel1() {
		return channel1;
	}

	/**
	 * @param rawsChannel1
	 *            the rawsChannel1 to set
	 */
	public void setChannel1(EdrsSessionFile channel1) {
		this.channel1 = channel1;
	}

	/**
	 * @return the rawsChannel2
	 */
	public EdrsSessionFile getChannel2() {
		return channel2;
	}

	/**
	 * @param rawsChannel2
	 *            the rawsChannel2 to set
	 */
	public void setChannel2(EdrsSessionFile channel2) {
		this.channel2 = channel2;
	}

	public void setChannel(EdrsSessionFile channel, int channelId) {
		switch (channelId) {
		case 1:
			this.setChannel1(channel);
			break;
		case 2:
			this.setChannel2(channel);
			break;
		}
	}

	/**
	 * @return the lastTimestampMessageReception
	 */
	public long getLastTimestampMessageReception() {
		return lastTimestampMessageReception;
	}

	/**
	 * @param lastTimestampMessageReception
	 *            the lastTimestampMessageReception to set
	 */
	public void setLastTimestampMessageReception(long lastTimestampMessageReception) {
		this.lastTimestampMessageReception = lastTimestampMessageReception;
	}

}
