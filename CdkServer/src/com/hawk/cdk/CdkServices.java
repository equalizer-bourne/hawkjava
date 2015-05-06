package com.hawk.cdk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.db.cache.HawkMemCacheDB;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.util.services.HawkCdkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * MC客户端
	 */
	private HawkMemCacheDB memCachedClient = null;
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
	public boolean initMC(String addr, int timeout, int redisPort) {
		// 创建连接
		memCachedClient = new HawkMemCacheDB();
		if (redisPort <= 0) {
			if (!memCachedClient.initAsMemCached(addr, timeout)) {
				return false;
			}
		} else {
			if (!memCachedClient.initAsRedis(addr, redisPort)) {
				return false;
			}
		}
		// 加载类型奖励
		loadTypeRewards();
		return true;
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
		if (cdk.length() == HawkCdkService.CDK_TOTAL_LENGTH) {
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
		if (cdk.length() == HawkCdkService.CDK_TOTAL_LENGTH) {
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
			for (int i = HawkCdkService.CDK_NAMT_TYPE_LEN; i < HawkCdkService.CDK_TOTAL_LENGTH; i++) {
				char ch = cdk.charAt(i);
				if (ch >= '0' && ch <= '9') {
					digitCount++;
				} else if (ch >= 'a' && ch <= 'z') {
					charCount++;
				}
			}
			return digitCount >= HawkCdkService.CDK_DIGIT_MIN_COUNT && charCount >= HawkCdkService.CDK_CHAR_MIN_COUNT;
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
			try {
				typeRewardMap.put(typeReward.getCdkGameType(), typeReward);
				String typeRewardInfos = CdkUtil.typeRewardsToString(typeRewardMap);
				if (memCachedClient.setString(MC_CDK_TYPES_KEY, typeRewardInfos)) {
					logMsg("Add TypeReward: " + typeReward.toString() + ", Time: " + HawkTime.getTimeString());
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 加载所有的类型奖励
	 */
	private void loadTypeRewards() {
		try {
			String types = memCachedClient.getString(MC_CDK_TYPES_KEY);
			if (types != null) {
				typeRewardMap = CdkUtil.stringToTypeRewards(types);
				for (Map.Entry<String, CdkTypeReward> entry : typeRewardMap.entrySet()) {
					HawkLog.logPrintln("Load Cdk Type: " + entry.getValue().toString());
				}
			} else {
				boolean addRet = memCachedClient.setString(MC_CDK_TYPES_KEY, "{}");
				if (!addRet) {
					HawkLog.logPrintln("Add MC_CDK_TYPES_KEY:{} Error");
				}
				typeRewardMap = new HashMap<String, CdkTypeReward>();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 字符串jsonmap转换为字符串
	 * 
	 * @param jsonMap
	 * @return
	 */
	public Map<String, CdkTypeReward> getGameTypeRewards(String game) {
		Map<String, CdkTypeReward> typeRewardsInfos = new HashMap<String, CdkTypeReward>();
		for (Map.Entry<String, CdkTypeReward> entry : typeRewardMap.entrySet()) {
			if (entry.getValue().getGame().equals(game)) {
				typeRewardsInfos.put(entry.getValue().getType(), entry.getValue());
			}
		}
		return typeRewardsInfos;
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
			try {
				for (String cdk : param.getCdks()) {
					String key = String.format(MC_CDK_DATA_FMT, cdk);
					if (memCachedClient.delete(key)) {
						delCdks.add(cdk);
						logMsg("Delete Cdk: " + cdk + ", Time: " + HawkTime.getTimeString());
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
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
			try {
				String typeRewardInfos = CdkUtil.typeRewardsToString(typeRewardMap);
				if (memCachedClient.setString(MC_CDK_TYPES_KEY, typeRewardInfos)) {
					logMsg("Delete TypeReward: " + typeReward.toString() + ", Time: " + HawkTime.getTimeString());
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 生成cdk序列号
	 * @param game
	 * @param type
	 * @return
	 */
	private String genCdkSerial(String game, String type) throws Exception {
		// 填充名字
		char[] cdkPrefix = new char[HawkCdkService.CDK_NAMT_TYPE_LEN];
		fillGameAndType(game, type, cdkPrefix);

		char[] cdkRandom = new char[HawkCdkService.CDK_TOTAL_LENGTH - HawkCdkService.CDK_NAMT_TYPE_LEN];
		// 随机数字
		for (int j = 0; j < HawkCdkService.CDK_DIGIT_MIN_COUNT; j++) {
			cdkRandom[j] = VALID_CDK_KEYS.charAt(HawkRand.randInt(0, 9));
		}

		// 随机字符
		for (int j = 0; j < HawkCdkService.CDK_CHAR_MIN_COUNT; j++) {
			cdkRandom[j + HawkCdkService.CDK_DIGIT_MIN_COUNT] = VALID_CDK_KEYS.charAt(HawkRand.randInt(10, VALID_CDK_KEYS.length() - 1));
		}

		int fixCount = HawkCdkService.CDK_DIGIT_MIN_COUNT + HawkCdkService.CDK_CHAR_MIN_COUNT;
		int randCount = HawkCdkService.CDK_TOTAL_LENGTH - HawkCdkService.CDK_NAMT_TYPE_LEN - fixCount;
		for (int j = 0; j < randCount; j++) {
			cdkRandom[j + fixCount] = VALID_CDK_KEYS.charAt(HawkRand.randInt(0, VALID_CDK_KEYS.length() - 1));
		}

		// 数字字符乱序
		for (int j = 0; j < HawkCdkService.CDK_DIGIT_MIN_COUNT + HawkCdkService.CDK_CHAR_MIN_COUNT; j++) {
			int r = HawkRand.randInt(0, HawkCdkService.CDK_DIGIT_MIN_COUNT + HawkCdkService.CDK_CHAR_MIN_COUNT - 1);
			if (r != j) {
				char curChar = cdkRandom[j];
				cdkRandom[j] = cdkRandom[r];
				cdkRandom[r] = curChar;
			}
		}

		// 生成数据库信息
		String cdkSerial = String.valueOf(cdkPrefix) + String.valueOf(cdkRandom);
		return cdkSerial;
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
			return HawkCdkService.CDK_TYPE_EXIST;
		}

		// 创建目录
		try {
			File cdksFolder = new File(System.getProperty("user.dir") + "/cdks/");
			if(!cdksFolder.exists()) {
				cdksFolder.mkdir();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		String filePath = String.format("%s/cdks/%s-%s.txt", System.getProperty("user.dir"), param.getGame(), param.getType());
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

		try {
			// 生成cdk
			int genCount = 0;
			while (genCount < param.getCount()) {
				// 生成数据库信息
				String cdkSerial = genCdkSerial(param.getGame(), param.getType());
				CdkInfo info = new CdkInfo();
				info.setCdk(cdkSerial);

				// 写入数据库
				String key = String.format(MC_CDK_DATA_FMT, cdkSerial);
				if (memCachedClient.setString(key, info.toString())) {
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
		}
		return HawkCdkService.CDK_STATUS_OK;
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
			return HawkCdkService.CDK_TYPE_NONEXIST;
		}

		String filePath = String.format("%s/cdks/%s-%s.txt", System.getProperty("user.dir"), typeReward.getGame(), typeReward.getType());
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

		try {
			// 生成cdk
			int genCount = 0;
			while (genCount < param.getCount()) {
				// 生成数据库信息
				String cdkSerial = genCdkSerial(param.getGame(), param.getType());
				CdkInfo info = new CdkInfo();
				info.setCdk(cdkSerial);

				// 写入数据库
				String key = String.format(MC_CDK_DATA_FMT, cdkSerial);
				if (memCachedClient.setString(key, info.toString())) {
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
		}
		return HawkCdkService.CDK_STATUS_OK;
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

				try {
					String typeRewardInfos = CdkUtil.typeRewardsToString(typeRewardMap);
					if (memCachedClient.setString(MC_CDK_TYPES_KEY, typeRewardInfos)) {
						logMsg("Reset TypeReward: " + typeReward.toString() + ", Time: " + HawkTime.getTimeString());
					}
				} catch (Exception e) {
					HawkException.catchException(e);
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

		try {
			String key = String.format(MC_CDK_DATA_FMT, param.getCdk());
			String cdk = memCachedClient.getString(key);
			if (cdk == null) {
				return null;
			}

			CdkInfo cdkInfo = new CdkInfo();
			if (!cdkInfo.parse(cdk)) {
				return null;
			}
			return cdkInfo;
		} catch (Exception e) {
			HawkException.catchException(e);
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
		if (param.getCdk().length() != HawkCdkService.CDK_TOTAL_LENGTH) {
			return HawkCdkService.CDK_STATUS_NONEXIST;
		}

		String game = getGameNameFromCdk(param.getCdk());
		String type = getTypeNameFromCdk(param.getCdk());

		// 不存在
		String gameType = game + "." + type;
		CdkTypeReward typeReward = typeRewardMap.get(gameType);
		if (typeReward == null || !checkCdkValid(game, type, param.getCdk())) {
			return HawkCdkService.CDK_STATUS_NONEXIST;
		}

		// 平台校验
		String puid = param.getPuid();
		String platform = typeReward.getPlatform();
		if (platform != null && platform.length() > 0 && !"null".equals(platform)) {
			boolean platformValid = false;
			String items[] = platform.split(",");
			for (String pf : items) {
				if (puid.startsWith(pf)) {
					platformValid = true;
					break;
				}
			}
			
			if (!platformValid) {
				return HawkCdkService.CDK_STATUS_PLAT;
			}
		}

		if (typeReward.isInFuture()) {
			return HawkCdkService.CDK_STATUS_FUTURE;
		}

		if (typeReward.isTimeout()) {
			return HawkCdkService.CDK_STATUS_PAST;
		}

		try {
			String key = String.format(MC_CDK_DATA_FMT, param.getCdk());
			String cdk = memCachedClient.getString(key);
			if (cdk == null) {
				return HawkCdkService.CDK_STATUS_NONEXIST;
			}

			CdkInfo cdkInfo = new CdkInfo();
			if (!cdkInfo.parse(cdk)) {
				logMsg("Cdk Format Error: " + cdkInfo.toString());
				return HawkCdkService.CDK_STATUS_NONEXIST;
			}

			if (cdkInfo.isBeused()) {
				logMsg("Cdk Been Used: " + cdkInfo.toString());
				return HawkCdkService.CDK_STATUS_USED;
			}

			cdkInfo.setUsed(param.getGame(), param.getPlatform(), param.getServer(), param.getPlayerid(), param.getPlayername(), typeReward.getReward());
			if (memCachedClient.setString(key, cdkInfo.toString())) {
				logMsg("Use Cdk: " + cdkInfo.toString() + ", Time: " + HawkTime.getTimeString());
			}

			param.setReward(typeReward.getReward());
			return HawkCdkService.CDK_STATUS_OK;

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return HawkCdkService.CDK_STATUS_NONEXIST;
	}

	/**
	 * 帧更新
	 */
	public boolean tick() {
		// 每120s通信一次, 避免cmem的180s超时
		int currentTime = (int) (Math.floor(System.currentTimeMillis() / 1000));
		if (currentTime - lastVisitTime >= 120000) {
			try {
				String author = memCachedClient.getString(MC_CDK_AUTHOR_KEY);
				if (author == null) {
					memCachedClient.setString(MC_CDK_AUTHOR_KEY, "hawk");
				}
			} catch (Exception e) {
				HawkException.catchException(e);
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
