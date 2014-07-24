package com.hawk.cdk;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.danga.MemCached.MemCachedClient;
import com.hawk.cdk.data.CdkInfo;
import com.hawk.cdk.data.CdkTypeReward;
import com.hawk.cdk.http.param.AppendCdkParam;
import com.hawk.cdk.http.param.DelCdkParam;
import com.hawk.cdk.http.param.DelTypeParam;
import com.hawk.cdk.http.param.GenCdkParam;
import com.hawk.cdk.http.param.QueryCdkParam;
import com.hawk.cdk.http.param.QueryTypeParam;
import com.hawk.cdk.http.param.ResetRewardParam;
import com.hawk.cdk.http.param.UseCdkParam;
import com.hawk.cdk.util.CdkUtil;

/**
 * CDK服务接口
 * 
 * @author hawk
 */
public class CdkServices {
	private static Logger logger = LoggerFactory.getLogger("CdkReward");

	// 前端访问状态码
	public static final int CDK_STATUS_OK = 0; // 可使用
	public static final int CDK_STATUS_NONEXIST = 1; // 不存在
	public static final int CDK_STATUS_FUTURE = 2; // 时间未到
	public static final int CDK_STATUS_PAST = 3; // 已过期
	public static final int CDK_STATUS_USED = 4; // 已使用
	public static final int CDK_STATUS_PLAT = 5; // 平台不正确

	// 后台操作错误码
	public static final int CDK_TYPE_EXIST = 10; // CDK类型已存在
	public static final int CDK_PARAM_ERROR = 11; // 参数错误
	public static final int CDK_TYPE_NONEXIST = 12; // 类型不存在

	// 常量定义
	public static final int CDK_TOTAL_LENGTH = 16; // CDK长度
	public static final int CDK_DIGIT_MIN_COUNT = 3; // 数字最少个数
	public static final int CDK_NAMT_TYPE_LEN = 8; // 名字和类型占用的长度
	public static final int CDK_CHAR_MIN_COUNT = 3; // 字符最少个数
	public static final int CDK_HEADER_SIZE = 2; // cdk标记字节

	// 定义memcached访问关键字
	private static String MC_CDK_AUTHOR_KEY = "org.hawk.cdk.author";
	private static String MC_CDK_TYPES_KEY = "org.hawk.cdk.types";
	private static String MC_CDK_DATA_FMT = "org.hawk.cdk.datas.%s";
	private static String VALID_CDK_KEYS = "0123456789abcdefghijklmnopqrstuvwxyz";

	/**
	 * 运行状态
	 */
	volatile boolean running = true;
	/**
	 * 上次的通信时间
	 */
	private int lastVisitTime = 0;
	/**
	 * 读写锁
	 */
	private Lock memCachedLock = null;
	/**
	 * MC客户端连接表
	 */
	private List<MemCachedClient> memCachedClients = null;
	/**
	 * 类型奖励
	 */
	private Map<String, CdkTypeReward> typeRewardMap = null;
	/**
	 * 单例实例对象
	 */
	private static CdkServices instance = null;

	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static CdkServices getInstance() {
		if (instance == null) {
			instance = new CdkServices();
		}
		return instance;
	}

	/**
	 * 初始化
	 */
	public void initMC() {
		if (memCachedLock == null) {
			memCachedLock = new ReentrantLock();
		}

		if (memCachedClients == null) {
			memCachedClients = new LinkedList<MemCachedClient>();
		}

		loadTypeRewards();
	}

	/**
	 * 日志记录
	 * 
	 * @param msg
	 */
	public void logMsg(String msg) {
		logger.info(msg);
	}

	/**
	 * 获取一个mc对象
	 * 
	 * @return
	 */
	public MemCachedClient obtainMC() {
		MemCachedClient mc = null;
		memCachedLock.lock();
		try {
			if (memCachedClients.size() > 0) {
				mc = memCachedClients.remove(0);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			memCachedLock.unlock();
		}

		// 没找到空闲即创建
		if (mc == null) {
			mc = new MemCachedClient();
		}

		return mc;
	}

	/**
	 * 还回mc
	 * 
	 * @param mc
	 */
	public void givebackMC(MemCachedClient mc) {
		if (mc != null) {
			memCachedLock.lock();
			try {
				memCachedClients.add(mc);
			} catch (Exception e) {
				HawkException.catchException(e);
			} finally {
				memCachedLock.unlock();
			}
		}
	}

	/**
	 * 获取所有奖励类型对应的奖励
	 * 
	 * @return
	 */
	public Map<String, CdkTypeReward> getTypeRewards() {
		return typeRewardMap;
	}

	/**
	 * 获取所有奖励类型对应的奖励
	 * 
	 * @return
	 */
	public CdkTypeReward getTypeReward(String type) {
		return typeRewardMap.get(type);
	}

	/**
	 * 往cdk填充游戏和类型数据
	 * 
	 * @param game
	 * @param type
	 * @param cdkSerial
	 */
	public void fillGameAndType(String game, String type, char[] cdkSerial) {
		try {
			cdkSerial[0] = VALID_CDK_KEYS.charAt(HawkRand.randInt(0, VALID_CDK_KEYS.length() - 1));
			cdkSerial[1] = game.charAt(0);

			cdkSerial[2] = VALID_CDK_KEYS.charAt(HawkRand.randInt(0, VALID_CDK_KEYS.length() - 1));
			cdkSerial[3] = game.charAt(1);

			cdkSerial[4] = VALID_CDK_KEYS.charAt(HawkRand.randInt(0, VALID_CDK_KEYS.length() - 1));
			cdkSerial[5] = type.charAt(0);

			cdkSerial[6] = VALID_CDK_KEYS.charAt(HawkRand.randInt(0, VALID_CDK_KEYS.length() - 1));
			cdkSerial[7] = type.charAt(1);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 从cdk读取游戏名
	 * 
	 * @param cdk
	 * @return
	 */
	public String getGameNameFromCdk(String cdk) {
		if (cdk.length() == CDK_TOTAL_LENGTH) {
			char[] gameName = { cdk.charAt(1), cdk.charAt(3) };
			return String.valueOf(gameName);
		}
		return "";
	}

	/**
	 * 从cdk读取游戏名
	 * 
	 * @param cdk
	 * @return
	 */
	public String getTypeNameFromCdk(String cdk) {
		if (cdk.length() == CDK_TOTAL_LENGTH) {
			char[] typeName = { cdk.charAt(5), cdk.charAt(7) };
			return String.valueOf(typeName);
		}
		return "";
	}

	/**
	 * 检测CDK有效性
	 * 
	 * @param game
	 * @param type
	 * @param cdk
	 * @return
	 */
	public boolean checkCdkValid(String game, String type, String cdk) {
		String gameName = getGameNameFromCdk(cdk);
		String typeName = getTypeNameFromCdk(cdk);
		if (gameName.equals(game) && typeName.equals(type)) {
			int charCount = 0;
			int digitCount = 0;
			for (int i = CDK_NAMT_TYPE_LEN; i < CDK_TOTAL_LENGTH; i++) {
				char ch = cdk.charAt(i);
				if (ch >= '0' && ch <= '9') {
					digitCount++;
				} else if (ch >= 'a' && ch <= 'z') {
					charCount++;
				}
			}
			return digitCount >= CDK_DIGIT_MIN_COUNT && charCount >= CDK_CHAR_MIN_COUNT;
		}
		return false;
	}

	/**
	 * 添加奖励类型
	 * 
	 * @param type
	 * @param rewards
	 * @return
	 */
	public boolean addCdkTypeReward(CdkTypeReward typeReward) {
		if (!typeRewardMap.containsKey(typeReward.getCdkGameType())) {
			MemCachedClient mc = obtainMC();
			try {
				typeRewardMap.put(typeReward.getCdkGameType(), typeReward);
				String typeRewardInfos = CdkUtil.typeRewardsToString(typeRewardMap);
				if (mc.set(MC_CDK_TYPES_KEY, typeRewardInfos)) {
					logMsg("Add TypeReward: " + typeReward.toString() + ", Time: " + HawkTime.getTimeString());
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			} finally {
				givebackMC(mc);
			}
		}
		return false;
	}

	/**
	 * 加载所有的类型奖励
	 */
	private void loadTypeRewards() {
		MemCachedClient mc = obtainMC();
		try {
			Object cdkObj = mc.get(MC_CDK_TYPES_KEY);
			if (cdkObj != null) {
				String types = (String) cdkObj;
				typeRewardMap = CdkUtil.stringToTypeRewards(types);
				for (Map.Entry<String, CdkTypeReward> entry : typeRewardMap.entrySet()) {
					HawkLog.logPrintln("Load Cdk Type: " + entry.getValue().toString());
				}
			} else {
				boolean addRet = mc.add(MC_CDK_TYPES_KEY, "{}");
				if (!addRet) {
					HawkLog.logPrintln("Add MC_CDK_TYPES_KEY:{} Error");
				}
				typeRewardMap = new HashMap<String, CdkTypeReward>();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			givebackMC(mc);
		}
	}

	/**
	 * 删除一个CDK
	 * 
	 * @param param
	 * @param delCdks
	 * @return
	 */
	public boolean delCdk(DelCdkParam param, List<String> delCdks) {
		if (param.getCdks() != null && param.getCdks().size() > 0) {
			MemCachedClient mc = obtainMC();
			try {
				for (String cdk : param.getCdks()) {
					String key = String.format(MC_CDK_DATA_FMT, cdk);
					if (mc.delete(key)) {
						delCdks.add(cdk);
						logMsg("Delete Cdk: " + cdk + ", Time: " + HawkTime.getTimeString());
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			} finally {
				givebackMC(mc);
			}
			return true;
		}
		return false;
	}

	/**
	 * 删除一个CDK类型
	 * 
	 * @param type
	 * @return
	 */
	public boolean delCdkType(DelTypeParam param) {
		if (param.getType() != null && param.getType().length() > 0) {
			String gameType = param.getGame() + "." + param.getType();
			if (!typeRewardMap.containsKey(gameType)) {
				return true;
			}

			CdkTypeReward typeReward = typeRewardMap.remove(gameType);
			MemCachedClient mc = obtainMC();
			try {
				String typeRewardInfos = CdkUtil.typeRewardsToString(typeRewardMap);
				if (mc.set(MC_CDK_TYPES_KEY, typeRewardInfos)) {
					logMsg("Delete TypeReward: " + typeReward.toString() + ", Time: " + HawkTime.getTimeString());
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			} finally {
				givebackMC(mc);
			}
		}
		return false;
	}

	/**
	 * 生成CDK
	 * 
	 * @param param
	 * @param genCdks
	 * @return
	 */
	public int genCdk(GenCdkParam param, List<String> genCdks) {
		CdkTypeReward typeReward = new CdkTypeReward();
		typeReward.setGame(param.getGame());
		typeReward.setPlatform(param.getPlatform());
		typeReward.setType(param.getType());
		typeReward.setReward(param.getReward());
		typeReward.setStarttime(param.getStarttime());
		typeReward.setEndtime(param.getEndtime());

		// 添加cdk类型奖励失败, 已存在
		if (!addCdkTypeReward(typeReward)) {
			return CDK_TYPE_EXIST;
		}

		String filePath = String.format("%s/cdks/%s-%s-%s-%s.txt", System.getProperty("user.dir"), param.getGame(), param.getType(), param.getStarttime(), param.getEndtime());

		if (param.getPlatform().length() > 0) {
			filePath = String.format("%s/cdks/%s-%s-%s-%s-%s.txt", System.getProperty("user.dir"), param.getGame(), param.getPlatform(), param.getType(), param.getStarttime(), param.getEndtime());
		}

		FileWriter fileWrite = null;
		BufferedWriter bufferedWriter = null;
		try {
			fileWrite = new FileWriter(filePath, false);
			if (fileWrite != null) {
				bufferedWriter = new BufferedWriter(fileWrite);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		MemCachedClient mc = obtainMC();
		try {
			// 生成cdk
			int genCount = 0;
			while (genCount < param.getCount()) {
				// 填充名字
				char[] cdkPrefix = new char[CDK_NAMT_TYPE_LEN];
				fillGameAndType(param.getGame(), param.getType(), cdkPrefix);

				char[] cdkRandom = new char[CDK_TOTAL_LENGTH - CDK_NAMT_TYPE_LEN];
				// 随机数字
				for (int j = 0; j < CDK_DIGIT_MIN_COUNT; j++) {
					cdkRandom[j] = VALID_CDK_KEYS.charAt(HawkRand.randInt(0, 9));
				}

				// 随机字符
				for (int j = 0; j < CDK_CHAR_MIN_COUNT; j++) {
					cdkRandom[j + CDK_DIGIT_MIN_COUNT] = VALID_CDK_KEYS.charAt(HawkRand.randInt(10, VALID_CDK_KEYS.length() - 1));
				}

				int fixCount = CDK_DIGIT_MIN_COUNT + CDK_CHAR_MIN_COUNT;
				int randCount = CDK_TOTAL_LENGTH - CDK_NAMT_TYPE_LEN - fixCount;
				for (int j = 0; j < randCount; j++) {
					cdkRandom[j + fixCount] = VALID_CDK_KEYS.charAt(HawkRand.randInt(0, VALID_CDK_KEYS.length() - 1));
				}

				// 数字字符乱序
				for (int j = 0; j < CDK_DIGIT_MIN_COUNT + CDK_CHAR_MIN_COUNT; j++) {
					int r = HawkRand.randInt(0, CDK_DIGIT_MIN_COUNT + CDK_CHAR_MIN_COUNT - 1);
					if (r != j) {
						char curChar = cdkRandom[j];
						cdkRandom[j] = cdkRandom[r];
						cdkRandom[r] = curChar;
					}
				}

				// 生成数据库信息
				String cdkSerial = String.valueOf(cdkPrefix) + String.valueOf(cdkRandom);
				CdkInfo info = new CdkInfo();
				info.setCdk(cdkSerial);

				// 写入数据库
				String key = String.format(MC_CDK_DATA_FMT, cdkSerial);
				if (mc.add(key, info.toString())) {
					genCdks.add(cdkSerial);
					genCount++;

					logMsg("Generate Cdk: " + cdkSerial + ", Time: " + HawkTime.getTimeString());

					if (bufferedWriter != null) {
						bufferedWriter.write(cdkSerial);
						bufferedWriter.newLine();
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			try {
				if (bufferedWriter != null) {
					bufferedWriter.close();
					fileWrite.close();
				}
			} catch (IOException e) {
				HawkException.catchException(e);
			}

			givebackMC(mc);
		}
		return CDK_STATUS_OK;
	}

	/**
	 * 追加cdk
	 * 
	 * @param cdkparam
	 * @param genCdks
	 * @return
	 */
	public int appendCdk(AppendCdkParam param, List<String> genCdks) {
		CdkTypeReward typeReward = getTypeReward(param.getGame() + "." + param.getType());

		// 追加cdk失败, 不存在
		if (typeReward == null) {
			return CDK_TYPE_NONEXIST;
		}

		String filePath = String.format("%s/cdks/%s-%s-%s-%s.txt", System.getProperty("user.dir"), typeReward.getGame(), typeReward.getType(), typeReward.getStarttime(), typeReward.getEndtime());

		if (typeReward.getPlatform().length() > 0) {
			filePath = String.format("%s/cdks/%s-%s-%s-%s-%s.txt", System.getProperty("user.dir"), typeReward.getGame(), typeReward.getPlatform(), typeReward.getType(), typeReward.getStarttime(), typeReward.getEndtime());
		}

		FileWriter fileWrite = null;
		BufferedWriter bufferedWriter = null;
		try {
			fileWrite = new FileWriter(filePath, true);
			if (fileWrite != null) {
				bufferedWriter = new BufferedWriter(fileWrite);
				bufferedWriter.newLine();
				bufferedWriter.newLine();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		MemCachedClient mc = obtainMC();
		try {
			// 生成cdk
			int genCount = 0;
			while (genCount < param.getCount()) {
				// 填充名字
				char[] cdkPrefix = new char[CDK_NAMT_TYPE_LEN];
				fillGameAndType(param.getGame(), param.getType(), cdkPrefix);

				char[] cdkRandom = new char[CDK_TOTAL_LENGTH - CDK_NAMT_TYPE_LEN];
				// 随机数字
				for (int j = 0; j < CDK_DIGIT_MIN_COUNT; j++) {
					cdkRandom[j] = VALID_CDK_KEYS.charAt(HawkRand.randInt(0, 9));
				}

				// 随机字符
				for (int j = 0; j < CDK_CHAR_MIN_COUNT; j++) {
					cdkRandom[j + CDK_DIGIT_MIN_COUNT] = VALID_CDK_KEYS.charAt(HawkRand.randInt(10, VALID_CDK_KEYS.length() - 1));
				}

				int fixCount = CDK_DIGIT_MIN_COUNT + CDK_CHAR_MIN_COUNT;
				int randCount = CDK_TOTAL_LENGTH - CDK_NAMT_TYPE_LEN - fixCount;
				for (int j = 0; j < randCount; j++) {
					cdkRandom[j + fixCount] = VALID_CDK_KEYS.charAt(HawkRand.randInt(0, VALID_CDK_KEYS.length() - 1));
				}

				// 数字字符乱序
				for (int j = 0; j < CDK_DIGIT_MIN_COUNT + CDK_CHAR_MIN_COUNT; j++) {
					int r = HawkRand.randInt(0, CDK_DIGIT_MIN_COUNT + CDK_CHAR_MIN_COUNT - 1);
					if (r != j) {
						char curChar = cdkRandom[j];
						cdkRandom[j] = cdkRandom[r];
						cdkRandom[r] = curChar;
					}
				}

				// 生成数据库信息
				String cdkSerial = String.valueOf(cdkPrefix) + String.valueOf(cdkRandom);
				CdkInfo info = new CdkInfo();
				info.setCdk(cdkSerial);

				// 写入数据库
				String key = String.format(MC_CDK_DATA_FMT, cdkSerial);
				if (mc.add(key, info.toString())) {
					genCdks.add(cdkSerial);
					genCount++;

					logMsg("Generate Cdk: " + cdkSerial + ", Time: " + HawkTime.getTimeString());

					if (bufferedWriter != null) {
						bufferedWriter.write(cdkSerial);
						bufferedWriter.newLine();
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			try {
				if (bufferedWriter != null) {
					bufferedWriter.close();
					fileWrite.close();
				}
			} catch (IOException e) {
				HawkException.catchException(e);
			}

			givebackMC(mc);
		}
		return CDK_STATUS_OK;
	}

	/**
	 * 查询cdk类型数据
	 * 
	 * @param param
	 * @return
	 */
	public CdkTypeReward queryTypeReward(QueryTypeParam param) {
		String gameType = param.getGame() + "." + param.getType();
		CdkTypeReward typeReward = typeRewardMap.get(gameType);
		return typeReward;
	}

	/**
	 * 重置cdk类型信息(奖励, 有效期)
	 * 
	 * @param param
	 * @return
	 */
	public CdkTypeReward resetTypeReward(ResetRewardParam param) {
		String gameType = param.getGame() + "." + param.getType();
		CdkTypeReward typeReward = typeRewardMap.get(gameType);
		if (typeReward != null) {
			boolean hasChanged = false;

			if (param.getPlatform() != null && param.getPlatform().length() > 0) {
				if ("0".equals(param.getPlatform()) || "null".equals(param.getPlatform())) {
					typeReward.setPlatform("");
				} else {
					typeReward.setPlatform(param.getPlatform());
				}
				hasChanged = true;
			} else if (typeReward.getPlatform().length() > 0) {
				typeReward.setPlatform("");
				hasChanged = true;
			}

			if (param.getReward() != null && param.getReward().length() > 0) {
				typeReward.setReward(param.getReward());
				hasChanged = true;
			}

			if (param.getStarttime() != null && param.getStarttime().length() > 0) {
				typeReward.setStarttime(param.getStarttime());
				hasChanged = true;
			}

			if (param.getEndtime() != null && param.getEndtime().length() > 0) {
				typeReward.setEndtime(param.getEndtime());
				hasChanged = true;
			}

			if (hasChanged) {
				typeRewardMap.put(typeReward.getCdkGameType(), typeReward);

				MemCachedClient mc = obtainMC();
				try {
					String typeRewardInfos = CdkUtil.typeRewardsToString(typeRewardMap);
					if (mc.set(MC_CDK_TYPES_KEY, typeRewardInfos)) {
						logMsg("Reset TypeReward: " + typeReward.toString() + ", Time: " + HawkTime.getTimeString());
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				} finally {
					givebackMC(mc);
				}
			}
		}
		return typeReward;
	}

	/**
	 * 查询cdk信息
	 * 
	 * @param param
	 * @return
	 */
	public CdkInfo queryCdkInfo(QueryCdkParam param) {
		String game = getGameNameFromCdk(param.getCdk());
		String type = getTypeNameFromCdk(param.getCdk());

		// 不存在
		String gameType = game + "." + type;
		CdkTypeReward typeReward = typeRewardMap.get(gameType);
		if (typeReward == null && checkCdkValid(game, type, param.getCdk())) {
			return null;
		}

		MemCachedClient mc = obtainMC();
		try {
			String key = String.format(MC_CDK_DATA_FMT, param.getCdk());
			Object cdkObj = mc.get(key);
			if (cdkObj == null) {
				return null;
			}

			CdkInfo cdkInfo = new CdkInfo();
			if (!cdkInfo.parse((String) cdkObj)) {
				return null;
			}
			return cdkInfo;
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			givebackMC(mc);
		}
		return null;
	}

	/**
	 * 使用cdk
	 * 
	 * @param param
	 * @return
	 */
	public int useCdk(UseCdkParam param) {
		if (param.getCdk().length() != CDK_TOTAL_LENGTH) {
			return CDK_STATUS_NONEXIST;
		}

		String game = getGameNameFromCdk(param.getCdk());
		String type = getTypeNameFromCdk(param.getCdk());

		// 不存在
		String gameType = game + "." + type;
		CdkTypeReward typeReward = typeRewardMap.get(gameType);
		if (typeReward == null && checkCdkValid(game, type, param.getCdk())) {
			return CDK_STATUS_NONEXIST;
		}

		// 平台校验(除了ios91的没有前缀,其他都有)
		String puid = param.getPuid();
		if (puid.indexOf('_') < 0) {
			puid = "91_" + param.getPuid();
		}

		String platform = typeReward.getPlatform();
		if (platform != null && platform.length() > 0 && !"null".equals(platform) && !puid.startsWith(platform)) {
			return CDK_STATUS_PLAT;
		}

		if (typeReward.isInFuture()) {
			return CDK_STATUS_FUTURE;
		}

		if (typeReward.isTimeout()) {
			return CDK_STATUS_PAST;
		}

		MemCachedClient mc = obtainMC();
		try {
			String key = String.format(MC_CDK_DATA_FMT, param.getCdk());
			Object cdkObj = mc.get(key);
			if (cdkObj == null) {
				return CDK_STATUS_NONEXIST;
			}

			CdkInfo cdkInfo = new CdkInfo();
			if (!cdkInfo.parse((String) cdkObj)) {
				logMsg("Cdk Format Error: " + cdkInfo.toString());
				return CDK_STATUS_NONEXIST;
			}

			if (cdkInfo.isBeused()) {
				logMsg("Cdk Been Used: " + cdkInfo.toString());
				return CDK_STATUS_USED;
			}

			cdkInfo.setUsed(param.getGame(), param.getPlatform(), param.getServer(), param.getPlayerid(), param.getPlayername(), typeReward.getReward());
			if (mc.set(key, cdkInfo.toString())) {
				logMsg("Use Cdk: " + cdkInfo.toString() + ", Time: " + HawkTime.getTimeString());
			}

			param.setReward(typeReward.getReward());
			return CDK_STATUS_OK;

		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			givebackMC(mc);
		}
		return CDK_STATUS_NONEXIST;
	}

	/**
	 * 帧更新
	 */
	public boolean tick() {
		// 每120s通信一次, 避免cmem的180s超时
		int currentTime = (int) (Math.floor(System.currentTimeMillis() / 1000));
		if (currentTime - lastVisitTime >= 120000) {
			MemCachedClient mc = obtainMC();
			try {
				Object cdkObj = mc.get(MC_CDK_AUTHOR_KEY);
				if (cdkObj == null) {
					mc.add(MC_CDK_AUTHOR_KEY, "hawk");
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			} finally {
				givebackMC(mc);
			}
		}
		return running;
	}

	/**
	 * 停止
	 */
	public void stop() {
		running = false;
	}
}
