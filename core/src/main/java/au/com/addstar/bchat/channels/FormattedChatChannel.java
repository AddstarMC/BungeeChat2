package au.com.addstar.bchat.channels;

import java.util.Map;

import com.google.common.base.Optional;

public class FormattedChatChannel extends ChatChannel {
	private String format;
	
	FormattedChatChannel(String name, ChatChannelManager manager) {
		super(name, manager);
		
		format = ""; // TODO: Default format
	}
	
	FormattedChatChannel(String name, Optional<String> listenPermission, String format, ChatChannelManager manager) {
		super(name, listenPermission, manager);
		
		this.format = format;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	@Override
	public void save(Map<String, String> values) {
		super.save(values);
		
		values.put("format", format);
		values.put("type", "format");
	}
	
	@Override
	public void load(Map<String, String> values) {
		super.load(values);
		
		format = values.get("format");
	}
}
