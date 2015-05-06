package com.hawk.game.util;

/**
 * 游戏常量定义
 * 
 * @author hawk
 */
public class GsConst {
	/**
	 * 对象类型
	 * 
	 * @author hawk
	 */
	public static class ObjType {
		// 玩家对象
		public static int PLAYER = 1;
		// 应用程序
		public static int MANAGER = 100;
	}

	/**
	 * 系统对象id
	 * 
	 * @author hawk
	 */
	public static class ObjId {
		// 应用程序
		public static int APP = 100;
	}

	/**
	 * 消息定义
	 */
	public static class MsgType {
		// 连接断开
		public static int SESSION_CLOSED = 2;
		// 玩家上线
		public static int PLAYER_LOGIN = 3;
		// 玩家初始化完成
		public static int PLAYER_ASSEMBLE = 4;
	}

	/**
	 * 模块定义
	 * 
	 * @author hawk
	 */
	public static class ModuleType {
		// 登陆模块
		public static int LOGIN_MODULE = 1;
		
		// 空闲模块(保证在最后)
		public static int IDLE_MODULE = 100;
	}
}
