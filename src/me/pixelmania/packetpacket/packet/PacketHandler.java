package me.pixelmania.packetpacket.packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ Retention( RetentionPolicy.RUNTIME )
@ Target( { ElementType.METHOD } )
public @ interface PacketHandler {
	PacketPriority priority() default PacketPriority.NORMAL;
	
	boolean ignoreCancelled() default false;
}
