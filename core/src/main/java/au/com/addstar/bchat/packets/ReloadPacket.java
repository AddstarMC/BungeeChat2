package au.com.addstar.bchat.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.cubespace.geSuit.core.util.NetworkUtils;

public class ReloadPacket extends BasePacket {
	public ReloadType type;
	
	public ReloadPacket() {
	}
	
	public ReloadPacket(ReloadType type) {
		this.type = type;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		NetworkUtils.writeEnum(out, type);
	}

	@Override
	public void read(DataInput in) throws IOException {
		type = NetworkUtils.readEnum(in, ReloadType.class);
	}
	
	public enum ReloadType {
		All,
		Channels,
		Groups
	}
}
