package dev.pixelmania.packetpacket.handler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import dev.pixelmania.packetpacket.PacketPacket;
import dev.pixelmania.packetpacket.packet.PPacket;
import dev.pixelmania.packetpacket.packet.PacketCancellation;
import dev.pixelmania.packetpacket.packet.PacketHandler;
import dev.pixelmania.packetpacket.packet.PacketListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;

public class HandlingPipeline implements Listener {
	@ EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		this.openPipeline( event.getPlayer() );
	}
	
	public static Object getPlayerConnection(Player player) {
		try {
			String ver = PacketPacket.getServerNMS();
			Class< ? > cls = ( Class< ? > ) Class.forName( "org.bukkit.craftbukkit." + ver + ".entity.CraftPlayer" );
			Object casted = cls.cast( player );
			Method method = casted.getClass().getMethod( "getHandle", new Class< ? >[] {} );
			Object entityLiving = method.invoke( casted );
			Object playerConnection = entityLiving.getClass().getField( "playerConnection" ).get( entityLiving );
			return playerConnection;
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
		return null;
	}
	
	public static ChannelPipeline getChannelPipeline(Player player) {
		return getChannel( player ).pipeline();
	}
	
	private static Channel getChannel(Player player) {
		try {
			Object playerConnection = getPlayerConnection ( player );
			Object networkManager = playerConnection.getClass().getField( "networkManager" ).get( playerConnection );
			String channel = "channel";
			String nms = PacketPacket.getServerNMS();
			if ( nms.equals("v1_8_R1") ) {
				channel = "i";
			} else if ( nms.equals("v1_8_R2") ) {
				channel = "k";
			}
			Field field = networkManager.getClass().getDeclaredField( channel );
			field.setAccessible( true );
			return ( Channel ) field.get( networkManager );
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
		return null;
	}
	
	public void openPipeline(Player player) {
		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
			
			@ Override
			public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				PacketCancellation packetCancellation = new PacketCancellation();
				handlePacket( packet, context.name(), packetCancellation );
				if ( !packetCancellation.isCancelled() ) {
					super.channelRead( context, packet );
				}
			}
			
			@ Override
			public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) {
				try {
					PacketCancellation packetCancellation = new PacketCancellation();
					handlePacket( packet, context.name(), packetCancellation );
					if ( !packetCancellation.isCancelled() ) {
						super.write( context, packet, promise );
					}
				} catch ( Exception exception ) {
					exception.printStackTrace();
				}
			}
			
		};
		ChannelPipeline pipeline = getChannelPipeline( player );
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
        		
        	}.runTaskTimer( PacketPacket.getPlugin(), 3l, 3l );
        }
	}
	
	public void handlePacket(Object packet, String player, PacketCancellation packetCancellation) {
		try {
			String[] className = packet.getClass().getName().toString().split("\\.");
			String[] nested = className[ className.length - 1 ].split("\\$");
			String packetName = "dev.pixelmania.packetpacket.packet.P" + nested[ nested.length - 1 ];
			PPacket pPacket = ( PPacket ) Class.forName( packetName ).newInstance();
			pPacket.a( packet, player );
			for ( PacketListener packetListener : pPacket.getPacketListeners() ) {
				List< Map< Method, Boolean > > methodLists = Arrays.asList( 
						new HashMap< Method, Boolean >(), new HashMap< Method, Boolean >(), new HashMap< Method, Boolean >(), 
						new HashMap< Method, Boolean >(), new HashMap< Method, Boolean >(), new HashMap< Method, Boolean >() );
				for ( Method method : packetListener.getClass().getMethods() ) {
					PacketHandler packetHandler = method.getAnnotation( PacketHandler.class );
					if ( packetHandler != null && method.getParameterCount() != 0 ) {
						Class < ? > parameterType = method.getParameterTypes()[ 0 ];
						if ( parameterType.isAssignableFrom( pPacket.getClass() ) ) {
							boolean ignoreCancelled = packetHandler.ignoreCancelled();
							switch ( packetHandler.priority() ) {
							case LOWEST: {
								methodLists.get( 0 ).put( method, ignoreCancelled );
								break;
							}
							case LOW: {
								methodLists.get( 1 ).put( method, ignoreCancelled );
								break;
							}
							case NORMAL: {
								methodLists.get( 2 ).put( method, ignoreCancelled );
								break;
							}
							case HIGH: {
								methodLists.get( 3 ).put( method, ignoreCancelled );
								break;
							}
							case HIGHEST: {
								methodLists.get( 4 ).put( method, ignoreCancelled );
								break;
							}
							case MONITOR: {
								methodLists.get( 5 ).put( method, ignoreCancelled );
								break;
							}
							default: methodLists.get( 2 ).put( method, ignoreCancelled );
							}
						}
					}
				}
				int count = 0;
				for ( Map< Method, Boolean > methodList : methodLists ) {
					for ( Method method : methodList.keySet() ) {
						boolean methodGet = methodList.get( method );
						if ( !( pPacket.isCancelled() && methodGet ) || count == 5 ) {
							try {
								method.invoke( packetListener, pPacket );
							} catch ( Exception exception ) {
								exception.printStackTrace();
							}
						}
					}
					count++;
				}
				packetCancellation.setCancelled( pPacket.isCancelled() );
			}
		} catch ( InstantiationException | IllegalAccessException | ClassNotFoundException exception ) {
			exception.printStackTrace();
		}
	}
	
	public static void closePipeline(Player player) {
		Channel channel = getChannel( player );
        channel.eventLoop().submit( () -> {
            channel.pipeline().remove( player.getName() );
            return null;
        });
	}
	
	@ EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		closePipeline( event.getPlayer() );
	}
}
