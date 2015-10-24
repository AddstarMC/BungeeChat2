package au.com.addstar.bchat.groups;

import java.util.Map;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import com.google.common.collect.Maps;

/**
 * This class handles registering group permissions officially.
 * This is required so that the permissions default value
 * can be set so that nobody gets it by default.
 * This means that being op does not grant the highest permission level
 */
public class GroupPermissionHandler implements GroupListener {
	private final PluginManager manager;
	private final Map<Group, Permission> installedPermissions;
	
	public GroupPermissionHandler(PluginManager manager) {
		this.manager = manager;
		installedPermissions = Maps.newHashMap();
	}
	
	@Override
	public void onAddGroup(Group group) {
		if (group.getPermission() != null) {
			if (manager.getPermission(group.getPermission()) == null) {
				Permission permission = new Permission(group.getPermission(), PermissionDefault.FALSE);
				manager.addPermission(permission);
				installedPermissions.put(group, permission);
			}
		}
	}

	@Override
	public void onRemoveGroup(Group group) {
		Permission permission = installedPermissions.remove(group);
		if (permission != null) {
			manager.removePermission(permission);
		}
	}
}
