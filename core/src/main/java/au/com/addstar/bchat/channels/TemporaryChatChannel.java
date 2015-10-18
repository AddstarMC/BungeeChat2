package au.com.addstar.bchat.channels;

import com.google.common.base.Optional;

public class TemporaryChatChannel extends FormattedChatChannel {
	public TemporaryChatChannel(String name, Optional<String> listenPermission, String format) {
		super(name, listenPermission, format);
	}
}
