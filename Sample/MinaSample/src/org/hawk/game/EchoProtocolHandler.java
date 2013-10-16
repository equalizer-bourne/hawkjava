package org.hawk.game;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public class EchoProtocolHandler extends IoHandlerAdapter {
	public void sessionCreated( IoSession session )
    {
		System.out.println("sessionCreated");
    }

    public void sessionOpened(IoSession session)
    {
    	System.out.println("sessionOpened");
    }
   
    public void sessionClosed(IoSession session)
    {
    	System.out.println("sessionClosed");
    }
    
    public void messageReceived(IoSession session, Object message)
    {	          
         session.write(message);
    }
}
