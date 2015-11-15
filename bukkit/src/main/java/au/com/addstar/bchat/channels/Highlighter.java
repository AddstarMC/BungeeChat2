package au.com.addstar.bchat.channels;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

import com.google.common.collect.Lists;

import au.com.addstar.bchat.util.Lazy;
import net.cubespace.geSuit.core.storage.StorageInterface;

public class Highlighter {
	private final StorageInterface storage;
	private final List<HighlighterOption> options;
	
	public Highlighter(StorageInterface storage) {
		this.storage = storage;
		options = Lists.newArrayList();
	}
	
	/**
	 * Loads the highlighter settings from the backend
	 */
	public void load() {
		storage.reset();
		Map<String, String> values = storage.getMap("keywords", Collections.emptyMap());
		
		options.clear();
		for (Entry<String, String> entry : values.entrySet()) {
			Pattern pattern = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
			options.add(new HighlighterOption(pattern, entry.getValue()));
		}
	}
	
	/**
	 * Highlights the input string according to the loaded highlighter settings
	 * @param input The input string
	 * @param initialColour This is the initial colour of the input string 
	 * @return The highlighted input string. If no highlighting is needed this will be the input string
	 */
	public String highlight(String input, String initialColour) {
		StringBuffer buffer = new StringBuffer(input);
		boolean changed = false;
		
		// The color to change to before
		String defaultColour = initialColour;
		
		for (HighlighterOption option : options) {
			Matcher m = option.pattern.matcher(buffer.toString());
			
			// Colorize the matching items
			Lazy<StringBuffer> modified = Lazy.of(() -> new StringBuffer());
			while (m.find()) {
				String restoreColour = ChatColor.getLastColors(buffer.substring(0, m.end()));
				if (restoreColour.isEmpty()) {
					restoreColour = defaultColour;
				}
				
				m.appendReplacement(modified.get(), option.color + m.group(0) + restoreColour);
			}
			
			if (modified.isAssigned()) {
				m.appendTail(modified.get());
				buffer = modified.get();
				changed = true;
			}
		}
		
		if (changed) {
			return buffer.toString();
		} else {
			return input;
		}
	}
	
	private static class HighlighterOption {
		private final Pattern pattern;
		private final String color;
		
		public HighlighterOption(Pattern pattern, String color) {
			this.pattern = pattern;
			this.color = color;
		}
	}
}
