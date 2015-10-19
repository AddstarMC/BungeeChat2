package au.com.addstar.bchat.channels;

import com.google.common.base.Optional;

public class FormattedChatChannel extends ChatChannel {
	private String format;
	
	public FormattedChatChannel(String name, Optional<String> listenPermission) {
		super(name, listenPermission);
	}
	
	public FormattedChatChannel(String name, Optional<String> listenPermission, String format) {
		super(name, listenPermission);
		
		this.format = format;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
}
