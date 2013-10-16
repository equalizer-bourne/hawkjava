package org.hawk.game;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class MinaMain {
	
	public static void main(String[] args) {
		try {
			NioSocketAcceptor acceptor = new NioSocketAcceptor(5);
			acceptor.setHandler(new EchoProtocolHandler());
			acceptor.bind(new InetSocketAddress(9009));
	        System.out.println("server start......");
        } catch (IOException e) {
			e.printStackTrace();
		}
	}
}

