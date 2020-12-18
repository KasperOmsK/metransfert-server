package com.metransfert.server;

import com.metransfert.common.PacketTypes;
import com.packeteer.network.Packet;

public class ErrorPacket extends Packet {
	
	public ErrorPacket(byte errorCode) {
		super(PacketTypes.ERROR, new byte[]{errorCode});
	}
	
}
