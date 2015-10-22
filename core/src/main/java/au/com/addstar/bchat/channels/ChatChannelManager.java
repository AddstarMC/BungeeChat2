package au.com.addstar.bchat.channels;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.storage.StorageInterface;
import net.cubespace.geSuit.core.storage.StorageSection;

public class ChatChannelManager {
	private final StorageInterface backend;
	
	private final Map<String, ChatChannel> channelMap;
	private final Map<String, ChatChannelTemplate> templateMap;
	
	public ChatChannelManager(StorageInterface backend) {
		this.backend = backend;
		
		channelMap = Collections.synchronizedMap(Maps.newHashMap());
		templateMap = Collections.synchronizedMap(Maps.newHashMap());
	}
	
	private void loadChannels() {
		synchronized(channelMap) {
			List<String> channelList = backend.getListString("channels");
			
			// Remove any invalid channels
			Iterator<String> it = channelMap.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (!channelList.contains(key)) {
					it.remove();
				}
			}
			
			StorageSection channelSection = backend.getSubsection("channel");
			// Load channels
			for (String channelName : channelList) {
				String type = channelSection.getMapPartial(channelName, "type").get("type");
				
				ChatChannel channel;
				switch (type) {
				case "plain":
					channel = new ChatChannel(channelName, this);
					break;
				case "format":
					channel = new FormattedChatChannel(channelName, this);
					break;
				case "command":
					channel = new CommandChatChannel(channelName, this);
					break;
				case "temporary":
					channel = new TemporaryChatChannel(channelName, this);
				default:
					continue;
				}
				
				channelSection.getStorable(channelName, channel);
				
				channelMap.put(channelName, channel);
			}
		}
	}
	
	private void saveChannels() {
		synchronized (channelMap) {
			List<String> channels = Lists.newArrayList(channelMap.keySet());
			backend.set("channels", channels);
			
			StorageSection channelSection = backend.getSubsection("channel");
			for (ChatChannel channel : channelMap.values()) {
				channelSection.set(channel.getName(), channel);
			}
		}
	}
	
	private void loadTemplates() {
		synchronized (templateMap) {
			List<String> templateList = backend.getListString("templates");
			
			// Remove any invalid templates
			Iterator<String> it = templateMap.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (!templateList.contains(key)) {
					it.remove();
				}
			}
			
			StorageSection templateSection = backend.getSubsection("template");
			// Load channels
			for (String templateName : templateList) {
				ChatChannelTemplate template = new ChatChannelTemplate(templateName);
				templateSection.getStorable(templateName, template);
				
				templateMap.put(templateName, template);
			}
		}
	}
	
	private void saveTemplates() {
		synchronized (templateMap) {
			List<String> templates = Lists.newArrayList(templateMap.keySet());
			backend.set("templates", templates);
			
			StorageSection templateSection = backend.getSubsection("template");
			for (ChatChannelTemplate template : templateMap.values()) {
				templateSection.set(template.getName(), template);
			}
		}
	}
	
	/**
	 * Reloads all channels and templates from the backend.
	 */
	public void load() {
		loadTemplates();
		loadChannels();
	}
	
	/**
	 * Saves any changes to channels / templates (and any additions) to the backend.
	 */
	public void save() {
		saveTemplates();
		saveChannels();
		
		backend.updateAtomic();
	}
	
	public TemporaryChatChannel createTemporaryChannel(String name, ChatChannelTemplate template) {
		TemporaryChatChannel channel = new TemporaryChatChannel(name, template, this);
		addChannel(channel);
		return channel;
	}
	
	public ChatChannel createPlainChannel(String name) {
		ChatChannel channel = new ChatChannel(name, this);
		addChannel(channel);
		
		return channel;
	}
	
	public FormattedChatChannel createFormattedChannel(String name) {
		FormattedChatChannel channel = new FormattedChatChannel(name, this);
		addChannel(channel);
		
		return channel;
	}
	
	public CommandChatChannel createCommandChannel(String name) {
		CommandChatChannel channel = new CommandChatChannel(name, this);
		addChannel(channel);
		
		return channel;
	}
	
	private void addChannel(ChatChannel channel) {
		synchronized (channelMap) {
			if (channelMap.containsKey(channel.getName())) {
				throw new IllegalArgumentException("Duplicate channel");
			}
			
			channelMap.put(channel.getName(), channel);
		}
	}
	
	public void removeChannel(String name) {
		channelMap.remove(name);
	}
	
	public ChatChannel getChannel(String name) {
		return channelMap.get(name);
	}
	
	public void addTemplate(ChatChannelTemplate template) {
		synchronized (templateMap) {
			if (templateMap.containsKey(template.getName())) {
				throw new IllegalArgumentException("Duplicate template");
			}
			
			templateMap.put(template.getName(), template);
		}
	}
	
	public void removeTemplate(String name) {
		templateMap.remove(name);
	}
	
	public ChatChannelTemplate getTemplate(String name) {
		return templateMap.get(name);
	}
}
