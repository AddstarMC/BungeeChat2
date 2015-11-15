package au.com.addstar.bchat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.storage.StorageInterface;
import net.md_5.bungee.api.ChatColor;

public class HighlighterConfigLoader {
	private final StorageInterface storage;
	private final Logger logger;
	
	public HighlighterConfigLoader(StorageInterface storage, Logger logger) {
		this.storage = storage;
		this.logger = logger;
	}
	
	public void load(File file) throws IOException {
		InputStream stream = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			String line;
			int lineNo = 0;
			StringBuilder colour = new StringBuilder();
			Map<String, String> map = Maps.newHashMap();
			
			while((line = reader.readLine()) != null) {
				++lineNo;
				
				if (line.startsWith("#") || line.trim().isEmpty()) {
					continue;
				}
				
				String regex, colourString;
			
				if(line.contains(">")) {
					int pos = line.lastIndexOf('>');
					regex = line.substring(0, pos).trim();
					colourString = line.substring(pos + 1).trim();
				} else {
					regex = line.trim();
					colourString = ChatColor.GOLD.toString();
				}

				try {
					Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				} catch(PatternSyntaxException e) {
					logger.warning("[" + file.getName() + "] Invalid regex: \"" + regex + "\" at line " + lineNo);
					continue;
				}
				
				colour.setLength(0); 
				for(int i = 0; i < colourString.length(); ++i) {
					char c = colourString.charAt(i);
					ChatColor col = ChatColor.getByChar(c);
					
					if(col == null) {
						logger.warning("[" + file.getName() + "] Invalid colour code: \'" + c + "\' at line " + lineNo);
						continue;
					}
					
					colour.append(col.toString());
				}
				
				map.put(regex, colour.toString());
			}
			
			// Save the loaded keywords
			storage.set("keywords", map);
			storage.update();
		} finally {
			reader.close();
		}
	}
}
