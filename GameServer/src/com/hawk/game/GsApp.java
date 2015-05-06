package com.hawk.game;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigStorage;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.net.HawkSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjBase;
import org.hawk.obj.HawkObjManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkShutdownHook;
import org.hawk.os.HawkTime;
import org.hawk.util.services.HawkCdkService;
import org.hawk.util.services.HawkEmailService;
import org.hawk.util.services.HawkReportService;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.callback.ShutdownCallback;
import com.hawk.game.config.GrayPuidCfg;
import com.hawk.game.config.SysBasicCfg;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.HPHeartBeat;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.ProtoUtil;

/**
 * 游戏应用
 * 
 * @author hawk
 * 
 */
public class GsApp extends HawkApp {
	/**
	 * 日志记录器
	 */
	private static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * puid登陆时间
	 */
	private Map<String, Long> puidLoginTime;

	/**
	 * 全局静态对象
	 */
	private static GsApp instance = null;

	/**
	 * 获取全局静态对象
	 * 
	 * @return
	 */
	public static GsApp getInstance() {
		return instance;
	}

	/**
	 * 构造函数
	 */
	public GsApp() {
		super(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.APP));

		if (instance == null) {
			instance = this;
		}

		puidLoginTime = new ConcurrentHashMap<String, Long>();
	}

	/**
	 * 从配置文件初始化
	 * 
	 * @param cfg
	 * @return
	 */
	public boolean init(String cfg) {
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

		// 初始化对象管理区
		if (!initAppObjMan()) {
			return false;
		}

		// 设置关服回调
		HawkShutdownHook.getInstance().setCallback(new ShutdownCallback());

		// 初始化全局实例对象
		HawkLog.logPrintln("init server data......");
		ServerData.getInstance().init();

		// cdk服务初始化
		if (GsConfig.getInstance().getCdkHost().length() > 0) {
			HawkLog.logPrintln("install cdk service......");
			HawkCdkService.getInstance().install(GsConfig.getInstance().getGameId(), GsConfig.getInstance().getPlatform(), String.valueOf(GsConfig.getInstance().getServerId()), GsConfig.getInstance().getCdkHost(), GsConfig.getInstance().getCdkTimeout());
		}

		// 数据上报服务初始化
		if (GsConfig.getInstance().getReportHost().length() > 0) {
			HawkLog.logPrintln("install report service......");
			HawkReportService.getInstance().install(GsConfig.getInstance().getGameId(), GsConfig.getInstance().getPlatform(), String.valueOf(GsConfig.getInstance().getServerId()), GsConfig.getInstance().getReportHost(), GsConfig.getInstance().getReportTimeout());
		}

		// 初始化邮件服务
		if (GsConfig.getInstance().getEmailUser().length() > 0) {
			HawkLog.logPrintln("install email service......");
			HawkEmailService.getInstance().install("smtp.163.com", 25, GsConfig.getInstance().getEmailUser(), GsConfig.getInstance().getEmailPwd());
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
		objMan.setObjTimeout(SysBasicCfg.getInstance().getPlayerCacheTime());

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
			// 数据上报
			try {
				HawkReportService.getInstance().onTick();
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			// 显示服务器信息
			ServerData.getInstance().showServerInfo();
			return true;
		}
		return false;
	}

	/**
	 * 清空玩家缓存
	 */
	@Override
	protected void onRemoveTimeoutObj(HawkAppObj appObj) {
		if (appObj != null && appObj instanceof Player) {
			Player player = (Player) appObj;
			if (player.isOnline()) {
				player.getSession().close(false);
			}

			if (player.getPlayerData() != null && player.getPlayerData().getPlayerEntity() != null) {
				logger.info("remove player: {}, puid: {}, gold: {}, coin: {}, level: {}, vip: {}", player.getId(), player.getPuid(), player.getGold(), player.getCoin(), player.getLevel(), player.getVipLevel());
			}
		}
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
			appObj = new Player(xid);
		}

		if (appObj == null) {
			HawkLog.errPrintln("create obj failed: " + xid);
		}
		return appObj;
	}

	/**
	 * 分发消息
	 */
	@Override
	public boolean dispatchMsg(HawkXID xid, HawkMsg msg) {
		if (xid.equals(getXid())) {
			return onMessage(msg);
		}
		return super.dispatchMsg(xid, msg);
	}

	/**
	 * 报告异常
	 */
	@Override
	public void reportException(Exception e) {
		HawkEmailService emailService = HawkEmailService.getInstance();
		if (emailService != null) {
			String emailTitle = String.format("exception(%s_%s_%d)", GsConfig.getInstance().getGameId(), GsConfig.getInstance().getPlatform(), GsConfig.getInstance().getServerId());
			emailService.sendEmail(emailTitle, HawkException.formatStackMsg(e), Arrays.asList("daijunhua@com4loves.com"));
		}
	}

	/**
	 * 会话协议回调, 由IO线程直接调用, 非线程安全
	 */
	@Override
	public boolean onSessionProtocol(HawkSession session, HawkProtocol protocol) {
		// 协议解密
		protocol = ProtoUtil.decryptionProtocol(session, protocol);
		if (protocol == null) {
			return false;
		}

		long protoTime = HawkTime.getMillisecond();
		try {
			// 心跳协议直接处理
			if (protocol.checkType(HP.sys.HEART_BEAT)) {
				HPHeartBeat.Builder builder = HPHeartBeat.newBuilder();
				builder.setTimeStamp(HawkTime.getSeconds());
				protocol.getSession().sendProtocol(HawkProtocol.valueOf(HP.sys.HEART_BEAT, builder));
				return true;
			}

			try {
				if (session.getAppObject() == null) {
					// 登陆协议
					if (protocol.checkType(HP.code.LOGIN_C)) {
						String puid = protocol.parseProtocol(HPLogin.getDefaultInstance()).getPuid().trim().toLowerCase();
						if (!checkPuidValid(puid, session)) {
							return true;
						}

						// 登陆协议时间间隔控制
						synchronized (puidLoginTime) {
							if (puidLoginTime.containsKey(puid) && HawkTime.getMillisecond() <= puidLoginTime.get(puid) + 5000) {
								return true;
							}
							puidLoginTime.put(puid, HawkTime.getMillisecond());
						}

						if (!preparePuidSession(puid, session)) {
							return false;
						}
					} else {
						HawkLog.errPrintln("session appobj null cannot process unlogin protocol: " + protocol.getType());
						return false;
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
				return false;
			}
			return super.onSessionProtocol(session, protocol);
		} finally {
			protoTime = HawkTime.getMillisecond() - protoTime;
			if (protoTime >= 20) {
				logger.info("protocol cost time exception, protocolId: {}, costTime: {}ms", protocol.getType(), protoTime);
			}
		}
	}

	/**
	 * 会话关闭回调
	 */
	@Override
	public void onSessionClosed(HawkSession session) {
		if (session != null && session.getAppObject() != null) {
			HawkXID xid = session.getAppObject().getXid();
			if (xid != null && xid.isValid()) {
				postMsg(HawkMsg.valueOf(GsConst.MsgType.SESSION_CLOSED, xid));
			}
		}
		super.onSessionClosed(session);
	}

	/**
	 * 检测灰度账号
	 * 
	 * @param puid
	 * @return
	 */
	private boolean checkPuidValid(String puid, HawkSession session) {
		int grayState = GsConfig.getInstance().getGrayState();
		// 灰度状态下, 限制灰度账号
		if (grayState > 0) {
			GrayPuidCfg grayPuid = HawkConfigManager.getInstance().getConfigByKey(GrayPuidCfg.class, puid);
			if (grayPuid == null) {
				session.sendProtocol(ProtoUtil.genErrorProtocol(HP.code.LOGIN_C_VALUE, Status.error.SERVER_GRAY_STATE_VALUE, 1));
				return false;
			}
		}

		int playerId = ServerData.getInstance().getPlayerIdByPuid(puid);
		if (playerId == 0) {
			// 注册人数达到上限
			int registerMaxSize = GsConfig.getInstance().getRegisterMaxSize();
			if (registerMaxSize > 0 && ServerData.getInstance().getRegisterPlayer() >= registerMaxSize) {
				session.sendProtocol(ProtoUtil.genErrorProtocol(HP.code.LOGIN_C_VALUE, Status.error.REGISTER_MAX_LIMIT_VALUE, 1));
				return false;
			}
		}
		return true;
	}

	/**
	 * 准备puid对应的会话
	 * 
	 * @param puid
	 * @return
	 */
	private boolean preparePuidSession(String puid, HawkSession session) {
		int playerId = ServerData.getInstance().getPlayerIdByPuid(puid);
		if (playerId == 0) {
			PlayerEntity playerEntity = new PlayerEntity(puid, "", "", "");
			if (!HawkDBManager.getInstance().create(playerEntity)) {
				return false;
			}
			playerId = playerEntity.getId();
			ServerData.getInstance().addPuidAndPlayerId(puid, playerId);

			logger.info("create player entity: {}, puid: {}", playerId, puid);
		}

		HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
		HawkObjBase<HawkXID, HawkAppObj> objBase = lockObject(xid);
		try {
			// 对象不存在即创建
			if (objBase == null || !objBase.isObjValid()) {
				objBase = createObj(xid);
				if (objBase != null) {
					objBase.lockObj();
				}

				logger.info("create player: {}, puid: {}", playerId, puid);
			}

			// 会话绑定应用对象
			if (objBase != null) {
				// 已存在会话的情况下, 踢出玩家
				Player player = (Player) objBase.getImpl();
				if (player != null && player.getSession() != null && player.getSession() != session) {
					player.kickout(Const.kickReason.DUPLICATE_LOGIN_VALUE);
				}

				// 绑定会话对象
				session.setAppObject(objBase.getImpl());
			}
		} finally {
			if (objBase != null) {
				objBase.unlockObj();
			}
		}
		return true;
	}
}
