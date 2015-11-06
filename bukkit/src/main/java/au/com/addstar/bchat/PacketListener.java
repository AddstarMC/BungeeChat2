package au.com.addstar.bchat;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.RefreshPacket;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;

public class PacketListener implements ChannelDataReceiver<BasePacket> {
	private final Plugin plugin;
	public PacketListener(Plugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onDataReceive(Channel<BasePacket> channel, BasePacket packet, int sourceServer, boolean isBroadcast) {
		if (packet instanceof RefreshPacket) {
			handleRefresh((RefreshPacket)packet);
		}
	}
	
	private void handleRefresh(RefreshPacket packet) {
		final Player player = plugin.getServer().getPlayer(packet.playerId);
		if (player == null) {
			return;
		}
		
		// Hide then show to all other players (respecting original visible state)
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				for (final Player other : player.getWorld().getPlayers()) {
					if (other.canSee(player)) {
						other.hidePlayer(player);
						plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
							@Override
							public void run() {
								other.showPlayer(player);
							}
						}, 1);
					}
				}
			}
		}, 10);
	}
}
