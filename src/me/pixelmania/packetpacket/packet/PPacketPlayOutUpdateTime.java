package me.pixelmania.packetpacket.packet;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PPacketPlayOutUpdateTime implements PPacket {
	private static Map< PacketListener, JavaPlugin > packetListeners = new HashMap< PacketListener, JavaPlugin >();
	
	public static PacketListener[] manualGetPacketListeners() {
		PacketListener[] packetListenersArray = new PacketListener[ packetListeners.size() ];
		return packetListeners.keySet().toArray( packetListenersArray );
	}
	
	@ Override
	public PacketListener[] getPacketListeners() {
		return manualGetPacketListeners();
	}
	
	public static void manualRegister(PacketListener packetListenerClass, JavaPlugin javaPlugin) {
		packetListeners.put( packetListenerClass, javaPlugin );
	}
	
	public static void unregister(PacketListener packetListenerClass) {
		packetListeners.remove( packetListenerClass );
	}
	
	@ Override
	public void register(PacketListener packetListenerClass, JavaPlugin javaPlugin) {
		packetListeners.put( packetListenerClass, javaPlugin );
	}
	
	private boolean isCancelled;
	
	@ Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
	
	@ Override
	public boolean isCancelled() {
		return this.isCancelled;
	}
	
	private Object packet;
	private String player;
	
	@ Override
	public void a(Object a, String b) {
		this.packet = a;
		this.player = b;
	}
	
	@ Override
	public Player getPlayer() {
		return Bukkit.getPlayer( this.player );
	}
	
	@ Override
	public String getPlayerName() {
		return this.player;
	}
	
	@ Override
	public Object getRawPacket() {
		return this.packet;
	}
}
