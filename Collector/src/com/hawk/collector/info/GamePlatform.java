package com.hawk.collector.info;

public class GamePlatform {
	private String game = "";
	private String platform = "";
	private String channel = "";
	private String logUserName = "";
	private String logUserPwd = "";
	private String logPath = "";
	private String sshPort = "0";
	
	public GamePlatform(String game, String platform, String channel, String logUserName, String logUserPwd, String logPath, String sshPort) {
		this.game = game;
		this.platform = platform;
		this.channel = channel;
		
		if (logUserName != null) {
			this.logUserName = logUserName;
		}
		
		if (logUserPwd != null) {
			this.logUserPwd = logUserPwd;
		}
		
		if (logPath != null) {
			this.logPath = logPath;
		}
		
		if (sshPort != null) {
			this.sshPort = sshPort;
		}
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

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public String getLogUserName() {
		return logUserName;
	}

	public void setLogUserName(String logUserName) {
		this.logUserName = logUserName;
	}

	public String getLogUserPwd() {
		return logUserPwd;
	}

	public void setLogUserPwd(String logUserPwd) {
		this.logUserPwd = logUserPwd;
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
	
	public String getSshPort() {
		return sshPort;
	}

	public void setSshPort(String sshPort) {
		this.sshPort = sshPort;
	}
	
	@Override
	public String toString() {
		return String.format("game: %s, platform: %s, channel: %s, logUserName: %s, logUserPwd: %s, logPath: %s, sshPort: %s", 
				game, platform, channel, logUserName, logUserPwd, logPath, sshPort);
	}
}
