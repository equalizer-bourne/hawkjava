package com.hawk.game.util;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.hawk.os.HawkException;

import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.player.Player;

/**
 * 行为日志
 * 
 * @author hawk
 * @date 2014-7-24
 */
public class BehaviorLogger {
	/**
	 * 日志源
	 * 
	 * @author hawk
	 * @date 2014-7-25
	 */
	public static enum Source {
		/**
		 * 用户操作
		 */
		USER_OPERATION,
		/**
		 * GM操作
		 */
		GM_OPERATION,
		/**
		 * 未知源
		 */
		UNKNOWN_SOURCE;
	}

	/**
	 * 行为定义
	 * 
	 * @author hawk
	 * @date 2014-2-24
	 */
	public static enum Action {
		/**
		 * 无明显Action操作的行为
		 */
		NULL,
		/**
		 * 初始化数据
		 */
		INIT,
		/**
		 * 系统行为
		 */
		SYSTEM,
		/**
		 * 登录游戏
		 */
		LOGIN_GAME,
		/**
		 * 登出游戏
		 */
		LOGOUT_GAME,
		/**
		 * 每日重置
		 */
		DAILY_RESET,
		/**
		 * 充值
		 */
		RECHARGE,
		/**
		 * 未知行为
		 */
		UNKONWN_ACTION;

		/**
		 * 行为名
		 */
		private String actionName;

		/**
		 * 获取行为名
		 * 
		 * @return
		 */
		public String getActionName() {
			return actionName;
		}

		/**
		 * 构造函数
		 */
		private Action() {
			this("");
		}

		/**
		 * 构造函数
		 */
		private Action(String value) {
			this.actionName = value;
		}
	}

	/**
	 * 日志参数
	 * 
	 * @author hawk
	 * @date 2014-7-24
	 */
	public static class Params {
		private String name;
		private Object value;

		public static final String AFTER = "after";

		public static final String COST = "cost";

		public static Params valueOf(String name, Object value) {
			Params params = new Params();
			params.setName(name);
			params.setValue(value);
			return params;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}

	/**
	 * GM日志记录器
	 */
	private static final Logger GM_LOGGER = Logger.getLogger("GM");
	/**
	 * 行为日志，数据流变化日志记录器
	 */
	private static final Logger ACTION_LOGGER = Logger.getLogger("Action");

	/**
	 * 数据流统计，主要用户客服问题查询
	 * 
	 * @param player
	 * @param source
	 * @param action
	 */
	public static void log4Service(Player player, Source source, Action action, Params... params) {
		try {
			JSONObject jsonObject = new JSONObject();
			// 行为时间
			jsonObject.put("puid", player.getPuid());
			jsonObject.put("playerId", player.getId());
			jsonObject.put("playerName", player.getName());
			jsonObject.put("source", source.name());
			jsonObject.put("action", action.name());

			JSONObject paramsJsonObject = new JSONObject();
			for (Params param : params) {
				paramsJsonObject.put(param.getName(), param.getValue().toString());
			}
			jsonObject.put("data", paramsJsonObject);

			ACTION_LOGGER.info(jsonObject.toString());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 数据流统计，主要用户客服问题查询
	 * 
	 * @param player
	 * @param source
	 * @param action
	 */
	public static void log4Service(PlayerEntity playerEntity, Source source, Action action, Params... params) {
		try {
			JSONObject jsonObject = new JSONObject();
			// 行为时间
			jsonObject.put("puid", playerEntity.getPuid());
			jsonObject.put("playerId", playerEntity.getId());
			jsonObject.put("playerName", playerEntity.getName());
			jsonObject.put("source", source.name());
			jsonObject.put("action", action.name());

			JSONObject paramsJsonObject = new JSONObject();
			for (Params param : params) {
				paramsJsonObject.put(param.getName(), param.getValue().toString());
			}
			jsonObject.put("data", paramsJsonObject);

			ACTION_LOGGER.info(jsonObject.toString());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 以玩家id为key统计
	 * 
	 * @param playerId
	 * @param source
	 * @param action
	 * @param params
	 */
	public static void log4Service(int playerId, Source source, Action action, Params... params) {
		try {
			JSONObject jsonObject = new JSONObject();
			// 行为时间
			jsonObject.put("playerId", playerId);
			jsonObject.put("source", source.name());
			jsonObject.put("action", action.name());

			JSONObject paramsJsonObject = new JSONObject();
			for (Params param : params) {
				paramsJsonObject.put(param.getName(), param.getValue().toString());
			}
			jsonObject.put("data", paramsJsonObject);

			ACTION_LOGGER.info(jsonObject.toString());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * GM统计，主要记录GM操作
	 * 
	 * @param user
	 * @param source
	 * @param action
	 */
	public static void log4GM(String user, Source source, Action action, Params... params) {
		try {
			JSONObject jsonObject = new JSONObject();
			// 行为时间
			jsonObject.put("user", user);
			jsonObject.put("source", source.name());
			jsonObject.put("action", action.name());

			JSONObject paramsJsonObject = new JSONObject();
			for (Params param : params) {
				paramsJsonObject.put(param.getName(), param.getValue().toString());
			}
			jsonObject.put("data", paramsJsonObject);

			GM_LOGGER.info(jsonObject.toString());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
