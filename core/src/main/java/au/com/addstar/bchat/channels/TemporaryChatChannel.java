package au.com.addstar.bchat.channels;

import java.util.Map;
import java.util.UUID;

public class TemporaryChatChannel extends FormattedChatChannel {
	private String name;
	private UUID channelAdmin;
	
	private ChatChannelTemplate template;
	
	TemporaryChatChannel(String name, ChatChannelTemplate template, ChatChannelManager manager) {
		super (template.getName() + ":" + name, manager);
		
		this.name = name;
		setTemplate(template);
	}
	
	TemporaryChatChannel(String fullName, ChatChannelManager manager) {
		super(fullName, manager);
	}
	
	public String getSubName() {
		return name;
	}
	
	public ChatChannelTemplate getTemplate() {
		return template;
	}
	
	public UUID getChannelAdmin() {
		return channelAdmin;
	}
	
	public void setChannelAdmin(UUID userId) {
		channelAdmin = userId;
	}
	
	public void setTemplate(ChatChannelTemplate template) {
		this.template = template;
		
		setFormat(template.getFormat());
		setListenPermission(template.getListenPermission());
		setScope(template.getScope());
	}
	
	public void remove() {
		manager.removeChannel(getName());
	}
	
	@Override
	public void save(Map<String, String> values) {
		super.save(values);
		
		values.put("subname", name);
		values.put("template", template.getName());
		
		if (channelAdmin != null) {
			values.put("admin", channelAdmin.toString());
		}
		
		values.put("type", "temp");
	}
	
	@Override
	public void load(Map<String, String> values) {
		super.load(values);
		
		name = values.get("subname");
		String templateName = values.get("template");
		ChatChannelTemplate template = manager.getTemplate(templateName);
		
		if (template == null) {
			throw new IllegalArgumentException("Missing template");
		}
		
		setTemplate(template);
		
		if (values.containsKey("admin")) {
			channelAdmin = UUID.fromString(values.get("admin"));
		}
	}
}
