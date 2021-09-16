package aPackage;

import org.bukkit.plugin.java.JavaPlugin;

import bPackage.APacketListenerClass;
import me.pixelmania.packetpacket.PacketPacket;

public class AClass extends JavaPlugin {
	@ Override
	public void onEnable() {
		PacketPacket.registerPackets( new APacketListenerClass(), this );
	}
}

// Registering packets is the same as if you were registering Bukkit events as shown above and below.
// Bukkit.getPluginManager().registerEvents( new EventListenerClass(), this ); << Bukkit Event Registering
// ;
// PacketPacket.registerPackets( new PacketsListenerClass(), this); << Packets Registering
// Very simple.
