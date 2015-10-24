package au.com.addstar.bchat.attachments;

import java.util.Map;

import au.com.addstar.bchat.groups.Group;
import net.cubespace.geSuit.core.attachments.Attachment;

public class StateAttachment extends Attachment {
	private String groupName;
	
	public void setGroup(Group group) {
		groupName = group.getName();
		setDirty();
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	@Override
	public void save(Map<String, String> values) {
		if (groupName != null) {
			values.put("group", groupName);
		}
	}

	@Override
	public void load(Map<String, String> values) {
		groupName = values.get("group");
	}

	@Override
	public AttachmentType getType() {
		return AttachmentType.Session;
	}
}
