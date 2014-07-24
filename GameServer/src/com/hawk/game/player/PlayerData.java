package com.hawk.game.player;

import java.util.Date;
import java.util.List;

import org.hawk.db.HawkDBManager;

import com.hawk.game.entity.BasicData;

/**
 * 管理所有玩家数据集合
 * @author xulinqs
 *
 */
public class PlayerData {
	
	/**
	 * 玩家基础数据
	 */
	private BasicData basicData ;
	
	public void createPlayerBasicData() {
		basicData = new BasicData();
		basicData.setName("xulin ");
		basicData.setLevel(10);
		basicData.setUpdateTime(new Date());
		basicData.setIsInvalid(false);
		
		HawkDBManager.getInstance().create(basicData);
		
	}
	
	public void loadData() {
		List<BasicData> basicDatas = HawkDBManager.getInstance().query("from BasicData ");
		for(BasicData basicData : basicDatas) {
			System.out.println(basicData);
		}
	}
	
	public BasicData getBasicData() {
		return basicData;
	}

	public void setBasicData(BasicData basicData) {
		this.basicData = basicData;
	} 
	
}

