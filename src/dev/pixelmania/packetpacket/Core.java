package dev.pixelmania.packetpacket;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import dev.pixelmania.packetpacket.handler.HandlingPipeline;
import dev.pixelmania.packetpacket.packet.PacketCancellation;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;

public class Core extends JavaPlugin {
	protected static Core plugin;
	
	@ Override
	public void onEnable() {
		plugin = this;
		if ( !Bukkit.getOnlinePlayers().isEmpty() ) {
			HandlingPipeline handlingPipeline = new HandlingPipeline();
			for ( Player player : Bukkit.getOnlinePlayers() ) {
				ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
					
					@ Override
					public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
						PacketCancellation packetCancellation = new PacketCancellation();
						handlingPipeline.handlePacket( packet, context.name(), packetCancellation );
						if ( !packetCancellation.isCancelled() ) {
							super.channelRead( context, packet );
						}
					}
					
					@ Override
					public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) {
						try {
							PacketCancellation packetCancellation = new PacketCancellation();
							handlingPipeline.handlePacket( packet, context.name(), packetCancellation );
							if ( !packetCancellation.isCancelled() ) {
								super.write( context, packet, promise );
							}
						} catch ( Exception exception ) {
							exception.printStackTrace();
						}
					}
					
				};
				ChannelPipeline pipeline = HandlingPipeline.getChannelPipeline( player );
				try {
					pipeline.addBefore( "packet_handler", player.getName(), channelDuplexHandler );
				} catch ( Exception exception ) {
					new BukkitRunnable() {
						
						@ Override
						public void run() {
							try {
								pipeline.addBefore( "packet_handler", player.getName(), channelDuplexHandler );
								this.cancel();
							} catch ( Exception exception ) {}
						}
						
					}.runTaskTimer( this, 3l, 3l );
				}
			}
		}
		Bukkit.getPluginManager().registerEvents( new HandlingPipeline(), this );
	}
	
	@ Override
	public void onDisable() {
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			HandlingPipeline.closePipeline( player );
		}
	}
}
