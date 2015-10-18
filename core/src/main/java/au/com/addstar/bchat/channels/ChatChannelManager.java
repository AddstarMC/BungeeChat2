package au.com.addstar.bchat.channels;

import java.util.Map;

import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.storage.StorageInterface;

public class ChatChannelManager {
	private final StorageInterface backend;
	
	private final Map<String, ChatChannel> channelMap;
	private final Map<String, ChatChannelTemplate> templateMap;
	
	public ChatChannelManager(StorageInterface backend) {
		this.backend = backend;
		
		channelMap = Maps.newHashMap();
		templateMap = Maps.newHashMap();
	}
	
	public void loadChannels() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public TemporaryChatChannel createTemporaryChannel(String name, ChatChannelTemplate template) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
