package com.metransfert.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.metransfert.server.exceptions.ConfigurationParseException;

public class ServerApplication {
	
	public static void main(String[] args) {
		System.out.println("Server start");
		
		Path userDir = Paths.get(System.getProperty("user.dir"));
		
		ServerConfiguration config = null;
		try{
			config = ServerConfiguration.loadFromFile(userDir.resolve("server.properties"));
		}catch (ConfigurationParseException | FileNotFoundException e){
			System.err.println("Could not load configuration from file");
			config = defaultConfig();
		}
		
		if(validate_config(config) == false){
			throw new RuntimeException("Config file is not valid");
		}
		
		System.out.println("======================================");
		System.out.println("Starting server with config : ");
		System.out.printf("[Network]\n\tPort : %d\n", config.serverPort());
		System.out.printf("[Server]\n\troot : %s\n", config.rootDirectory());
		System.out.printf("\tstore : %s\n", config.storeDirectory());
		System.out.printf("\tlease time : %d s\n", config.defaultLeaseTime());
		System.out.printf("\tID length: %d\n", config.IDLength());
		System.out.println("======================================");
			
		try {
			FileServer s = new FileServer(config);
			s.start();
			s.join();
		} catch (IOException e) {
			System.out.println("Could not create server...");
			e.printStackTrace();	
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static ServerConfiguration defaultConfig() { //TODO : create a default server.properties file
		Path defaultRoot = Paths.get(System.getProperty("user.dir"));
		Path defaultStore = defaultRoot.resolve("store");
		String charSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
		return new ServerConfiguration(defaultRoot, defaultStore, 300, 5, 7999, charSet);
	}

	private static boolean validate_config(ServerConfiguration config) {
		return true; //TODO: vraiment vérifier la config
	}
}
