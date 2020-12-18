package com.metransfert.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import com.metransfert.common.ErrorTypes;
import com.metransfert.common.PacketTypes;
import com.metransfert.server.exceptions.ConnectionLostException;
import com.metransfert.server.handlers.FileRequestTransactionHandler;
import com.metransfert.server.handlers.FileTransactionHandler;
import com.metransfert.server.handlers.ITransactionHandler;
import com.metransfert.server.handlers.InfoRequestTransaction;
import com.metransfert.server.handlers.PingHandler;
import com.packeteer.network.PacketHeader;
import com.packeteer.network.PacketInputStream;
import com.packeteer.network.PacketOutputStream;

public class ClientProcessor extends Thread {
	
	private Socket socket;
	
	private PacketInputStream in;
	private PacketOutputStream out;
	private HashMap<Byte, ITransactionHandler> handlers = new HashMap<Byte, ITransactionHandler>();
	
	private ArrayList<Runnable> threadFinishListeners = new ArrayList<>();
	
	public ClientProcessor(Socket sock) throws IOException{
		
		// ===== fields initialisation ========
		this.socket = sock;
		
		this.in = new PacketInputStream(new BufferedInputStream(sock.getInputStream()));
		this.out = new PacketOutputStream(new BufferedOutputStream(sock.getOutputStream()));
		
		// ======== populating handlers map 
		
		handlers.put(PacketTypes.FILEUPLOAD, new FileTransactionHandler(out, in) );
		
		handlers.put(PacketTypes.REQFILE, new FileRequestTransactionHandler(out, in));
		
		handlers.put(PacketTypes.REQINFO, new InfoRequestTransaction(out, in));
		
		handlers.put(PacketTypes.PING, new PingHandler(out, in));
	
	}
	
	@Override
	public void run() {
		System.out.println("Début transaction client");
		
		//init some stuff ?
		
		//begin reading network 
		try{
			PacketHeader header = in.peekHeader();
			
			//TODO: validate packet
			
			ITransactionHandler h = handlers.get(header.type);
			if(h != null){
				h.Handle(header);
			}
			else{
				//invalid packet type
				System.err.println("No handler for packet type : " + header.type);
				out.writeAndFlush(new ErrorPacket(ErrorTypes.INVALID_REQUEST));
			}	
		}
		catch(IOException | ConnectionLostException e){
			//erreur de communication, la connection a été fermée ?
			System.err.println("Connection closed abruptly");
			//e.printStackTrace();
		}
		finally{
			System.out.println("Closing socket");
			try {
				socket.shutdownOutput();
				socket.close();
			} catch (IOException e) {
				System.err.println("Error while closing socket : ");
				e.printStackTrace();
			}
		}
		
		//finalize 
		
		//notify listeners
		for(int i=0; i<threadFinishListeners.size(); i++){
			threadFinishListeners.get(i).run();
		}
		
		System.out.println("Fin transaction client");
	}

	//listeners
	void addThreadFinishListener(Runnable r){
		this.threadFinishListeners.add(r);
	}
	
	void removeThreadFinishListeners(Runnable r){
		this.threadFinishListeners.remove(r);
	}
}
