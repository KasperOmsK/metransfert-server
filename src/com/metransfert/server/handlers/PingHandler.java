package com.metransfert.server.handlers;

import java.io.IOException;

import com.metransfert.common.PacketTypes;
import com.metransfert.server.exceptions.ConnectionLostException;
import com.packeteer.network.Packet;
import com.packeteer.network.PacketBuilder;
import com.packeteer.network.PacketHeader;
import com.packeteer.network.PacketInputStream;
import com.packeteer.network.PacketOutputStream;

public class PingHandler extends TransactionHandler {

	public PingHandler(PacketOutputStream out, PacketInputStream in) {
		super(out, in);
	}
	
	public void Handle(PacketHeader header) throws ConnectionLostException {
		Packet pong = PacketBuilder.newBuilder(PacketTypes.PONG).build();
		try {
			out.writeAndFlush(pong);
		} catch (IOException e) {
			throw new ConnectionLostException(e);
		}
	}
}
