package com.hawk.game.entity;

import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 玩家基础数据
 * @author xulinqs
 *
 */
@Entity
@Table(name = "player")
public class BasicData extends BaseData{
	
	@Column(name = "level")
	private int level ;
	
	@Column(name = "name")
	private String name ;
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
