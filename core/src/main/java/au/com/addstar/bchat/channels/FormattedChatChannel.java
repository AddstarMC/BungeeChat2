package au.com.addstar.bchat.channels;

import java.util.Map;

import com.google.common.base.Optional;

public class FormattedChatChannel extends ChatChannel {
	private String format;
	private boolean useHighlighter;
	
	FormattedChatChannel(String name, ChatChannelManager manager) {
		super(name, manager);
		
		format = ""; // TODO: Default format
		useHighlighter = false;
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
	
	public boolean getUseHighlighter() {
		return useHighlighter;
	}
	
	public void setUseHighlighter(boolean useHighlighter) {
		this.useHighlighter = useHighlighter;
	}
	
	@Override
	public void save(Map<String, String> values) {
		super.save(values);
		
		values.put("format", format);
		values.put("highlight", String.valueOf(useHighlighter));
		values.put("type", "format");
	}
	
	@Override
	public void load(Map<String, String> values) {
		super.load(values);
		
		format = values.get("format");
		useHighlighter = Boolean.parseBoolean(values.get("highlight"));
	}
}
