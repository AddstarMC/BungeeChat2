package au.com.addstar.bchat.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import au.com.addstar.bchat.channels.ChatChannel;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.util.NetworkUtils;

/**
 * Represents a message being sent to a channel
 */
public class PostFormatBroadcastPacket extends BasePacket {
	public String channelId;
	public String message;
	public UUID sender;
	public boolean isHighlightChannel;
	
	public PostFormatBroadcastPacket() {
	}
	
	public PostFormatBroadcastPacket(ChatChannel channel, String message, GlobalPlayer sender, boolean isHighlightChannel) {
		this.channelId = channel.getName();
		this.message = message;
		this.sender = sender.getUniqueId();
		this.isHighlightChannel = isHighlightChannel;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeBoolean(isHighlightChannel);
		out.writeUTF(channelId);
		NetworkUtils.writeUUID(out, sender);
		out.writeUTF(message);
	}

	@Override
	public void read(DataInput in) throws IOException {
		isHighlightChannel = in.readBoolean();
		channelId = in.readUTF();
		sender = NetworkUtils.readUUID(in);
		message = in.readUTF();
	}
}
