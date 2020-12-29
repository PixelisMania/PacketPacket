package dev.pixelmania.packetpacket;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import dev.pixelmania.packetpacket.handler.HandlingPipeline;
import dev.pixelmania.packetpacket.packet.PPacket;
import dev.pixelmania.packetpacket.packet.PacketHandler;
import dev.pixelmania.packetpacket.packet.PacketListener;

public class PacketPacket {
	public static JavaPlugin getPlugin() {
		return Core.plugin;
	}
	
	public static void registerPackets(PacketListener packetListenerClass, JavaPlugin javaPlugin) {
		for ( Method method : packetListenerClass.getClass().getMethods() ) {
			if ( method.getAnnotation( PacketHandler.class ) != null && method.getParameterCount() != 0 ) {
				Class< ? > parameterType = method.getParameterTypes()[ 0 ];
				try {
					if ( parameterType.newInstance() instanceof PPacket ) {
							@ SuppressWarnings("unchecked")
							PPacket pPacket = ( ( Class< PPacket > ) parameterType ).newInstance();
							pPacket.register( packetListenerClass, javaPlugin );
					}
				} catch ( Exception exception ) {
					exception.printStackTrace();
				}
			}
		}
	}
	
	public static void sendPacket(Player player, Object packet) {
		try {
			Object playerConnection = HandlingPipeline.getPlayerConnection( player );
			Class< ? > pacCls = Class.forName("net.minecraft.server." + getServerNMS() + ".Packet");
			playerConnection.getClass().getMethod( "sendPacket", pacCls ).invoke( playerConnection, packet );
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	public static String getServerNMS() {
		return Bukkit.getServer().getClass().getPackage().toString().split("\\.")[ 3 ].split(",")[ 0 ];
	}
}
