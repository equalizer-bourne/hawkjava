package com.hawk.game;

import java.util.Collection;
import java.util.LinkedList;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigStorage;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.HawkMsgManager;
import org.hawk.net.HawkSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjBase;
import org.hawk.obj.HawkObjManager;
import org.hawk.os.HawkException;
import org.hawk.xid.HawkXID;

import com.hawk.game.config.GsConfig;
import com.hawk.game.player.GsPlayer;
import com.hawk.game.util.GsConst;

/**
 * 游戏应用
 * 
 * @author hawk
 * 
 */
public class GsApp extends HawkApp {
	/**
	 * 对象xid列表
	 */
	Collection<HawkXID> objXidList;

	/**
	 * 构造函数
	 */
	public GsApp() {
		super(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.APP));
		
		// 初始化数据
		objXidList = new LinkedList<HawkXID>();
	}

	/**
	 * 从配置文件初始化
	 * 
	 * @param cfg
	 * @return
	 */
	public boolean init(String cfg) {
		// 初始化对象管理区
		if (!initAppObjMan()) {
			return false;
		}
		
		GsConfig appCfg = null;
		try {
			HawkConfigStorage cfgStorgae = new HawkConfigStorage(GsConfig.class);
			appCfg = (GsConfig) cfgStorgae.getConfigList().get(0);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		
		// 父类初始化
		if (!super.init(appCfg)) {
			return false;
		}
				
		return true;
	}

	/**
	 * 初始化应用对象管理器
	 * 
	 * @return
	 */
	private boolean initAppObjMan() {
		HawkObjManager<HawkXID, HawkAppObj> objMan = null;

		// 创建玩家管理区
		objMan = createObjMan(GsConst.ObjType.PLAYER);

		// 创建全局管理器, 并注册应用对象
		objMan = createObjMan(GsConst.ObjType.MANAGER);
		objMan.allocObject(getXid(), this);

		return true;
	}

	/**
	 * 帧更新
	 */
	@Override
	public boolean onTick() {
		if (super.onTick()) {
			// 定时更新对象
			tickAppObjs(GsConst.ObjType.PLAYER);
			tickAppObjs(GsConst.ObjType.MANAGER);
			return true;
		}
		return false;
	}

	/**
	 * 更新指定的对象管理区
	 * 
	 * @return
	 */
	private boolean tickAppObjs(int objType) {
		HawkObjManager<HawkXID, HawkAppObj> objMan = getObjMan(objType);
		if (objMan != null) {
			objXidList.clear();
			if (objMan.collectObjKey(objXidList, null) > 0) {
				postTick(objXidList);
			}
			return true;
		}
		return false;
	}

	/**
	 * 应用对象消息处理
	 */
	@Override
	public boolean onMessage(HawkMsg msg) {
		// 对象释放
		if (msg.getMsg() == GsConst.MsgType.DELETE_OBJ) {
			HawkXID xid = msg.getParam(0);
			deleteObj(xid);
			return true;
		}
		return false;
	}

	/**
	 * 创建应用对象
	 */
	@Override
	protected HawkAppObj onCreateObj(HawkXID xid) {
		HawkAppObj appObj = null;
		// 创建管理器
		if (xid.getType() == GsConst.ObjType.MANAGER) {

		} else if (xid.getType() == GsConst.ObjType.PLAYER) {
			appObj = new GsPlayer(xid);
		}

		if (appObj == null) {
			HawkLog.errPrintln("create obj failed: " + xid);
		}
		return appObj;
	}

	/**
	 * 投递协议
	 */
	@Override
	public boolean postProtocol(HawkXID xid, HawkProtocol protocol) {
		return super.postProtocol(xid, protocol);
	}

	/**
	 * 协议消息
	 */
	@Override
	public boolean dispatchProto(HawkXID xid, HawkProtocol protocol) {
		return super.dispatchProto(xid, protocol);
	}

	/**
	 * 分发消息
	 */
	@Override
	public boolean dispatchMsg(HawkXID xid, HawkMsg msg) {
		if (xid.getType() == GsConst.ObjType.MANAGER && xid.getId() == GsConst.ObjId.APP) {
			return onMessage(msg);
		}
		return super.dispatchMsg(xid, msg);
	}

	/**
	 * 会话协议回调, 由IO线程直接调用, 非线程安全
	 */
	@Override
	public boolean onSessionProtocol(HawkSession session, HawkProtocol protocol) {
		if (session.getAppObject() == null) {
			HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, session.hashCode());
			HawkObjBase<HawkXID, HawkAppObj> objBase = lockObject(xid);
			try {
				if (objBase == null || !objBase.isObjValid()) {
					HawkAppObj appObj = createObj(xid);
					session.setAppObject(appObj);
				}
			} finally {
				if (objBase != null) {
					objBase.unlockObj();
				}
			}
		}
		return super.onSessionProtocol(session, protocol);
	}

	/**
	 * 会话关闭回调
	 */
	@Override
	public void onSessionClosed(HawkSession session) {
		if (session != null && session.getAppObject() != null) {
			HawkXID xid = session.getAppObject().getXid();
			if (xid != null && xid.isValid()) {
				HawkMsg msg = HawkMsgManager.getInstance().create(GsConst.MsgType.SESSION_CLOSED, xid);
				postMsg(msg);
			}
		}
		super.onSessionClosed(session);
	}
}
