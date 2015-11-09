package au.com.addstar.bchat.channels;

import java.util.UUID;

import net.cubespace.geSuit.core.GlobalPlayer;

public class DMChatChannel extends PostFormattedChatChannel {
	private boolean replaceSelf;
	private String replaceWord;
	
	private final GlobalPlayer end1;
	private final GlobalPlayer end2;
	
	DMChatChannel(GlobalPlayer end1, GlobalPlayer end2, DMChannelTemplate template, ChatChannelManager manager) {
		super("@" + end1.getUniqueId().toString() + ":" + end2.getUniqueId().toString(), manager);
		
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
	
	/**
	 * Gets the target player if {@code sender} is the sending player.
	 * @param sender The sender of the message. This should be either {@link #getEnd1()} or {@link #getEnd2()}
	 * @return The target player
	 */
	public GlobalPlayer getTarget(GlobalPlayer sender) {
		if (end1.equals(sender)) {
			return end2;
		} else {
			return end1;
		}
	}
	
	@Override
	public String getFormat(GlobalPlayer sender, GlobalPlayer listener) {
		if (!replaceSelf) {
			return getFormat();
		}
		
		GlobalPlayer target = getTarget(sender);
		
		String format = getFormat();
		if (sender.equals(listener)) {
			format = format.replace("{DISPLAYNAME}", replaceWord);
			format = format.replace("{RAWDISPLAYNAME}", replaceWord);
			format = format.replace("{NAME}", replaceWord);
			format = format.replace("{RAWNAME}", replaceWord);
		} else if (target.equals(listener)) {
			format = format.replace("{TDISPLAYNAME}", replaceWord);
			format = format.replace("{TRAWDISPLAYNAME}", replaceWord);
			format = format.replace("{TNAME}", replaceWord);
			format = format.replace("{TRAWNAME}", replaceWord);
		}
		
		return format;
	}
}
