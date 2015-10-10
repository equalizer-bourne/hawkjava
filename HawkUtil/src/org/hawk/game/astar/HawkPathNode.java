package org.hawk.game.astar;

import java.util.Comparator;

public class HawkPathNode implements Comparator<HawkPathNode> {
	/**
	 * 节点比较类
	 * 
	 * @author hawk
	 */
	public static class NodeComparator implements Comparator<HawkPathNode> {
		@Override
		public int compare(HawkPathNode o1, HawkPathNode o2) {
			return o1.getF() - o2.getF();
		}
	}

	// X坐标
	private int x;
	// Y坐标
	private int y;
	// 当前点到起点的移动耗费
	private int g;
	// 当前点到终点的移动耗费，即曼哈顿距离|x1-x2|+|y1-y2|(忽略障碍物)
	private int h;
	// f=g+h
	private int f;
	// 父类节点
	private HawkPathNode parent;

	public HawkPathNode(int x, int y, HawkPathNode parent) {
		this.x = x;
		this.y = y;
		this.parent = parent;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}

	public int getF() {
		return f;
	}

	public void setF(int f) {
		this.f = f;
	}

	public HawkPathNode getParent() {
		return parent;
	}

	public void setParent(HawkPathNode parent) {
		this.parent = parent;
	}

	@Override
	public int compare(HawkPathNode o1, HawkPathNode o2) {
		return o1.getF() - o2.getF();
	}
}
