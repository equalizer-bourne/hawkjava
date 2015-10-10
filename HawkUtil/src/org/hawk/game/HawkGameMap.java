package org.hawk.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.xid.HawkXID;

public class HawkGameMap {
	/**
	 * 地图上的对象列表
	 */
	Map<HawkXID, HawkMapObject> mapObjects;
	
	public HawkGameMap() {
		mapObjects = new HashMap<HawkXID, HawkMapObject>();
	}
	
	/**
	 * 添加对象
	 * 
	 * @param xid
	 * @param object
	 */
	protected void addObject(HawkXID xid, HawkMapObject object) {
		object.setGameMap(this);
		mapObjects.put(xid, object);
	}
	
	/**
	 * 移除对象
	 * 
	 * @param xid
	 */
	protected void removeObject(HawkXID xid) {
		HawkMapObject mapObject = mapObjects.remove(xid);
		if (mapObject != null) {
			mapObject.setGameMap(null);
		}
	}
	
	/**
	 * 获取地图对象
	 * 
	 * @param xid
	 * @return
	 */
	protected HawkMapObject getObject(HawkXID xid) {
		return mapObjects.get(xid);
	}
	
	/**
	 * 在地图上广播消息
	 * 
	 * @param mapId
	 * @param msg
	 */
	protected synchronized void broadcastMsg(HawkMsg msg) {
		
	}
	
	/**
	 * 在地图上广播协议
	 * 
	 * @param mapId
	 * @param protocol
	 */
	protected synchronized void broadcastProtocol(HawkProtocol protocol) {
		
	}
	
	/**
	 * 更新
	 */
	protected void onTick() {
		for (Entry<HawkXID, HawkMapObject> entry : mapObjects.entrySet()) {
			entry.getValue().onTick();
		}
	}
}
