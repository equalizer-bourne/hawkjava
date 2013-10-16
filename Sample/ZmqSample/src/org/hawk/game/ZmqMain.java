package org.hawk.game;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertArrayEquals;

public class ZmqMain {
	
	public static void runRouterDealer() {
		ZMQ.Context context = ZMQ.context(1);
		
		ZMQ.Socket routerSock = context.socket(ZMQ.ROUTER);
		routerSock.setLinger(0);
        if (routerSock != null) {
        	routerSock.bind("tcp://127.0.0.1:7000");
        	byte[] identity = new String("routerSock").getBytes();
        	routerSock.setIdentity(identity);
        	byte[] fetchId = routerSock.getIdentity();
        	System.out.println("routerSock Id: " + new String(fetchId));
        }
        
        ZMQ.Socket dealerSock = context.socket(ZMQ.DEALER);
        dealerSock.setLinger(0);
        if (dealerSock != null) {
        	dealerSock.connect("tcp://127.0.0.1:7000");
        	byte[] identity = new String("dealerSock").getBytes();
        	dealerSock.setIdentity(identity);
        	byte[] fetchId = dealerSock.getIdentity();
        	System.out.println("dealerSock Id: " + new String(fetchId));
        }
        
        dealerSock.send(new String("first msg"), ZMQ.SNDMORE);
        dealerSock.send(new String("second msg"), ZMQ.SNDMORE);
        dealerSock.send(new String("third msg"));
        
        byte[] identity = routerSock.recv();
        System.out.println("len: " + identity.length + ", msg: " + new String(identity));
        while (routerSock.hasReceiveMore()) {
        	byte[] msg = routerSock.recv();
        	System.out.println("len: " + msg.length + ", msg: " + new String(msg));
        }
        
        routerSock.send(identity, ZMQ.SNDMORE);
        routerSock.send(new String("recv ok"));
        
        byte[] resp = dealerSock.recv();
        System.out.println("len: " + resp.length + ", msg: " + new String(resp));
        while (dealerSock.hasReceiveMore()) {
        	byte[] msg = dealerSock.recv();
        	System.out.println("len: " + msg.length + ", msg: " + new String(msg));
        }
        
        dealerSock.close();
        routerSock.close();
        
        context.term();
	}
	
	public static void runPushPull() {
		ZMQ.Context context   = ZMQ.context(1);
		
		ZMQ.Socket pullSock = context.socket(ZMQ.PULL);
		pullSock.setLinger(0);
        if (pullSock != null) {
        	pullSock.bind("tcp://127.0.0.1:7001");
        	byte[] identity = new String("pullSock").getBytes();
        	pullSock.setIdentity(identity);
        	byte[] fetchId = pullSock.getIdentity();
        	System.out.println("pullSock Id: " + new String(fetchId));
        }
        
        ZMQ.Socket pushSock = context.socket(ZMQ.PUSH);
        pushSock.setLinger(0);
        if (pushSock != null) {
        	pushSock.connect("tcp://127.0.0.1:7001");
        	byte[] identity = new String("pushSock").getBytes();
        	pushSock.setIdentity(identity);
        	byte[] fetchId = pushSock.getIdentity();
        	System.out.println("pushSock Id: " + new String(fetchId));
        }
        
        pushSock.send(new String("first msg"), ZMQ.SNDMORE);
        pushSock.send(new String("second msg"), ZMQ.SNDMORE);
        pushSock.send(new String("third msg"));
        
        byte[] msg = pullSock.recv();
        System.out.println("len: " + msg.length + ", msg: " + new String(msg));
        while (pullSock.hasReceiveMore()) {
        	msg = pullSock.recv();
        	System.out.println("len: " + msg.length + ", msg: " + new String(msg));
        }
        
        pushSock.close();
        pullSock.close();
		context.term();
	}
	
	public static void main(String[] args) {
		System.out.println("ZMQ-Version: " + ZMQ.getVersionString());
		
		runRouterDealer();
        
		runPushPull();
		
        System.out.println("ZMQ-Version: " + ZMQ.getVersionString());
	}
}
