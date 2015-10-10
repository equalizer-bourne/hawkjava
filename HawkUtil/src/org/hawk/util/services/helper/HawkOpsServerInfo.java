package org.hawk.util.services.helper;

import net.sf.json.JSONObject;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.google.gson.JsonObject;

/**
 * 运维服务器信息
 * 
 * @author hawk
 */
public class HawkOpsServerInfo {
	private String game;
	private String platform;
	private String serverId;
	private String port;
	private String scriptPort;
	private String dbUrl;
	private String dbUser;
	private String dbPwd;
	private String workPath;
	private String pid;
	private String myip;
	
	public HawkOpsServerInfo() {
		workPath = HawkOSOperator.getWorkPath();
		pid = String.valueOf(HawkOSOperator.getProcessId());
	}
	
	public String getGame() {
		return game;
	}

	public void setGame(String game) {
		this.game = game;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getScriptPort() {
		return scriptPort;
	}

	public void setScriptPort(String scriptPort) {
		this.scriptPort = scriptPort;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPwd() {
		return dbPwd;
	}

	public void setDbPwd(String dbPwd) {
		this.dbPwd = dbPwd;
	}

	public String getWorkPath() {
		return workPath;
	}

	public void setWorkPath(String workPath) {
		this.workPath = workPath;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getMyip() {
		return myip;
	}

	public void setMyip(String myip) {
		this.myip = myip;
	}
	
	public String getIdentify() {
		return String.format("%s.%s.%s", game, platform, serverId);
	}

	public String toString() {
		return toJson().toString();
	}

	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("game", game);
		jsonObject.addProperty("platform", platform);
		jsonObject.addProperty("serverId", serverId);
		jsonObject.addProperty("port", port);
		jsonObject.addProperty("scriptPort", scriptPort);
		jsonObject.addProperty("dbUrl", dbUrl);
		jsonObject.addProperty("dbUser", dbUser);
		jsonObject.addProperty("dbPwd", dbPwd);
		jsonObject.addProperty("workPath", workPath);
		jsonObject.addProperty("pid", pid);
		jsonObject.addProperty("myip", myip);
		return jsonObject;
	}

	public boolean fromJson(JSONObject jsonObject) {
		try {
			game = jsonObject.getString("game");
			platform = jsonObject.getString("platform");
			serverId = jsonObject.getString("serverId");
			port = jsonObject.getString("port");
			scriptPort = jsonObject.getString("scriptPort");
			dbUrl = jsonObject.getString("dbUrl");
			dbUser = jsonObject.getString("dbUser");
			dbPwd = jsonObject.getString("dbPwd");
			workPath = jsonObject.getString("workPath");
			pid = jsonObject.getString("pid");
			myip = jsonObject.getString("myip");
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	public boolean meetConditions(String game, String serverids) {
		if (game != null && game.equals(this.game) && serverids != null && serverids.length() > 0) {
			String platformServerIds[] = serverids.split(",");
			for (String platformServerId : platformServerIds) {
				if (platformServerId.equals(this.platform + "." + this.serverId)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasChanged(HawkOpsServerInfo serverInfo) {
		return !game.equals(serverInfo.game) ||
			   !platform.equals(serverInfo.platform) ||
			   !serverId.equals(serverInfo.serverId) ||
			   !port.equals(serverInfo.port) ||
			   !scriptPort.equals(serverInfo.scriptPort) ||
			   !dbUrl.equals(serverInfo.dbUrl) ||
			   !dbUser.equals(serverInfo.dbUser) ||
			   !dbPwd.equals(serverInfo.dbPwd) ||
			   !workPath.equals(serverInfo.workPath) ||
			   !pid.equals(serverInfo.pid) ||
			   !myip.equals(serverInfo.myip);
	}
}
