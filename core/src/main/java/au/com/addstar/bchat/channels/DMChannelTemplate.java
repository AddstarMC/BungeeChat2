package au.com.addstar.bchat.channels;

import java.util.Map;

public class DMChannelTemplate extends ChatChannelTemplate {
	public static final String DMName = "#DM";
	
	private boolean replaceSelf;
	private String replaceWord;
	
	public DMChannelTemplate(String name) {
		super(name);
	}
	
	public boolean shouldReplaceSelf() {
		return replaceSelf;
	}
	
	public void setReplaceSelf(boolean replaceSelf) {
		this.replaceSelf = replaceSelf;
	}
	
	public String getReplaceWord() {
		return replaceWord;
	}
	
	public void setReplaceWord(String word) {
		replaceWord = word;
	}
	
	@Override
	public void save(Map<String, String> values) {
		super.save(values);
		
		values.put("replace", String.valueOf(replaceSelf));
		values.put("word", replaceWord);

		values.put("type", "dm");
	}
	
	@Override
	public void load(Map<String, String> values) {
		super.load(values);
		
		replaceSelf = Boolean.parseBoolean(values.get("replace"));
		replaceWord = values.get("word");
	}
}
