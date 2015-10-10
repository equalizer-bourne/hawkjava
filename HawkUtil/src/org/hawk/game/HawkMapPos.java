package org.hawk.game;

/**
 * 地图位置
 * 
 * @author hawk
 */
public class HawkMapPos {
	private int x;
	private int y;

	public HawkMapPos() {
		this.x = 0;
		this.y = 0;
	}
	
	public HawkMapPos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public HawkMapPos setX(int x) {
		this.x = x;
		return this;
	}

	public int getY() {
		return y;
	}

	public HawkMapPos setY(int y) {
		this.y = y;
		return this;
	}
	
	public HawkMapPos add(HawkMapPos pos) {
		return add(pos.x, pos.y);
	}

	public HawkMapPos add(int x, int y) {
		this.x += x;
		this.y += y;
		return this;
	}
}
