package aPackage;

import me.pixelmania.packetpacket.packet.PPacketPlayInChat;
import me.pixelmania.packetpacket.packet.PacketHandler;
import me.pixelmania.packetpacket.packet.PacketListener;
import me.pixelmania.packetpacket.packet.PacketPriority;

public class AClass implements PacketListener {
	@ PacketHandler ( priority = PacketPriority.HIGHEST, ignoreCancelled = false )
	public void aMethod(PPacketPlayInChat packet) {
		packet.setCancelled( true );
		System.out.println( packet.getPlayerName() + " : " + packet.isCancelled() );
	}
}

// Class must implement "dev.pixelmania.packetpacket.packet.PacketListener" as shown above.
// priority and ignoreCancelled is optional for @ PacketHandler.
// all the packet names are the same as Bukkit's but just add an extra "P" to the beginnning.
// ( "PacketPlayInChat" - from Bukkit is "PPacketPlayInChat" - from PacketPacket )
