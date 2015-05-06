package org.hawk.net.websocket;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 握手回应协议
 * @author hawk
 */
public class HawkHandShakeResponse {
	/**
	 * 回复信息
	 */
	private String response;
	
	/**
	 * 构造函数
	 * @param response
	 */
    public HawkHandShakeResponse(String response){
        this.response = response;
    }
    
    /**
     * 获取回应信息
     * @return
     */
    public String getResponse(){
        return this.response;
    }
    
    /**
     * Web Socket handshake response go as a plain string.
     * @param response
     * @return
     */
    public IoBuffer getResponseBuffer() {                
        IoBuffer buffer = IoBuffer.allocate(response.getBytes().length, false);
        buffer.setAutoExpand(true);
        buffer.put(response.getBytes());
        buffer.flip();
        return buffer;
    }
}
