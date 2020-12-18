package com.metransfert.server.handlers;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import com.metransfert.common.ErrorTypes;
import com.metransfert.common.PacketTypes;
import com.metransfert.server.FileServer;
import com.metransfert.server.exceptions.ConnectionLostException;
import com.packeteer.network.Packet;
import com.packeteer.network.PacketBuilder;
import com.packeteer.network.PacketHeader;
import com.packeteer.network.PacketInputStream;
import com.packeteer.network.PacketOutputStream;
import com.packeteer.network.PacketUtils;

public class InfoRequestTransaction extends TransactionHandler{

	public InfoRequestTransaction(PacketOutputStream out, PacketInputStream in) {
		super(out, in);
	}
	
	@Override
	public void Handle(PacketHeader header) throws ConnectionLostException {
		Packet p = null;
		String reqId = null;
		try {
			p = in.readPacket();
			ByteBuffer b = p.getPayloadBuffer();
			reqId = PacketUtils.readNetworkString(b);
		} catch (IOException e) {
			throw new ConnectionLostException(e);
		}
		catch(BufferUnderflowException e2){
			throw new ConnectionLostException(e2);
		}
		
		Path file = FileServer.instance().requestFile(reqId);
		if(file == null){
			sendError(ErrorTypes.FILE_DOES_NOT_EXIST);
			System.err.println("Le fichier demandé n'existe pas");
			return;
		}
				
		sendReqInfoResult(file.getFileName().toString(), (int)file.toFile().length());
	}
	
	private void sendReqInfoResult(String filename, int fileSize) throws ConnectionLostException{
		Packet sendPacket = PacketBuilder.newBuilder(PacketTypes.INFORESULT)
				.writeInt(fileSize)
				.write(filename).build();
		
		try {
			out.writeAndFlush(sendPacket);
		} catch (IOException e) {
			throw new ConnectionLostException(e);
		}
	}

}
