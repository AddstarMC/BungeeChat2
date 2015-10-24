package au.com.addstar.bchat.groups;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.ReloadPacket;
import au.com.addstar.bchat.packets.ReloadPacket.ReloadType;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.storage.StorageInterface;
import net.cubespace.geSuit.core.storage.StorageSection;

public class GroupManager {
	private final StorageInterface backend;
	private final Channel<BasePacket> pipe;
	private final Map<String, Group> groupMap;
	private final List<Group> orderedGroups;
	
	private GroupListener listener;
	
	private String consoleName;
	private Group consoleGroup;
	
	public GroupManager(StorageInterface backend, Channel<BasePacket> pipe) {
		this.backend = backend;
		this.pipe = pipe;
		
		groupMap = Collections.synchronizedMap(Maps.newHashMap());
		orderedGroups = Collections.synchronizedList(Lists.newArrayList());
		listener = new NullGroupListener();
	}
	
	public void addGroup(Group group) throws IllegalArgumentException {
		if (groupMap.containsKey(group.getName())) {
			throw new IllegalArgumentException("Duplicate group");
		}
		
		groupMap.put(group.getName(), group);
		
		int index = Collections.binarySearch(orderedGroups, group);
		if (index < 0) {
			index = -(index + 1);
		}
		
		orderedGroups.add(index, group);
		listener.onAddGroup(group);
	}
	
	public List<Group> getGroups() {
		return Collections.unmodifiableList(orderedGroups);
	}
	
	public Group getGroup(String name) {
		return groupMap.get(name);
	}
	
	public void removeGroup(String name) {
		Group group = groupMap.remove(name);
		if (group != null) {
			orderedGroups.remove(group);
			listener.onRemoveGroup(group);
		}
	}
	
	public Group getHighestGroup(Predicate<Group> test) {
		Group best = null;
		for (Group group : orderedGroups) {
			if (test.test(group)) {
				best = group;
			}
		}
		
		return best;
	}
	
	public String getConsoleName() {
		return consoleName;
	}
	
	public void setConsoleName(String name) {
		consoleName = name;
	}
	
	public Group getConsoleGroup() {
		return consoleGroup;
	}
	
	public void setConsoleGroup(Group group) {
		Preconditions.checkArgument(groupMap.get(group.getName()) == group);
		consoleGroup = group;
	}
	
	public void setListener(GroupListener listener) {
		this.listener = listener;
	}
	
	public void load() {
		List<String> groups = backend.getListString("groups");
		if (groups == null) {
			return;
		}
		
		// Remove groups that are not present in the backend
		Iterator<String> it = groupMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (!groups.contains(key)) {
				listener.onRemoveGroup(groupMap.get(key));
				it.remove();
			}
		}
		
		synchronized (groupMap) {
			StorageSection section = backend.getSubsection("group");
			for (String groupName : groups) {
				Group group = new Group(groupName);
				section.getStorable(groupName, group);
				
				Group oldGroup = groupMap.put(groupName, group);
				if (oldGroup != null) {
					orderedGroups.remove(oldGroup);
				}
				
				int index = Collections.binarySearch(orderedGroups, group);
				if (index < 0) {
					index = -(index + 1);
				}
				
				orderedGroups.add(index, group);
				listener.onAddGroup(group);
			}
		}
		
		StorageSection console = backend.getSubsection("console");
		if (console != null) {
			consoleName = console.getString("name", null);
			String groupName = console.getString("group", null);
			if (groupName != null) {
				consoleGroup = groupMap.get(groupName);
			}
		}
	}
	
	public void save() {
		List<String> groupNames = Lists.newArrayList(groupMap.keySet());
		backend.set("groups", groupNames);
		
		synchronized (groupMap) {
			StorageSection section = backend.getSubsection("group");
			for (Group group : groupMap.values()) {
				section.set(group.getName(), group);
			}
		}
		
		StorageSection console = backend.getSubsection("console");
		if (consoleName != null) {
			console.set("name", consoleName);
		} else {
			console.remove("name");
		}
		
		if (consoleGroup != null) {
			console.set("group", consoleGroup.getName());
		} else {
			console.remove("group");
		}
		
		backend.updateAtomic();
		pipe.broadcast(new ReloadPacket(ReloadType.Groups));
	}
	
	private static class NullGroupListener implements GroupListener {
		@Override
		public void onAddGroup(Group group) {}
		@Override
		public void onRemoveGroup(Group group) {}
	}
}
