package org.hawk.game;

import java.util.LinkedList;
import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

/**
 * 地图物体对象
 * 
 * @author hawk
 */
public abstract class HawkMapObject extends HawkAppObj {
	/**
	 * 所在游戏地图
	 */
	HawkGameMap gameMap;
	/**
	 * 位置
	 */
	HawkMapPos pos;
	/**
	 * 移动速度(ms/格子)
	 */
	int moveSpeed;
	/**
	 * 上次移动时间
	 */
	long lastMoveTime;
	/**
	 * 移动路径
	 */
	List<HawkMapPos> movePath;
	
	/**
	 * 构造函数
	 * 
	 * @param xid
	 */
	public HawkMapObject(HawkXID xid) {
		super(xid);
		pos = new HawkMapPos();
		movePath = new LinkedList<HawkMapPos>();
		moveSpeed = 0;
		lastMoveTime = 0;
	}

	/**
	 * 获取所在地图对象
	 * 
	 * @return
	 */
	public HawkGameMap getGameMap() {
		return gameMap;
	}
	
	/**
	 * 设置所在地图
	 * 
	 * @param gameMap
	 */
	public void setGameMap(HawkGameMap gameMap) {
		this.gameMap = gameMap;
	}
	
	/**
	 * 获取地图位置
	 * 
	 * @return
	 */
	public HawkMapPos getPos() {
		return pos;
	}

	/**
	 * 设置地图位置
	 * 
	 * @param pos
	 */
	public void setPos(HawkMapPos pos) {
		this.pos = pos;
	}
	
	/**
	 * 设置地图位置
	 * 
	 * @param x
	 * @param y
	 */
	public void setPos(int x, int y) {
		this.pos.setX(x).setY(y);
	}
	
	/**
	 * 获取移动速度
	 * 
	 * @return
	 */
	public int getMoveSpeed() {
		return moveSpeed;
	}

	/**
	 * 设置移动速度
	 * 
	 * @param moveSpeed
	 */
	public void setMoveSpeed(int moveSpeed) {
		this.moveSpeed = moveSpeed;
	}
	
	/**
	 * 开始移动
	 * 
	 * @param movePath
	 */
	public void startMove(List<HawkMapPos> movePath) {
		// 先停止移动
		if (isMoving()) {
			stopMove();
		}
		
		this.movePath = movePath;
		lastMoveTime = HawkTime.getMillisecond();
		// 广播开始移动
	}
	
	/**
	 * 停止移动
	 */
	public void stopMove() {
		this.movePath.clear();
		lastMoveTime = 0;
		// 广播停止移动
	}
	
	/**
	 * 是否在移动中
	 * 
	 * @return
	 */
	public boolean isMoving() {
		return movePath.size() > 0;
	}
	
	/**
	 * 帧更新
	 */
	@Override
	public boolean onTick() {
		if (isMoving()) {
			long curTime = HawkTime.getMillisecond();
			// 移动过一半即认可格子位移
			if ((curTime - lastMoveTime) * 2 >= moveSpeed) {
				pos = movePath.get(0);
				if (curTime - lastMoveTime >= moveSpeed) {
					movePath.remove(0);
					// 移动完整之后做一次位置同步
				}
			}
		}
		return super.onTick();
	}
}
