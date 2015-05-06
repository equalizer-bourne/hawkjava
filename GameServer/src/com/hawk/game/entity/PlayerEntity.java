package com.hawk.game.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

/**
 * 玩家基础数据
 * 
 * @author hawk
 * 
 */
@Entity
@Table(name = "player")
@SuppressWarnings("serial")
public class PlayerEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "AUTO_INCREMENT", strategy = "native")
	@GeneratedValue(generator = "AUTO_INCREMENT")
	@Column(name = "id", unique = true)
	private int id = 0;
	
	@Column(name = "puid", unique = true, nullable = false)
	protected String puid = "";

	@Column(name = "name", unique = true, nullable = false)
	private String name ;
	
	@Column(name = "level")
	private int level;
	
	@Column(name = "exp")
	private int exp;
	
	@Column(name = "gold")
	protected int gold = 0;

	@Column(name = "coin")
	protected long coin = 0;

	@Column(name = "recharge")
	protected int recharge = 0;

	@Column(name = "vipLevel")
	protected int vipLevel = 0;

	@Column(name = "device", nullable = false)
	protected String device = "";

	@Column(name = "platform", nullable = false)
	protected String platform = "";

	@Column(name = "phoneInfo", nullable = false)
	protected String phoneInfo = "";
	
	@Column(name = "forbidenTime")
	protected Date forbidenTime = null;
	
	@Column(name = "silentTime")
	protected Date silentTime = null;
	
	@Column(name = "loginTime")
	protected Date loginTime = null;

	@Column(name = "logoutTime")
	protected Date logoutTime = null;
	
	@Column(name = "resetTime")
	protected Date resetTime = null;
	
	@Column(name = "createTime", nullable = false)
	protected Date createTime = null;
	
	@Column(name = "updateTime")
	protected Date updateTime;

	@Column(name = "invalid")
	protected boolean invalid;
	
	public PlayerEntity() {
		this.createTime = HawkTime.getCalendar().getTime();
		this.loginTime = HawkTime.getCalendar().getTime();
	}
	
	public PlayerEntity(String puid, String device, String platform, String phoneInfo) {
		this.puid = puid;
		this.device = device;
		this.platform = platform;
		this.phoneInfo = phoneInfo;
		this.setName(puid);
		this.createTime = HawkTime.getCalendar().getTime();
		this.loginTime = HawkTime.getCalendar().getTime();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPuid() {
		return puid;
	}

	public void setPuid(String puid) {
		this.puid = puid;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}
	
	public long getCoin() {
		return coin;
	}

	public void setCoin(long coin) {
		this.coin = coin;
	}

	public int getRecharge() {
		return recharge;
	}

	public void setRecharge(int recharge) {
		this.recharge = recharge;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public Date getLogoutTime() {
		return logoutTime;
	}

	public void setLogoutTime(Date logoutTime) {
		this.logoutTime = logoutTime;
	}
	
	public Date getResetTime() {
		return resetTime;
	}

	public void setResetTime(Date resetTime) {
		this.resetTime = resetTime;
	}
	
	public Date getForbidenTime() {
		return forbidenTime;
	}

	public void setForbidenTime(Date forbidenTime) {
		this.forbidenTime = forbidenTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public String getPhoneInfo() {
		return phoneInfo;
	}

	public void setPhoneInfo(String phoneInfo) {
		this.phoneInfo = phoneInfo;
	}

	public Date getSilentTime() {
		return silentTime;
	}

	public void setSilentTime(Date silentTime) {
		this.silentTime = silentTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}
