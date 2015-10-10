package org.hawk.util.services.helper;

import net.sf.json.JSONObject;

import org.hawk.os.HawkException;

import com.google.gson.JsonObject;

/**
 * 订单实体对象
 * 
 * @author hawk
 */
public class HawkOrderEntity {
	protected int id = 0;
	protected String myOrder = "";
	protected String pfOrder = "";
	protected String suuid = "";
	protected String game = "";
	protected String platform = "";
	protected int serverId = 0;
	protected String channel = "";
	protected int playerId = 0;
	protected String puid = "";
	protected String device = "";
	protected String goodsId = "";
	protected int goodsCount = 0;
	protected int orderMoney = 0;
	protected int payMoney = 0;
	protected String currency = "";
	protected int addGold = 0;
	protected int giftGold = 0;
	protected String payPf = "";
	protected String userData = "";
	protected int status = 0;

	public JsonObject toProtocolJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("myOrder", myOrder);
		jsonObject.addProperty("pfOrder", pfOrder);
		jsonObject.addProperty("playerId", playerId);
		jsonObject.addProperty("puid", puid);
		jsonObject.addProperty("goodsId", goodsId);
		jsonObject.addProperty("goodsCount", goodsCount);
		jsonObject.addProperty("orderMoney", orderMoney);
		jsonObject.addProperty("payMoney", payMoney);
		return jsonObject;
	}

	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("suuid", suuid);
		jsonObject.addProperty("game", game);
		jsonObject.addProperty("platform", platform);
		jsonObject.addProperty("serverId", serverId);
		jsonObject.addProperty("myOrder", myOrder);
		jsonObject.addProperty("pfOrder", pfOrder);
		jsonObject.addProperty("playerId", playerId);
		jsonObject.addProperty("puid", puid);
		jsonObject.addProperty("channel", channel);
		jsonObject.addProperty("device", device);
		jsonObject.addProperty("goodsId", goodsId);
		jsonObject.addProperty("goodsCount", goodsCount);
		jsonObject.addProperty("orderMoney", orderMoney);
		jsonObject.addProperty("payMoney", payMoney);
		jsonObject.addProperty("currency", currency);
		jsonObject.addProperty("addGold", addGold);
		jsonObject.addProperty("giftGold", giftGold);
		jsonObject.addProperty("payPf", payPf);
		jsonObject.addProperty("userData", userData);
		jsonObject.addProperty("status", status);
		jsonObject.addProperty("id", id);
		return jsonObject;
	}

	public boolean fromJson(JSONObject jsonObject) {
		try {
			if (jsonObject.containsKey("id")) {
				id = jsonObject.getInt("id");
			}

			if (jsonObject.containsKey("myOrder")) {
				myOrder = jsonObject.getString("myOrder");
			}
			
			if (jsonObject.containsKey("pfOrder")) {
				pfOrder = jsonObject.getString("pfOrder");
			}

			if (jsonObject.containsKey("suuid")) {
				suuid = jsonObject.getString("suuid");
			}
			
			if (jsonObject.containsKey("game")) {
				game = jsonObject.getString("game");
			}

			if (jsonObject.containsKey("platform")) {
				platform = jsonObject.getString("platform");
			}

			if (jsonObject.containsKey("serverId")) {
				serverId = jsonObject.getInt("serverId");
			}

			if (jsonObject.containsKey("channel")) {
				channel = jsonObject.getString("channel");
			}

			if (jsonObject.containsKey("playerId")) {
				playerId = jsonObject.getInt("playerId");
			}

			if (jsonObject.containsKey("puid")) {
				puid = jsonObject.getString("puid");
			}

			if (jsonObject.containsKey("device")) {
				device = jsonObject.getString("device");
			}

			if (jsonObject.containsKey("goodsId")) {
				goodsId = jsonObject.getString("goodsId");
			}

			if (jsonObject.containsKey("goodsCount")) {
				goodsCount = jsonObject.getInt("goodsCount");
			}

			if (jsonObject.containsKey("orderMoney")) {
				orderMoney = jsonObject.getInt("orderMoney");
			}

			if (jsonObject.containsKey("payMoney")) {
				payMoney = jsonObject.getInt("payMoney");
			}

			if (jsonObject.containsKey("currency")) {
				currency = jsonObject.getString("currency");
			}

			if (jsonObject.containsKey("payPf")) {
				payPf = jsonObject.getString("payPf");
			}

			if (jsonObject.containsKey("userData")) {
				userData = jsonObject.getString("userData");
			}
			
			if (jsonObject.containsKey("status")) {
				status = jsonObject.getInt("status");
			}

			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	public boolean isValid() {
		return suuid.length() > 0 && game.length() > 0 && platform.length() > 0 && serverId > 0 && 
			   channel.length() > 0 && playerId > 0 && puid.length() > 0 && 
			   goodsId.length() > 0 && goodsCount > 0 && orderMoney > 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof HawkOrderEntity) {
			HawkOrderEntity entity = (HawkOrderEntity) o;
			if (myOrder.equals(entity.getMyOrder())) {
				return true;
			}
		}
		return false;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMyOrder() {
		return myOrder;
	}

	public void setMyOrder(String myOrder) {
		this.myOrder = myOrder;
	}

	public String getPayPf() {
		return payPf;
	}

	public void setPayPf(String payPf) {
		this.payPf = payPf;
	}
	
	public String getPfOrder() {
		return pfOrder;
	}

	public void setPfOrder(String pfOrder) {
		this.pfOrder = pfOrder;
	}

	public String getSuuid() {
		return suuid;
	}

	public void setSuuid(String suuid) {
		this.suuid = suuid;
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

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public String getPuid() {
		return puid;
	}

	public void setPuid(String puid) {
		this.puid = puid;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public int getGoodsCount() {
		return goodsCount;
	}

	public void setGoodsCount(int goodsCount) {
		this.goodsCount = goodsCount;
	}

	public int getOrderMoney() {
		return orderMoney;
	}

	public void setOrderMoney(int orderMoney) {
		this.orderMoney = orderMoney;
	}

	public int getPayMoney() {
		return payMoney;
	}

	public void setPayMoney(int payMoney) {
		this.payMoney = payMoney;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public int getAddGold() {
		return addGold;
	}

	public void setAddGold(int addGold) {
		this.addGold = addGold;
	}

	public int getGiftGold() {
		return giftGold;
	}

	public void setGiftGold(int giftGold) {
		this.giftGold = giftGold;
	}
	
	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
