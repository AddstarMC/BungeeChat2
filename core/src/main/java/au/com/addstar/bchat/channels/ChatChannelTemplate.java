package au.com.addstar.bchat.channels;

import java.util.Map;

import com.google.common.base.Optional;

import net.cubespace.geSuit.core.storage.Storable;

/**
 * Represents a template for creating {@link TemporaryChatChannel} instances
 */
public class ChatChannelTemplate implements Storable {
	private final String name;
	private Optional<String> listenPermission;
	private Optional<String> createPermission;
	private Optional<String> joinPermission;
	private ChannelScope scope;
	private String format;
	private boolean useHighlighter;
	
	public ChatChannelTemplate(String name) {
		this.name = name;
		
		listenPermission = Optional.absent();
		createPermission = Optional.absent();
		joinPermission = Optional.absent();
		scope = ChannelScope.GLOBAL;
		
		format = ""; // TODO: Default format
	}
	
	public String getName() {
		return name;
	}
	
	public Optional<String> getListenPermission() {
		return listenPermission;
	}
	
	public void setListenPermission(Optional<String> permission) {
		listenPermission = permission;
	}
	
	public Optional<String> getCreatePermission() {
		return createPermission;
	}
	
	public void setCreatePermission(Optional<String> permission) {
		createPermission = permission;
	}
	
	public Optional<String> getJoinPermission() {
		return joinPermission;
	}
	
	public void setJoinPermission(Optional<String> permission) {
		joinPermission = permission;
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
	
	public boolean getUseHighlighter() {
		return useHighlighter;
	}
	
	public void setUseHighlighter(boolean useHighlighter) {
		this.useHighlighter = useHighlighter;
	}
	
	@Override
	public void save(Map<String, String> values) {
		if (listenPermission.isPresent()) {
			values.put("listen", listenPermission.get());
		}
		
		if (createPermission.isPresent()) {
			values.put("create", createPermission.get());
		}
		
		if (joinPermission.isPresent()) {
			values.put("join", joinPermission.get());
		}
		
		values.put("format", format);
		values.put("scope", scope.name());
		values.put("highlight", String.valueOf(useHighlighter));
	}
	
	@Override
	public void load(Map<String, String> values) {
		if (values.containsKey("listen")) {
			listenPermission = Optional.of(values.get("listen"));
		} else {
			listenPermission = Optional.absent();
		}
		
		if (values.containsKey("create")) {
			createPermission = Optional.of(values.get("create"));
		} else {
			createPermission = Optional.absent();
		}
		
		if (values.containsKey("join")) {
			joinPermission = Optional.of(values.get("join"));
		} else {
			joinPermission = Optional.absent();
		}
		
		format = values.get("format");
		scope = ChannelScope.valueOf(values.get("scope"));
		useHighlighter = Boolean.parseBoolean(values.get("highlight"));
	}
}
