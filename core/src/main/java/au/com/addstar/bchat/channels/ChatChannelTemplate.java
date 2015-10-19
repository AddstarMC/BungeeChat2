package au.com.addstar.bchat.channels;

import com.google.common.base.Optional;

/**
 * Represents a template for creating {@link TemporaryChatChannel} instances
 */
public class ChatChannelTemplate {
	private final String name;
	private Optional<String> listenPermission;
	private Optional<String> createPermission;
	private Optional<String> joinPermission;
	private ChannelScope scope;
	private String format;
	
	public ChatChannelTemplate(String name) {
		this.name = name;
		
		listenPermission = Optional.absent();
		createPermission = Optional.absent();
		joinPermission = Optional.absent();
		scope = ChannelScope.GLOBAL;
	}
	
	public String getName() {
		return name;
	}
	
	public Optional<String> getListenPermission() {
		return listenPermission;
	}
	
	public void setListenPermission(String permission) {
		listenPermission = Optional.fromNullable(permission);
	}
	
	public Optional<String> getCreatePermission() {
		return createPermission;
	}
	
	public void setCreatePermission(String permission) {
		createPermission = Optional.fromNullable(permission);
	}
	
	public Optional<String> getJoinPermission() {
		return joinPermission;
	}
	
	public void setJoinPermission(String permission) {
		joinPermission = Optional.fromNullable(permission);
	}
	
	public ChannelScope getScope() {
		return scope;
	}
	
	public void setScope(ChannelScope scope) {
		this.scope = scope;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
}
