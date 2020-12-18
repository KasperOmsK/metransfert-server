package com.metransfert.server.handlers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.metransfert.common.ErrorTypes;
import com.metransfert.common.PacketTypes;
import com.metransfert.server.FileServer;
import com.metransfert.server.Store;
import com.metransfert.server.exceptions.ConnectionLostException;
import com.packeteer.network.Packet;
import com.packeteer.network.PacketBuilder;
import com.packeteer.network.PacketHeader;
import com.packeteer.network.PacketInputStream;
import com.packeteer.network.PacketOutputStream;
import com.packeteer.network.PacketUtils;

public class FileTransactionHandler extends TransactionHandler {
	
	private FileServer myServer;
	
	public FileTransactionHandler(PacketOutputStream out, PacketInputStream in) {
		super(out, in);
		this.myServer = FileServer.instance();
	}

	//TODO: refactor this garbage...
	@Override
	public void Handle(PacketHeader header) throws ConnectionLostException {
		
		int read = 0;
		boolean sucess = true;
		
		String incoming_filename = null;
		try {
			in.readHeader(); //skip the header
			incoming_filename = in.readString();
		} catch (IOException e1) {
			throw new ConnectionLostException(e1);
		}
		read += PacketUtils.calculateNetworkStringLength(incoming_filename);
		
		if(!validateFileName(incoming_filename)){
			System.err.println(incoming_filename + " is not a valid filename.");
			sendError(ErrorTypes.INVALID_FILENAME);
			return;
		}
		
		Store newStore = this.myServer.allocateStore();		
		
		Path newFile = newStore.path.resolve(incoming_filename);
		FileOutputStream fileStream = null;
		
		try{
			Files.createFile(newFile);
			fileStream = new FileOutputStream(newFile.toFile());
			
			int packetLen = header.payloadLength;
			
			byte[] buffer = new byte[10*1024];
			int count = 0;		
			while(read < packetLen){
				count = in.read(buffer);
				read += count;
				fileStream.write(buffer, 0, count);
			}
		}catch(FileNotFoundException e1){
			System.err.println("Newly created file does not exist ? This is not normally possible... Something went very wrong");
			sendError(ErrorTypes.SERVER_ERROR);
			sucess = false;
			e1.printStackTrace();
		}
		catch(FileAlreadyExistsException e3){
			System.err.println("file " + newStore.ID + "/" + newFile.getFileName() + " alread exists... wtf ?");
			sendError(ErrorTypes.SERVER_ERROR);
			sucess = false;
			e3.printStackTrace();
		}
		catch(IOException e2){
			sucess = false;
								
			//if network error : throw ConnectionLostException
			if(isNetworkException(e2) == true){
				System.err.println("Connection exception");
				throw new ConnectionLostException(e2);
			}
			else{ //if file error : send an error to the client
				System.err.println("server error");
				sendError(ErrorTypes.SERVER_ERROR);
			}
			e2.printStackTrace();
		}
		finally{
			try {				
				if(fileStream != null)
					fileStream.close();
				//notify server the store is invalid
				if(sucess == false)
					myServer.invalidateStore(newStore); 
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//it worked ! tell the server the store should start
		newStore.start();
		
	
		//send response
		Packet resp = new PacketBuilder(PacketTypes.UPLOADRESULT).write(newStore.ID).build();
		try {
			out.writeAndFlush(resp);
		} catch (IOException e) {
			throw new ConnectionLostException(e);
		}
	}
	
	
	private boolean isNetworkException(IOException e2) {
		if(e2 instanceof SocketException)
			return true;
		
		return false;
	}

	private boolean validateFileName(String incoming_filename) {
		if(incoming_filename.contains("test_invalid_filename")) //TODO : implement a real filename validation
			return false;
		
		return true;
	}

}
