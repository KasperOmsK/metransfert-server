package com.metransfert.server.handlers;

import java.io.IOException;

import com.metransfert.common.PacketTypes;
import com.metransfert.server.exceptions.ConnectionLostException;
import com.packeteer.network.Packet;
import com.packeteer.network.PacketBuilder;
import com.packeteer.network.PacketHeader;
import com.packeteer.network.PacketInputStream;
import com.packeteer.network.PacketOutputStream;

public class TransactionHandler implements ITransactionHandler {
	
	protected PacketOutputStream out;
	protected PacketInputStream in;
	
	
	public TransactionHandler(PacketOutputStream out, PacketInputStream in) {
		this.out = out;
		this.in = in;
	}


	@Override
	public void Handle(PacketHeader header) throws ConnectionLostException {
	}
	
	//TODO: maybe differentiate between server errors and client errors ?
	protected final IOException sendError(byte code){
		try {
			Packet p = PacketBuilder.newBuilder(PacketTypes.ERROR).writeByte(code).build();
			out.writeAndFlush(p);
		} catch (IOException e) {
			return e;
		}
		return null;
	}
}
