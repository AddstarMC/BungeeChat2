package au.com.addstar.bchat.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import net.cubespace.geSuit.core.util.NetworkUtils;

public class RefreshPacket extends BasePacket {
	public UUID playerId;
	
	public RefreshPacket() {}
	public RefreshPacket(UUID playerId) {
		this.playerId = playerId;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		NetworkUtils.writeUUID(out, playerId);
	}

	@Override
	public void read(DataInput in) throws IOException {
		playerId = NetworkUtils.readUUID(in);
	}
}
