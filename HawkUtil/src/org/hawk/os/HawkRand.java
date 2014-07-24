package org.hawk.os;

import java.util.Random;

/**
 * 随机数生成封装
 * 
 * @author hawk
 * 
 */
public class HawkRand {
	/**
	 * 控制随机数自生产变量定义
	 */
	private static final int A = 48271;
	private static final int M = 2147483647;
	private static final int Q = M / A;
	private static final int R = M % A;
	private static int State = -1;

	/**
	 * 随机整数
	 * 
	 * @return
	 */
	public static int randInt() {
		if (State < 0) {
			Random random = new Random(System.currentTimeMillis());
			State = random.nextInt();
		}

		int tmpState = A * (State % Q) - R * (State / Q);
		if (tmpState >= 0) {
			State = tmpState;
		} else {
			State = tmpState + M;
		}
		return State;
	}

	/**
	 * 限制上限的随机整数
	 * 
	 * @param max
	 * @return
	 */
	public static int randInt(int max) {
		return randInt() % (max + 1);
	}

	/**
	 * 限制范围的随机整数
	 * 
	 * @param low
	 * @param high
	 * @return
	 * @throws HawkException
	 */
	public static int randInt(int low, int high) throws HawkException {
		if (low > high) {
			throw new HawkException("random range error");
		}
		return randInt(high - low) + low;
	}

	/**
	 * 随机浮点数
	 * 
	 * @return
	 */
	public static float randFloat() {
		return (float) randInt() / (float) M;
	}

	/**
	 * 设置上限的随机浮点数
	 * 
	 * @param max
	 * @return
	 */
	public static float randFloat(float max) {
		return randFloat() * max;
	}

	/**
	 * 设置范围的随机浮点数
	 * 
	 * @param low
	 * @param high
	 * @return
	 * @throws HawkException
	 */
	public static float randFloat(float low, float high) throws HawkException {
		if (low > high) {
			throw new HawkException("random range error");
		}
		return randFloat(high - low) + low;
	}
}
