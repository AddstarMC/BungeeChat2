package au.com.addstar.bchat.groups;

import java.util.Map;

import net.cubespace.geSuit.core.storage.Storable;
import net.md_5.bungee.api.ChatColor;

public class Group implements Storable, Comparable<Group> {
	private final String name;
	
	private String permission;
	private String color;
	private int priority;
	private String prefix;
	private String suffix;
	
	public Group(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPermission() {
		return permission;
	}
	
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
	public String getColor() {
		return color;
	}
	
	public String getColorFormatted() {
		String formatted = "";
		for (char c : color.toCharArray()) {
			formatted += ChatColor.COLOR_CHAR + String.valueOf(c);
		}
		
		return formatted;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setPrefix(String prefix) {
		if (prefix != null) {
			prefix = ChatColor.translateAlternateColorCodes('&', prefix); 
		}
		this.prefix = prefix;
	}
	
	public String getSuffix() {
		return suffix;
	}
	
	public void setSuffix(String suffix) {
		if (suffix != null) {
			suffix = ChatColor.translateAlternateColorCodes('&', suffix); 
		}
		this.suffix = suffix;
	}
	
	@Override
	public void save(Map<String, String> values) {
		if (permission != null) {
			values.put("permission", permission);
		}
		values.put("color", color);
		values.put("priority", String.valueOf(priority));
		if (prefix != null) {
			values.put("prefix", prefix);
		}
		if (suffix != null) {
			values.put("suffix", suffix);
		}
	}
	
	@Override
	public void load(Map<String, String> values) {
		permission = values.get("permission");
		color = values.get("color");
		priority = Integer.parseInt(values.get("priority"));
		prefix = values.get("prefix");
		suffix = values.get("suffix");
	}
	
	@Override
	public int compareTo(Group o) {
		return Integer.compare(priority, o.priority);
	}
}
