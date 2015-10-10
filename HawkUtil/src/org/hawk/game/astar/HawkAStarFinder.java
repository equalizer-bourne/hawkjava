package org.hawk.game.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.game.HawkMapPos;

/**
 * AStar寻路
 * 
 * @author hawk
 */
public class HawkAStarFinder {
	// 垂直方向或水平方向移动的路径评分
	private final int COST_STRAIGHT = 10;
	// 斜方向移动的路径评分
	private final int COST_DIAGONAL = 14;

	// 地图(1可通过 0不可通过)
	private int[][] map;
	// 地图行
	private int row;
	// 地图列
	private int column;
	// 开启列表
	private List<HawkPathNode> openList;
	// 关闭列表
	private List<HawkPathNode> closeList;

	public HawkAStarFinder(int[][] map, int row, int column) {
		this.map = map;
		this.row = row;
		this.column = column;
		openList = new ArrayList<HawkPathNode>();
		closeList = new ArrayList<HawkPathNode>();
	}

	/**
	 * 寻路（-1：错误，0：没找到，1：找到了）
	 * 
	 * @param sx
	 * @param sy
	 * @param ex
	 * @param ey
	 * @param path
	 * @return
	 */
	public int search(int sx, int sy, int ex, int ey, List<HawkMapPos> path) {
		return search(new HawkMapPos(sx, sy),  new HawkMapPos(ex, ey), path);
	}
	
	/**
	 * 寻路（-1：错误，0：没找到，1：找到了）
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public int search(HawkMapPos startPos, HawkMapPos endPos, List<HawkMapPos> path) {
		if (startPos.getX() < 0 || startPos.getX() >= row || 
			startPos.getY() < 0 || startPos.getY() >= column || 
			endPos.getX() < 0 || endPos.getX() >= row || 
			endPos.getY() < 0 || endPos.getY() >= column) {
			return -1;
		}

		if (map[startPos.getX()][startPos.getY()] == 0 || map[endPos.getX()][endPos.getY()] == 0) {
			return -1;
		}

		HawkPathNode sNode = new HawkPathNode(startPos.getX(), startPos.getY(), null);
		HawkPathNode eNode = new HawkPathNode(endPos.getX(), endPos.getY(), null);
		openList.add(sNode);

		// 寻路
		List<HawkPathNode> resultList = search(sNode, eNode);
		if (resultList.size() == 0) {
			return 0;
		}

		// 复制路径数据
		if (path != null) {
			for (HawkPathNode node : resultList) {
				path.add(new HawkMapPos(node.getX(), node.getY()));
			}
		}
		
		// 标志地图数据
		for (HawkPathNode node : resultList) {
			map[node.getX()][node.getY()] = -1;
		}

		return 1;
	}

	// 查找核心算法
	private List<HawkPathNode> search(HawkPathNode sNode, HawkPathNode eNode) {
		List<HawkPathNode> resultList = new ArrayList<HawkPathNode>();
		boolean isFind = false;
		HawkPathNode node = null;
		while (openList.size() > 0) {
			// 取出开启列表中最低F值，即第一个存储的值的F为最低的
			node = openList.get(0);

			// 判断是否找到目标点
			if (node.getX() == eNode.getX() && node.getY() == eNode.getY()) {
				isFind = true;
				break;
			}

			// 上
			if ((node.getY() - 1) >= 0) {
				checkPath(node.getX(), node.getY() - 1, node, eNode, COST_STRAIGHT);
			}

			// 下
			if ((node.getY() + 1) < column) {
				checkPath(node.getX(), node.getY() + 1, node, eNode, COST_STRAIGHT);
			}

			// 左
			if ((node.getX() - 1) >= 0) {
				checkPath(node.getX() - 1, node.getY(), node, eNode, COST_STRAIGHT);
			}

			// 右
			if ((node.getX() + 1) < row) {
				checkPath(node.getX() + 1, node.getY(), node, eNode, COST_STRAIGHT);
			}

			// 左上
			if ((node.getX() - 1) >= 0 && (node.getY() - 1) >= 0) {
				checkPath(node.getX() - 1, node.getY() - 1, node, eNode, COST_DIAGONAL);
			}

			// 左下
			if ((node.getX() - 1) >= 0 && (node.getY() + 1) < column) {
				checkPath(node.getX() - 1, node.getY() + 1, node, eNode, COST_DIAGONAL);
			}

			// 右上
			if ((node.getX() + 1) < row && (node.getY() - 1) >= 0) {
				checkPath(node.getX() + 1, node.getY() - 1, node, eNode, COST_DIAGONAL);
			}

			// 右下
			if ((node.getX() + 1) < row && (node.getY() + 1) < column) {
				checkPath(node.getX() + 1, node.getY() + 1, node, eNode, COST_DIAGONAL);
			}

			// 从开启列表中删除, 并添加到关闭列表中
			closeList.add(openList.remove(0));
			// 开启列表中排序，把F值最低的放到最底端
			Collections.sort(openList, new HawkPathNode.NodeComparator());
		}

		if (isFind) {
			getPath(resultList, node);
		}
		return resultList;
	}

	/**
	 * 打印路径信息
	 */
	public void printResult() {
		for (int x = 0; x < row; x++) {
			for (int y = 0; y < column; y++) {
				if (map[x][y] == 1) {
					System.out.print("口");
				} else if (map[x][y] == 0) {
					System.out.print("▇");
				} else if (map[x][y] == -1) {
					System.out.print("〓");
				}
			}
			System.out.println();
		}
	}
	
	/**
	 * 查询此路是否能走通
	 * 
	 * @param x
	 * @param y
	 * @param parent
	 * @param eNode
	 * @param cost
	 * @return
	 */
	private boolean checkPath(int x, int y, HawkPathNode parent, HawkPathNode eNode, int cost) {
		HawkPathNode node = new HawkPathNode(x, y, parent);

		// 查找地图中是否能通过
		if (map[x][y] == 0) {
			closeList.add(node);
			return false;
		}

		// 查找关闭列表中是否存在
		if (isListContains(closeList, x, y) != -1) {
			return false;
		}

		// 查找开启列表中是否存在
		int index = -1;
		if ((index = isListContains(openList, x, y)) != -1) {
			// G值是否更小，即是否更新G，F值
			if ((parent.getG() + cost) < openList.get(index).getG()) {
				node.setParent(parent);
				countG(node, eNode, cost);
				countF(node);
				openList.set(index, node);
			}
		} else {
			// 添加到开启列表中
			node.setParent(parent);
			count(node, eNode, cost);
			openList.add(node);
		}
		return true;
	}

	/**
	 * 集合中是否包含某个元素(-1：没有找到，否则返回所在的索引)
	 * 
	 * @param list
	 * @param x
	 * @param y
	 * @return
	 */
	private int isListContains(List<HawkPathNode> list, int x, int y) {
		for (int i = 0; i < list.size(); i++) {
			HawkPathNode node = list.get(i);
			if (node.getX() == x && node.getY() == y) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 从终点往返回到起点
	 * 
	 * @param resultList
	 * @param node
	 */
	private void getPath(List<HawkPathNode> resultList, HawkPathNode node) {
		if (node.getParent() != null) {
			getPath(resultList, node.getParent());
		}
		resultList.add(node);
	}

	/**
	 * 计算G,H,F值
	 * 
	 * @param node
	 * @param eNode
	 * @param cost
	 */
	private void count(HawkPathNode node, HawkPathNode eNode, int cost) {
		countG(node, eNode, cost);
		countH(node, eNode);
		countF(eNode);
	}

	/**
	 * 计算G值
	 * 
	 * @param node
	 * @param eNode
	 * @param cost
	 */
	private void countG(HawkPathNode node, HawkPathNode eNode, int cost) {
		if (node.getParent() == null) {
			node.setG(cost);
		} else {
			node.setG(node.getParent().getG() + cost);
		}
	}

	/**
	 * 计算H值
	 * 
	 * @param node
	 * @param eNode
	 */
	private void countH(HawkPathNode node, HawkPathNode eNode) {
		node.setH(Math.abs(node.getX() - eNode.getX()) + Math.abs(node.getY() - eNode.getY()));
	}

	/**
	 * 计算F值
	 * 
	 * @param node
	 */
	private void countF(HawkPathNode node) {
		node.setF(node.getG() + node.getF());
	}
	
	/**
	 * 测试代码
	 */
	public static void astarTest() {
		int[][] map=new int[][]{
                {1,1,1,1,1,1,1,1,1,1},
                {1,1,1,1,0,1,1,1,1,1},
                {1,1,1,1,0,1,1,1,1,1},
                {1,1,1,1,0,1,1,1,1,1},
                {1,1,1,1,1,1,1,1,1,1},
                {1,1,1,1,0,1,1,1,1,1}
        };

		HawkAStarFinder finder = new HawkAStarFinder(map, 6, 10);
		if (finder.search(4, 0, 3, 8, null) > 0) {
			finder.printResult();
		}
	}
}
