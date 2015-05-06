package com.hawk.game.player;

import java.util.List;

import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;

import com.hawk.game.ServerData;
import com.hawk.game.entity.PlayerEntity;

/**
 * 管理所有玩家数据集合
 * 
 * @author hawk
 * 
 */
public class PlayerData {
	/**
	 * 玩家对象
	 */
	private Player player = null;

	/**
	 * 玩家基础数据
	 */
	private PlayerEntity playerEntity = null;
	
	/**
	 * 构造函数
	 * 
	 * @param player
	 */
	public PlayerData(Player player) {
		this.player = player;
	}

	/**
	 * 获取数据对应玩家对象
	 * 
	 * @return
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * 获取玩家数据实体
	 * 
	 * @return
	 */
	public PlayerEntity getPlayerEntity() {
		return playerEntity;
	}

	/**
	 * 设置玩家数据实体
	 * 
	 * @param playerEntity
	 */
	public void setPlayerEntity(PlayerEntity playerEntity) {
		this.playerEntity = playerEntity;
	}
	
	/**********************************************************************************************************
	 * 数据db操作区
	 **********************************************************************************************************/
	/**
	 * 加载玩家信息
	 * 
	 * @return
	 */
	public PlayerEntity loadPlayer(String puid) {
		if (playerEntity == null) {
			List<PlayerEntity> playerEntitys = HawkDBManager.getInstance().query("from PlayerEntity where puid = ? and invalid = 0", puid);
			if (playerEntitys != null && playerEntitys.size() > 0) {
				playerEntity = playerEntitys.get(0);
				try {
					if (playerEntity.getSilentTime() != null && playerEntity.getSilentTime().getTime() > HawkTime.getMillisecond()) {
						ServerData.getInstance().addDisablePhone(playerEntity.getPhoneInfo());
					}
				} catch (Exception e) {
				}
			}
		}
		return playerEntity;
	}
}
