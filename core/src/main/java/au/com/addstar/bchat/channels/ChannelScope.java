package au.com.addstar.bchat.channels;

/**
 * Represents the scope of a channel.
 * This influences who can hear a channel
 */
public enum ChannelScope {
	/**
	 * Everywhere can hear this channel
	 */
	GLOBAL,
	/**
	 * Only the source server can hear this channel
	 */
	SERVER,
	/**
	 * Only the source world can hear this channel
	 */
	WORLD
}
