package com.metransfert.server.exceptions;

public class ConnectionLostException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2615332801085691834L;
	
	public ConnectionLostException(Throwable cause){
		super(cause);
	}
	
}
