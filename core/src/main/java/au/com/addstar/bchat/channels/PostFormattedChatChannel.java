package au.com.addstar.bchat.channels;

import com.google.common.base.Optional;

import net.cubespace.geSuit.core.GlobalPlayer;

/**
 * Represents a channel that is formatted immediately before being sent to a player.
 */
public abstract class PostFormattedChatChannel extends FormattedChatChannel {
	PostFormattedChatChannel(String name, Optional<String> listenPermission, String format, ChatChannelManager manager) {
		super(name, listenPermission, format, manager);
	}
	
	PostFormattedChatChannel(String name, ChatChannelManager manager) {
		super(name, manager);
	}

	/**
	 * Gets the format that will be shown to {@code listener}
	 * @param sender The sender of this message
	 * @param listener The player that is receiving this. May be null
	 * @return The format
	 */
	public abstract String getFormat(GlobalPlayer sender, GlobalPlayer listener);
}
