package au.com.addstar.bchat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import au.com.addstar.bchat.channels.ChannelScope;
import au.com.addstar.bchat.channels.ChatChannel;
import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.channels.ChatChannelTemplate;
import au.com.addstar.bchat.channels.CommandChatChannel;
import au.com.addstar.bchat.channels.FormattedChatChannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class ChannelConfigLoader {
	private final Logger logger;
	private final ChatChannelManager manager;

	public ChannelConfigLoader(ChatChannelManager manager, Logger logger) {
		this.manager = manager;
		this.logger = logger;
	}

	public void load(File file) throws IOException {
		ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
		Configuration config = provider.load(file);

		loadDefault(config);
		loadChannels(config);
		loadTemplates(config);
	}

	private ChatChannel loadDefault(Configuration config) {
		FormattedChatChannel defaultChannel = manager.createFormattedChannel("");

		String format = config.getString("default.format", "{PREFIX}<{DISPLAYNAME}&f>{SUFFIX}: {MESSAGE}");
		format = ChatColor.translateAlternateColorCodes('&', format);
		defaultChannel.setFormat(format);

		return defaultChannel;
	}

	private void loadChannels(Configuration config) {
		Configuration channelSection = config.getSection("channels");
		if (channelSection == null) {
			return;
		}

		for (String channelName : channelSection.getKeys()) {
			// Check for invalid name
			if (channelName.equals("default")) {
				logger.warning("Invalid channel definition 'default'. default is a reserved name");
				continue;
			}

			Configuration section = channelSection.getSection(channelName);
			try {
				loadChannel(channelName, section);
			} catch (IllegalArgumentException e) {
				logger.log(Level.WARNING, "Invalid channel definition '" + channelName + "'", e);
			}
		}
	}

	/**
	 * Load a single channel. This is for the channels section. This should not
	 * be used anywhere else
	 * 
	 * @param name The name of the channel
	 * @param section The Configuration containing the channel data
	 * @throws IllegalArgumentException Thrown if the channel data is incorrect
	 */
	private void loadChannel(String name, Configuration section) throws IllegalArgumentException {
		Optional<String> listenPermission;
		if (section.get("listenPermission") != null) {
			listenPermission = Optional.of(section.getString("listenPermission"));
		} else {
			listenPermission = Optional.absent();
		}

		ChatChannel channel;

		if (section.get("command") != null) {
			// This is a command channel
			String[] commands;
			// Load command(s)
			if (section.get("command") instanceof List<?>) {
				List<String> list = section.getStringList("command");
				if (list.isEmpty()) {
					throw new IllegalArgumentException("Invalid command definition");
				}

				commands = Iterables.toArray(list, String.class);
			} else {
				String command = section.getString("command");
				if (command.trim().isEmpty()) {
					throw new IllegalArgumentException("Invalid command definition");
				}
				commands = new String[] { command.trim() };
			}

			// Load format
			String format = section.getString("format", "{PREFIX}<{DISPLAYNAME}&f>{SUFFIX}: {MESSAGE}");
			format = ChatColor.translateAlternateColorCodes('&', format);

			// Load permission
			Optional<String> commandPermission;
			if (section.getString("commandPermission") != null) {
				commandPermission = Optional.of(section.getString("commandPermission"));
			} else {
				commandPermission = listenPermission;
			}

			// Load scope((CommandChatChannel) channel).setCommands();
			ChannelScope scope = ChannelScope.valueOf(section.getString("scope", "GLOBAL"));

			// Load highlighter
			boolean useHighlighter = section.getBoolean("highlight", false);

			// Create the channel
			channel = manager.createCommandChannel(name, commands);
			((CommandChatChannel) channel).setFormat(format);
			((CommandChatChannel) channel).setCommandPermission(commandPermission);
			channel.setScope(scope);
			((CommandChatChannel) channel).setUseHighlighter(useHighlighter);
		} else {
			// Just a basic chat channel
			channel = manager.createPlainChannel(name);
		}

		channel.setListenPermission(listenPermission);
	}

	private void loadTemplates(Configuration config) {
		Configuration templatesSection = config.getSection("templates");
		if (templatesSection == null) {
			return;
		}

		for (String templateName : templatesSection.getKeys()) {
			// Check for invalid name
			if (templateName.equals("default")) {
				logger.warning("Invalid template definition 'default'. default is a reserved name");
				continue;
			}

			Configuration section = templatesSection.getSection(templateName);
			try {
				loadTemplate(templateName, section);
			} catch (IllegalArgumentException e) {
				logger.log(Level.WARNING, "Invalid template definition '" + templateName + "'", e);
			}
		}
	}

	private void loadTemplate(String name, Configuration section) throws IllegalArgumentException {
		Optional<String> listenPermission;
		if (section.get("listenPermission") != null) {
			listenPermission = Optional.of(section.getString("listenPermission"));
		} else {
			listenPermission = Optional.absent();
		}

		ChatChannelTemplate template = new ChatChannelTemplate(name);

		// Load format
		String format = section.getString("format", "{PREFIX}<{DISPLAYNAME}&f>{SUFFIX}: {MESSAGE}");
		format = ChatColor.translateAlternateColorCodes('&', format);

		// Load permissions
		Optional<String> createPermission;
		if (section.getString("createPermission") != null) {
			createPermission = Optional.of(section.getString("createPermission"));
		} else {
			createPermission = Optional.absent();
		}
		
		Optional<String> joinPermission;
		if (section.getString("joinPermission") != null) {
			joinPermission = Optional.of(section.getString("joinPermission"));
		} else {
			joinPermission = Optional.absent();
		}
		
		// Load scope
		ChannelScope scope = ChannelScope.valueOf(section.getString("scope", "GLOBAL"));

		// Load highlighter
		boolean useHighlighter = section.getBoolean("highlight", false);
		
		template.setListenPermission(listenPermission);
		template.setFormat(format);
		template.setCreatePermission(createPermission);
		template.setJoinPermission(joinPermission);
		template.setScope(scope);
		template.setUseHighlighter(useHighlighter);

		manager.addTemplate(template);
	}
}
