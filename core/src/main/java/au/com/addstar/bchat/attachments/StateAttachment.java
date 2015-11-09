package au.com.addstar.bchat.attachments;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import au.com.addstar.bchat.groups.Group;
import net.cubespace.geSuit.core.attachments.Attachment;

public class StateAttachment extends Attachment {
	private String groupName;
	private String worldName;
	private String serverName;
	
	private UUID replyTarget;
	
	public void setGroup(Group group) {
		if (group != null && group.getName().equals(groupName)) {
			return;
		}
		
		groupName = group.getName();
		setDirty();
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	public void setWorld(String world) {
		if (!Objects.equals(worldName, world)) {
			worldName = world;
			setDirty();
		}
	}
	
	public String getWorld() {
		return worldName;
	}
	
	public void setServer(String server) {
		if (!Objects.equals(serverName, server)) {
			serverName = server;
			setDirty();
		}
	}
	
	public String getServer() {
		return serverName;
	}
	
	public void setReplyTo(UUID id) {
		if (!Objects.equals(replyTarget, id)) {
			replyTarget = id;
			setDirty();
		}
	}
	
	public UUID getReplyTo() {
		return replyTarget;
	}
	
	@Override
	public void save(Map<String, String> values) {
		if (groupName != null) {
			values.put("group", groupName);
		}
		
		if (worldName != null) {
			values.put("world", worldName);
		}
		
		if (serverName != null) {
			values.put("server", serverName);
		}
		
		if (replyTarget != null) {
			values.put("reply", replyTarget.toString());
		}
	}

	@Override
	public void load(Map<String, String> values) {
		groupName = values.get("group");
		worldName = values.get("world");
		serverName = values.get("server");
		if (values.containsKey("reply")) {
			replyTarget = UUID.fromString(values.get("reply"));
		} else {
			replyTarget = null;
		}
	}

	@Override
	public AttachmentType getType() {
		return AttachmentType.Session;
	}
}
