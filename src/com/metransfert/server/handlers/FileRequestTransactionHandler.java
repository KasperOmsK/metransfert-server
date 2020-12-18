package com.metransfert.server.handlers;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import com.metransfert.common.FileTransfert;
import com.metransfert.common.ErrorTypes;
import com.metransfert.server.FileServer;
import com.metransfert.server.exceptions.ConnectionLostException;
import com.packeteer.network.Packet;
import com.packeteer.network.PacketHeader;
import com.packeteer.network.PacketInputStream;
import com.packeteer.network.PacketOutputStream;
import com.packeteer.network.PacketUtils;

public class FileRequestTransactionHandler extends TransactionHandler {

	public FileRequestTransactionHandler(PacketOutputStream out, PacketInputStream in) {
		super(out, in);
	}
	
	@Override
	public void Handle(PacketHeader header) throws ConnectionLostException {
		String requestedID = null;
		Packet requestPacket = null;
		try {
			requestPacket = in.readPacket();
			ByteBuffer b = requestPacket.getPayloadBuffer();
			requestedID = PacketUtils.readNetworkString(b);
		} catch (IOException e) {
			throw new ConnectionLostException(e);
		}
		catch(BufferOverflowException e2){
			throw new ConnectionLostException(e2);
		}
		
		System.out.println("requested id " + requestedID);
		FileServer server = FileServer.instance();
		Path requestedFile = server.requestFile(requestedID);
		
		if(requestedFile == null){
			System.err.println("Le fichier demandé n'existe pas");
			sendError(ErrorTypes.FILE_DOES_NOT_EXIST);
			return;
		}
		
		FileTransfert ft = new FileTransfert(in, out);
		
		try {
			ft.upload(requestedFile.toFile());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error while uploading. FileRequestHandler should not use FileTransfert");
			throw new RuntimeException("FileRequestTransactionHandler-- Oops. This should not happen because we shouldn't use FileTransfert for upload");
		}
	}
}
