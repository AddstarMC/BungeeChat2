package au.com.addstar.bchat;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import au.com.addstar.bchat.groups.Group;
import au.com.addstar.bchat.groups.GroupManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class GroupConfigLoader {
	private final Logger logger;
	private final GroupManager manager;
	
	public GroupConfigLoader(GroupManager manager, Logger logger) {
		this.manager = manager;
		this.logger = logger;
	}
	
	public void load(File file) throws IOException {
		ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
		Configuration config = provider.load(file);
		
		loadGroups(config);
		loadConsole(config);
	}
	
	private void loadGroups(Configuration config) {
		Configuration groups = config.getSection("groups");
		if (groups == null) {
			return;
		}
		
		for (String name : groups.getKeys()) {
			try {
				Group group = loadGroup(name, groups.getSection(name));
				manager.addGroup(group);
			} catch (IllegalArgumentException e) {
				logger.warning("Invalid group definition '" + name + "'. " + e.getMessage());
			}
		}
	}
	
	private Group loadGroup(String name, Configuration section) {
		Group group = new Group(name);
		
		// Settings for non default group
		if (!name.equals("default")) {
			if (section.getString("permission") == null) {
				throw new IllegalArgumentException("Missing permission");
			}
			
			if (section.getString("priority") == null) {
				throw new IllegalArgumentException("Missing priority");
			}
			
			group.setPermission(section.getString("permission"));
			
			int priority = section.getInt("priority", Integer.MIN_VALUE);
			if (priority == Integer.MIN_VALUE) {
				throw new IllegalArgumentException("Invalid priority");
			}
			
			group.setPriority(priority);
		}
		
		// Common settings
		group.setColor(section.getString("color", ""));
		group.setPrefix(section.getString("prefix"));
		group.setSuffix(section.getString("suffix"));
		
		return group;
	}
	
	private void loadConsole(Configuration config) {
		Configuration section = config.getSection("console");
		if (section == null) {
			return;
		}
		
		manager.setConsoleName(section.getString("name"));
		String groupName = section.getString("group");
		
		if (groupName != null) {
			Group group = manager.getGroup(groupName);
			if (group != null) {
				manager.setConsoleGroup(group);
			} else {
				logger.warning("Invalid console group. It is unknown");
			}
		}
	}
}
