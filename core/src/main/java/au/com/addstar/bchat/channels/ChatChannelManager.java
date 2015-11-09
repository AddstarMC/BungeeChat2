package au.com.addstar.bchat.channels;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import au.com.addstar.bchat.Debugger;
import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.ReloadPacket;
import au.com.addstar.bchat.packets.ReloadPacket.ReloadType;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.GlobalServer;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.objects.Tuple;
import net.cubespace.geSuit.core.storage.StorageInterface;
import net.cubespace.geSuit.core.storage.StorageSection;

public class ChatChannelManager {
	public static final String DefaultChannel = "";
	private static final String GlobalDef = "#global";
	
	private final StorageInterface backend;
	private final Channel<BasePacket> channel;
	
	private final Map<String, ChatChannel> channelMap;
	private final Map<String, ChatChannelTemplate> templateMap;
	
	private final Map<String, CommandChatChannel> commandMap;
	private final Map<String, Map<String, String>> defaultChannelMap;
	
	private final Map<Tuple<GlobalPlayer, GlobalPlayer>, DMChatChannel> dmChannels;
	
	public ChatChannelManager(StorageInterface backend, Channel<BasePacket> channel) {
		this.backend = backend;
		this.channel = channel;
		
		channelMap = Collections.synchronizedMap(Maps.newHashMap());
		templateMap = Collections.synchronizedMap(Maps.newHashMap());
		commandMap = Collections.synchronizedMap(Maps.newHashMap());
		
		defaultChannelMap = Maps.newHashMap();
		
		dmChannels = Collections.synchronizedMap(Maps.newHashMap());
	}
	
	public void clear() {
		channelMap.clear();
		templateMap.clear();
		commandMap.clear();
		defaultChannelMap.clear();
	}
	
	private void loadChannels() {
		synchronized(channelMap) {
			List<String> channelList = backend.getListString("channels");
			Logger debug = Debugger.getLogger(Debugger.Backend);
			debug.info("Loading channels from backend");
			
			// Remove any invalid channels
			Iterator<String> it = channelMap.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (!channelList.contains(key)) {
					debug.fine("Removing absent local channel " + key);
					
					// Remove command bindings
					ChatChannel channel = channelMap.remove(key);
					if (channel instanceof CommandChatChannel) {
						CommandChatChannel cChannel = (CommandChatChannel)channel;
						for (String command : cChannel.getCommands()) {
							commandMap.remove(command.toLowerCase(), cChannel);
						}
					}
					
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
					break;
				default:
					continue;
				}
				
				channelSection.getStorable(channelName, channel);
				
				channelMap.put(channelName, channel);
				debug.fine("Adding local channel " + channelName);
				if (channel instanceof CommandChatChannel) {
					CommandChatChannel cChannel = (CommandChatChannel)channel;
					debug.finer("Registering commands for " + channelName + ": " + cChannel.getCommands());
					for (String command : cChannel.getCommands()) {
						commandMap.put(command.toLowerCase(), cChannel);
					}
				}
			}
		}
	}
	
	private void saveChannels() {
		synchronized (channelMap) {
			Logger debug = Debugger.getLogger(Debugger.Backend);
			debug.info("Saving channels to backend");
			
			List<String> channels = Lists.newArrayList(channelMap.keySet());
			backend.set("channels", channels);
			
			StorageSection channelSection = backend.getSubsection("channel");
			for (ChatChannel channel : channelMap.values()) {
				channelSection.set(channel.getName(), channel);
				debug.fine("Pushing channel " + channel.getName());
			}
		}
	}
	
	private void loadTemplates() {
		synchronized (templateMap) {
			Logger debug = Debugger.getLogger(Debugger.Backend);
			debug.info("Loading templates from backend");
			
			List<String> templateList = backend.getListString("templates");
			
			// Remove any invalid templates
			Iterator<String> it = templateMap.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (!templateList.contains(key)) {
					debug.fine("Removing absent local template " + key);
					it.remove();
				}
			}
			
			StorageSection templateSection = backend.getSubsection("template");
			// Load channels
			for (String templateName : templateList) {
				ChatChannelTemplate template;
				String type = templateSection.getMapPartial(templateName, "type").get("type");
				if (type != null) {
					template = new DMChannelTemplate(templateName);
				} else {
					template = new ChatChannelTemplate(templateName);
				}
				
				templateSection.getStorable(templateName, template);
				
				templateMap.put(templateName, template);
				debug.fine("Adding local template " + templateName);
				
				// Update all current DMChannels
				if (templateName.equals(DMChannelTemplate.DMName) && template instanceof DMChannelTemplate) {
					for (DMChatChannel channel : dmChannels.values()) {
						channel.setTemplate((DMChannelTemplate)template);
					}
				}
			}
		}
	}
	
	private void saveTemplates() {
		synchronized (templateMap) {
			Logger debug = Debugger.getLogger(Debugger.Backend);
			debug.info("Saving templates to backend");
			
			List<String> templates = Lists.newArrayList(templateMap.keySet());
			backend.set("templates", templates);
			
			StorageSection templateSection = backend.getSubsection("template");
			for (ChatChannelTemplate template : templateMap.values()) {
				templateSection.set(template.getName(), template);
				debug.fine("Pushing template " + template.getName());
			}
		}
	}
	
	private void loadDefaultChannels() {
		synchronized (defaultChannelMap) {
			Logger debug = Debugger.getLogger(Debugger.Backend);
			debug.info("Loading default channels from backend");
			
			List<String> servers = backend.getListString("defaults");
			
			// Remove any invalid templates
			Iterator<String> it = defaultChannelMap.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (!servers.contains(key)) {
					it.remove();
					debug.fine("Removing absent local default " + key);
				}
			}
			
			StorageSection section = backend.getSubsection("default");
			// Load defaults
			for (String serverName : servers) {
				Map<String, String> map = defaultChannelMap.get(serverName);
				if (map == null) {
					map = Maps.newHashMap();
					defaultChannelMap.put(serverName, map);
				}
				
				if (serverName.equals(GlobalDef)) {
					// Special global def
					map.put(GlobalDef, section.getString(GlobalDef, ""));
				} else {
					// Normal server def
					map.clear();
					Map<String, String> target = section.getMap(serverName, Collections.emptyMap());
					map.putAll(target);
				}
				
				debug.fine("Adding local default " + serverName + " = " + map);
			}
		}
	}
	
	private void saveDefaultChannels() {
		synchronized (defaultChannelMap) {
			Logger debug = Debugger.getLogger(Debugger.Backend);
			debug.info("Saving channel defaults to backend");
			
			List<String> servers = Lists.newArrayList(defaultChannelMap.keySet());
			backend.set("defaults", servers);
			
			StorageSection section = backend.getSubsection("default");
			for (String server : defaultChannelMap.keySet()) {
				if (server.equals(GlobalDef)) {
					section.set(GlobalDef, defaultChannelMap.get(server).getOrDefault(GlobalDef, ""));
				} else {
					section.set(server, defaultChannelMap.get(server));
				}
			}
		}
	}
	
	/**
	 * Reloads all channels and templates from the backend.
	 */
	public void load() {
		backend.reset();
		loadTemplates();
		loadChannels();
		loadDefaultChannels();
	}
	
	/**
	 * Saves any changes to channels / templates (and any additions) to the backend.
	 */
	public void save() {
		saveTemplates();
		saveChannels();
		saveDefaultChannels();
		
		backend.updateAtomic();
		channel.broadcast(new ReloadPacket(ReloadType.Channels));
		Debugger.getLogger(Debugger.Packet).info("Broadcasting reload for channels");
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
	
	public CommandChatChannel createCommandChannel(String name, String...commands) {
		CommandChatChannel channel = new CommandChatChannel(name, commands, this);
		addChannel(channel);
		
		return channel;
	}
	
	private void addChannel(ChatChannel channel) {
		synchronized (channelMap) {
			if (channelMap.containsKey(channel.getName())) {
				throw new IllegalArgumentException("Duplicate channel");
			}
			
			channelMap.put(channel.getName(), channel);
			
			if (channel instanceof CommandChatChannel) {
				CommandChatChannel cChannel = (CommandChatChannel)channel;
				for (String command : cChannel.getCommands()) {
					commandMap.put(command.toLowerCase(), cChannel);
				}
			}
		}
	}
	
	public void removeChannel(String name) {
		ChatChannel channel = channelMap.remove(name);
		if (channel instanceof CommandChatChannel) {
			CommandChatChannel cChannel = (CommandChatChannel)channel;
			for (String command : cChannel.getCommands()) {
				commandMap.remove(command.toLowerCase(), cChannel);
			}
		}
	}
	
	public ChatChannel getChannel(String name) {
		if (name.startsWith("@")) {
			// DM Channel
			name = name.substring(1);
			String[] ids = name.split(":");
			UUID end1 = UUID.fromString(ids[0]);
			UUID end2 = UUID.fromString(ids[1]);
			GlobalPlayer player1 = Global.getPlayer(end1);
			GlobalPlayer player2 = Global.getPlayer(end2);
			
			if (player1 == null || player2 == null) {
				return null;
			}
			
			return getDMChannel(player1, player2);
		}
		
		return channelMap.get(name);
	}
	
	public CommandChatChannel getChannelForCommand(String command) {
		return commandMap.get(command.toLowerCase());
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
	
	/**
	 * Gets the channel chat is sent to for a server
	 * @param server The server
	 * @return The channel to send chat to.
	 */
	public ChatChannel getDefaultChannel(GlobalServer server) {
		Map<String, String> map = defaultChannelMap.get(server.getName());
		if (map == null) {
			map = defaultChannelMap.getOrDefault(GlobalDef, Collections.emptyMap());
		}
		
		ChatChannel channel = null;
		if (map.containsKey(GlobalDef)) {
			String channelName = map.get(GlobalDef);
			channel = channelMap.get(channelName);
		}
		
		if (channel == null) {
			channel = channelMap.get(ChatChannelManager.DefaultChannel);
		}
		
		return channel;
	}
	
	/**
	 * Gets the channel chat is sent to for a server and world
	 * @param server The server
	 * @param world The world
	 * @return The channel to send chat to.
	 */
	public ChatChannel getDefaultChannel(GlobalServer server, String world) {
		Map<String, String> map = defaultChannelMap.get(server.getName());
		if (map == null) {
			map = defaultChannelMap.getOrDefault(GlobalDef, Collections.emptyMap());
		}
		
		ChatChannel channel = null;
		if (map.containsKey(world)) {
			String channelName = map.get(world);
			channel = channelMap.get(channelName);
		} else if (map.containsKey(GlobalDef)) {
			String channelName = map.get(GlobalDef);
			channel = channelMap.get(channelName);
		}
		
		if (channel == null) {
			channel = channelMap.get(ChatChannelManager.DefaultChannel);
		}
		
		return channel;
	}
	
	private void setDefaultChannel0(String server, String world, ChatChannel channel) {
		Map<String, String> map = defaultChannelMap.get(server);
		if (map == null) {
			map = Maps.newHashMap();
			defaultChannelMap.put(server, map);
		}
		
		map.put(world, channel.getName());
	}
	
	/**
	 * Sets the default channel for a server. This is used if no world specific value is available
	 * @param server The server to set it on
	 * @param channel The channel to default to
	 */
	public void setDefaultChannel(String server, ChatChannel channel) {
		Preconditions.checkArgument(!server.equals(GlobalDef));
		setDefaultChannel0(server, GlobalDef, channel);
	}
	
	/**
	 * Sets the default channel for a server and world.
	 * @param server The server to set it on
	 * @param world The world to set it in
	 * @param channel The channel to default to
	 */
	public void setDefaultChannel(String server, String world, ChatChannel channel) {
		Preconditions.checkArgument(!server.equals(GlobalDef));
		Preconditions.checkArgument(!world.equals(GlobalDef));
		setDefaultChannel0(server, world, channel);
	}
	
	/**
	 * Sets the global default channel. This is used if no more specific defaults exists
	 * @param channel The channel to default to
	 */
	public void setDefaultChannel(ChatChannel channel) {
		setDefaultChannel0(GlobalDef, GlobalDef, channel);
	}
	
	/**
	 * Gets a direct messaging channel from one player to another.
	 * The from and too players can be in any order
	 * @param from One participant of the channel
	 * @param to The other participant of the channel 
	 * @return A DM channel to communicate on
	 */
	public DMChatChannel getDMChannel(GlobalPlayer from, GlobalPlayer to) {
		Tuple<GlobalPlayer, GlobalPlayer> tuple1 = new Tuple<>(from, to);
		Tuple<GlobalPlayer, GlobalPlayer> tuple2 = new Tuple<>(to, from);
		
		synchronized (dmChannels) {
			DMChatChannel channel = dmChannels.get(tuple1);
			if (channel == null) {
				channel = dmChannels.get(tuple2);
			}
			
			if (channel == null) {
				channel = new DMChatChannel(from, to, getDMTemplate(), this);
				dmChannels.put(tuple1, channel);
			}
			
			return channel;
		}
	}
	
	private DMChannelTemplate getDMTemplate() {
		return (DMChannelTemplate)templateMap.get(DMChannelTemplate.DMName);
	}
	
	/**
	 * For internal use only. Do not use
	 * @param player
	 */
	public void onDisconnect(GlobalPlayer player) {
		synchronized (dmChannels) {
			Map<Tuple<GlobalPlayer, GlobalPlayer>, DMChatChannel> channels = Maps.filterKeys(dmChannels, (k) -> k.getA() == player || k.getB() == player);
			channels.clear();
		}
	}
}
