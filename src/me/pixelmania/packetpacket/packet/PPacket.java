package me.pixelmania.packetpacket.packet;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface PPacket {
	public void a(Object a, String b);
	
	public PacketListener[] getPacketListeners();
	
	public void register(PacketListener packetListenerClass, JavaPlugin javaPlugin);
	
	public void setCancelled(boolean isCancelled);
	
	public boolean isCancelled();
	
	public Player getPlayer();
	
	public String getPlayerName();
	
	public Object getRawPacket();
}
