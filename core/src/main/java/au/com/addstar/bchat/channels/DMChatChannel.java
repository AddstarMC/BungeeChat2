package au.com.addstar.bchat.channels;

import java.util.UUID;

import net.cubespace.geSuit.core.GlobalPlayer;

public class DMChatChannel extends FormattedChatChannel {
	private boolean replaceSelf;
	private String replaceWord;
	
	private final GlobalPlayer end1;
	private final GlobalPlayer end2;
	
	DMChatChannel(GlobalPlayer end1, GlobalPlayer end2, DMChannelTemplate template, ChatChannelManager manager) {
		super("@" + end1.getName() + "-" + end2.getName(), manager);
		
		this.end1 = end1;
		this.end2 = end2;
		
		setTemplate(template);
	}
	
	public void setTemplate(DMChannelTemplate template) {
		setFormat(template.getFormat());
		replaceSelf = template.shouldReplaceSelf();
		replaceWord = template.getReplaceWord();
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
	
	public GlobalPlayer getEnd1() {
		return end1;
	}
	
	public GlobalPlayer getEnd2() {
		return end2;
	}
	
	public boolean isParticipant(UUID id) {
		return id.equals(end1.getUniqueId()) || id.equals(end2.getUniqueId());
	}
}
