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
		// 应用程序
		public static int MANAGER = 1;
		// 玩家对象
		public static int PLAYER = 2;
	}

	/**
	 * 系统对象id
	 * 
	 * @author hawk
	 */
	public static class ObjId {
		// 应用程序
		public static int APP = 1;
	}

	/**
	 * 消息定义
	 */
	public static class MsgType {
		// 删除对象
		public static int DELETE_OBJ = 1;
		// 连接断开
		public static int SESSION_CLOSED = 2;
	}

	/**
	 * 模块定义
	 * 
	 * @author hawk
	 */
	public static class ModuleType {
		// 登陆模块
		public static int LOGIN_MODULE = 1;
	}
}
