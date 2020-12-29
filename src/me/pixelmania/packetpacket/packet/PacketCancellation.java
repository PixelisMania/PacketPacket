package me.pixelmania.packetpacket.packet;

public class PacketCancellation {
	private boolean isCancelled;
	
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
	
	public boolean isCancelled() {
		return this.isCancelled;
	}
}
