package org.hawk.db.cache;

import java.util.Date;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

public class HawkMemCacheDB {
	/**
	 * MC客户端
	 */
	private MemCachedClient memCachedClient = null;
	/**
	 * redis 客户端连接池
	 */
	private JedisPool jedisPool;
	
	/**
	 * 初始化memcached客户端
	 * 
	 * @param addr
	 * @param timeout
	 * @return
	 */
	public boolean initAsMemCached(String addr, int timeout) {
		try {
			SockIOPool pool = SockIOPool.getInstance();
			pool.setServers(new String[] { addr });
			pool.setSocketTO(timeout);
			pool.initialize();
			HawkLog.logPrintln("memcache sockio pool initialize, addr: " + addr + ", timeout: " + timeout);
			memCachedClient = new MemCachedClient();
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 初始化redis客户端
	 * 
	 * @param addr
	 * @param port
	 * @return
	 */
	public boolean initAsRedis(String addr, int port) {
		try {
			jedisPool = new JedisPool(addr, port);
			HawkLog.logPrintln("jedis pool initialize, addr: " + addr + ", port: " + port);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		if (memCachedClient != null) {
			Object value = memCachedClient.get(key);
			if (value != null) {
				return (String)value;
			}
		} else if (jedisPool != null) {
			try (Jedis jedis = jedisPool.getResource()) {
				return jedis.get(key);
			}
		}
		return null;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public byte[] getBytes(String key) {
		if (memCachedClient != null) {
			Object value = memCachedClient.get(key);
			if (value != null) {
				return (byte[])value;
			}
		} else if (jedisPool != null) {
			try (Jedis jedis = jedisPool.getResource()) {
				return jedis.get(key.getBytes());
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setString(String key, String value) {
		if (memCachedClient != null) {
			return memCachedClient.set(key, value);
		} else if (jedisPool != null) {
			try (Jedis jedis = jedisPool.getResource()) {
				return jedis.set(key, (String)value) != null;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setBytes(String key, byte[] value) {
		if (memCachedClient != null) {
			return memCachedClient.set(key, value);
		} else if (jedisPool != null) {
			try (Jedis jedis = jedisPool.getResource()) {
				return jedis.set(key.getBytes(), value) != null;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setString(String key, String value, int expireSeconds) {
		if (memCachedClient != null) {
			Date expiry = HawkTime.getCalendar().getTime();
			expiry.setTime(expiry.getTime() + expireSeconds * 1000);
			return memCachedClient.set(key, value, expiry);
		} else if (jedisPool != null) {
			try (Jedis jedis = jedisPool.getResource()) {
				jedis.set(key, (String)value);
				jedis.expire(key, expireSeconds);
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setBytes(String key, byte[] value, int expireSeconds) {
		if (memCachedClient != null) {
			Date expiry = HawkTime.getCalendar().getTime();
			expiry.setTime(expiry.getTime() + expireSeconds * 1000);
			return memCachedClient.set(key, value, expiry);
		} else if (jedisPool != null) {
			try (Jedis jedis = jedisPool.getResource()) {
				jedis.set(key.getBytes(), value);
				jedis.expire(key.getBytes(), expireSeconds);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 存在key
	 * 
	 * @param key
	 * @return
	 */
	public boolean exists(String key) {
		if (memCachedClient != null) {
			return memCachedClient.keyExists(key);
		} else if (jedisPool != null) {
			try (Jedis jedis = jedisPool.getResource()) {
				return jedis.exists(key);
			}
		}
		return false;
	}
	
	/**
	 * 删除key
	 * 
	 * @param key
	 * @return
	 */
	public boolean delete(String key) {
		if (memCachedClient != null) {
			return memCachedClient.delete(key);
		} else if (jedisPool != null) {
			try (Jedis jedis = jedisPool.getResource()) {
				return jedis.del(key) > 0;
			}
		}
		return false;
	}
}
