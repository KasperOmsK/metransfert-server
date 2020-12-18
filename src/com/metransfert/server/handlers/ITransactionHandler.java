package com.metransfert.server.handlers;

import com.metransfert.server.exceptions.ConnectionLostException;
import com.packeteer.network.PacketHeader;

public interface ITransactionHandler {
	public void Handle(PacketHeader header) throws ConnectionLostException;
}
