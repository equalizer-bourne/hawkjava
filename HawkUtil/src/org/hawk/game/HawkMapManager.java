package org.hawk.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.app.HawkApp;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.util.HawkTickable;

public class HawkMapManager extends HawkTickable {
	/**
	 * 游戏地图列表
	 */
	Map<Integer, HawkGameMap> gameMaps;
	
	/**
	 * 单例使用
	 */
	static HawkMapManager instance;
	
	/**
	 * 获取全局管理器
	 * 
	 * @return
	 */
	public static HawkMapManager getInstance() {
		if (instance == null) {
			instance = new HawkMapManager();
		}
		return instance;
	}

	/**
	 * 默认构造函数
	 */
	private HawkMapManager() {
		gameMaps = new HashMap<Integer, HawkGameMap>();
		HawkApp.getInstance().addTickable(this);
	}
	
	/**
	 * 向地图管理器添加地图
	 * 
	 * @param mapId
	 * @param map
	 * @return
	 */
	public boolean addGameMap(int mapId, HawkGameMap map) {
		if (mapId > 0 && map != null && !gameMaps.containsKey(mapId)) {
			gameMaps.put(mapId, map);
			return true;
		}
		return false;
	}
	
	/**
	 * 获取地图对象
	 * 
	 * @param mapId
	 * @return
	 */
	public HawkGameMap getGameMap(int mapId) {
		return gameMaps.get(mapId);
	}

	/**
	 * 在地图上广播消息
	 * 
	 * @param mapId
	 * @param msg
	 */
	public void broadcastMsg(int mapId, HawkMsg msg) {
		HawkGameMap gameMap = getGameMap(mapId);
		if (gameMap != null) {
			gameMap.broadcastMsg(msg);
		}
	}
	
	/**
	 * 在地图上广播协议
	 * 
	 * @param mapId
	 * @param protocol
	 */
	public void broadcastProtocol(int mapId, HawkProtocol protocol) {
		HawkGameMap gameMap = getGameMap(mapId);
		if (gameMap != null) {
			gameMap.broadcastProtocol(protocol);
		}
	}
	
	/**
	 * 地图管理器更新
	 */
	@Override
	public void onTick() {
		for (Entry<Integer, HawkGameMap> entry : gameMaps.entrySet()) {
			entry.getValue().onTick();
		}
	}
}
