package fr.viveris.s1pdgs.jobgenerator.model;

import java.util.Objects;

/**
 * Class describing an EDRS session
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSession {

	/**
	 * Channel identifier 1
	 */
	private static final int CH_ID_1 = 1;

	/**
	 * Channel identifier 2
	 */
	private static final int CH_ID_2 = 2;

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
	private long lastTsMsg;

	/**
	 * Default constructor
	 */
	public EdrsSession() {
		lastTsMsg = System.currentTimeMillis();
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
	public void setChannel1(final EdrsSessionFile channel1) {
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
	public void setChannel2(final EdrsSessionFile channel2) {
		this.channel2 = channel2;
	}

	/**
	 * Set channel in right attribute according the channel identifier
	 * 
	 * @param channel
	 * @param channelId
	 */
	public void setChannel(final EdrsSessionFile channel, final int channelId) {
		if (channelId == CH_ID_1) {
			this.setChannel1(channel);
		} else if (channelId == CH_ID_2) {
			this.setChannel2(channel);
		}
	}

	/**
	 * @return the lastTsMsg
	 */
	public long getLastTsMsg() {
		return lastTsMsg;
	}

	/**
	 * @param lastTsMsg
	 *            the lastTsMsg to set
	 */
	public void setLastTsMsg(final long lastTsMsg) {
		this.lastTsMsg = lastTsMsg;
	}

	/**
	 * to string
	 */
	@Override
	public String toString() {
		return String.format("{channel1: %s, channel2: %s, lastTsMsg: %s}", channel1, channel2,
				lastTsMsg);
	}

	/**
	 * hashcode
	 */
	@Override
	public int hashCode() {
		return Objects.hash(channel1, channel2, lastTsMsg);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			EdrsSession other = (EdrsSession) obj;
			ret = Objects.equals(channel1, other.channel1) && Objects.equals(channel2, other.channel2)
					&& lastTsMsg == other.lastTsMsg;
		}
		return ret;
	}

}
