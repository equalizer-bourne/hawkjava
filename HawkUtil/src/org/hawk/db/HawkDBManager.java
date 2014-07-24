package org.hawk.db;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.Entity;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkClassScaner;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * 数据库管理器
 * 
 * @author hawk
 */
public class HawkDBManager implements Runnable {
	/**
	 * 配置对象
	 */
	protected Configuration conf;
	/**
	 * db连接会话工厂
	 */
	protected SessionFactory sessionFactory;
	/**
	 * 更新线程
	 */
	protected Thread asyncThread;
	/**
	 * 运行状态
	 */
	volatile boolean running;
	/**
	 * 异步队列锁
	 */
	private Lock asyncLock;
	/**
	 * 异步对象队列
	 */
	List<Object> asyncList;
	/**
	 * 异步线程存储队列
	 */
	List<Object> entityList;
	/**
	 * 异步队列表, 避免重复对象
	 */
	Map<Object, Object> asyncMap;
	/**
	 * 异步队列存储周期
	 */
	private int asyncPeriod;

	/**
	 * 单例使用
	 */
	static HawkDBManager instance;

	/**
	 * 获取全局管理器
	 * 
	 * @return
	 */
	public static HawkDBManager getInstance() {
		if (instance == null) {
			instance = new HawkDBManager();
		}
		return instance;
	}

	/**
	 * 默认构造函数
	 */
	private HawkDBManager() {
		asyncLock = new ReentrantLock();
		asyncList = new LinkedList<Object>();
		entityList = new LinkedList<Object>();
		asyncMap = new HashMap<Object, Object>();
	}

	/**
	 * 初始化数据库配置
	 * @param hbmXml
	 * @param connUrl
	 * @param userName
	 * @param passWord
	 * @param entityPackages, 多个package之间用逗号分隔
	 * @return
	 */
	public boolean init(String hbmXml, String connUrl, String userName, String passWord, String entityPackages) {
		try {
			if (this.conf == null) {
				String fileName = HawkApp.getInstance().getWorkPath() + hbmXml;
				this.conf = new Configuration();
				this.conf.configure(new File(fileName));
			}

			// 重新数据库设置
			this.conf.setProperty("hibernate.connection.url", connUrl);
			this.conf.setProperty("hibernate.connection.username", userName);
			this.conf.setProperty("hibernate.connection.password", passWord);
			
			String[] entityPackageArray = entityPackages.split(",");
			if (entityPackageArray != null) {
				for (String entityPackage : entityPackageArray) {
					HawkLog.logPrintln("init entity package: " + entityPackage);
					List<Class<?>> classList = HawkClassScaner.scanClassesFilter(entityPackage.trim(), Entity.class);
					for(Class<?> entityClass : classList) {
						this.conf.addAnnotatedClass(entityClass);
					}
				}
			}
			
			this.sessionFactory = this.conf.buildSessionFactory();
			this.running = true;
			
			HawkLog.logPrintln(String.format("init database, connUrl: %s, userName: %s, pwd: %s, hbmXml: %s", connUrl, userName, passWord, hbmXml));
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	public boolean startAsyncThread(int periodMs) {
		if (asyncThread == null) {
			asyncPeriod = periodMs;
			asyncThread = new Thread(this);
			asyncThread.setName("DBManager");
			asyncThread.start();
			return true;
		}
		return false;
	}

	/**
	 * 关闭db管理器
	 */
	public void close() {
		running = false;
		if (asyncThread != null) {
			try {
				asyncThread.join();
			} catch (InterruptedException e) {
				HawkException.catchException(e);
			}
		}
		
		if (sessionFactory != null) {
			sessionFactory.close();
		}
	}

	/**
	 * 获取db会话
	 * 
	 * @return
	 */
	public Session getSession() {
		if (sessionFactory != null) {
			return sessionFactory.getCurrentSession();
		}
		return null;
	}

	/**
	 * 创建对象
	 * 
	 * @param entity
	 */
	public boolean create(Object entity) {
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				session.saveOrUpdate(entity);
				trans.commit();
				return true;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 删除对象
	 * 
	 * @param entity
	 */
	public boolean delete(Object entity) {
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				session.delete(entity);
				trans.commit();
				return true;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 更新对象
	 * 
	 * @param entity
	 */
	public boolean update(Object entity) {
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				session.saveOrUpdate(entity);
				trans.commit();
				return true;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 异步对象更新
	 * 
	 * @param entity
	 */
	public boolean updateAsync(Object entity) {
		asyncLock.lock();
		try {
			if (!asyncMap.containsKey(entity)) {
				asyncMap.put(entity, entity);
				asyncList.add(entity);
				return true;
			}
		} finally {
			asyncLock.unlock();
		}
		return false;
	}

	/**
	 * 执行sql语句
	 * 
	 * @param hql
	 * @return
	 */
	public int update(String hql) {
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				Query query = getSession().createQuery(hql);
				int result = query.executeUpdate();
				session.clear();
				trans.commit();
				return result;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 根据条件返回指定的对象
	 * 
	 * @param entityClass
	 * @param hql
	 * @param values
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T fetch(Class<T> entityClass, String hql, Object... values) {
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				Query query = getSession().createQuery(hql);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						query.setParameter(i, values[i]);
					}
				}
				Object entity = query.uniqueResult();
				session.clear();
				trans.commit();
				return (T) entity;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 根据查询条件返回指定个数的对象列表
	 * 
	 * @param entityClass
	 * @param hql
	 * @param values
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> query(Class<T> entityClass, String hql, Object... values) {
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				Query query = getSession().createQuery(hql);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						query.setParameter(i, values[i]);
					}
				}
				List<T> list = (List<T>) query.list();
				session.clear();
				trans.commit();
				return list;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 根据查询条件返回指定个数的对象列表。先在远程库中进行查询，返回的对象列表将会被遍历
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T> List<T> query(String hql, Object... values) {
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				Query query = getSession().createQuery(hql);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						query.setParameter(i, values[i]);
					}
				}
				List<T> list = (List<T>) query.list();
				session.clear();
				trans.commit();
				return list;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 根据查询条件返回指定个数的对象列表
	 * 
	 * @param entityClass
	 * @param hql
	 * @param start
	 * @param count
	 * @param values
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> limitQuery(Class<T> entityClass, String hql, int start, int count, Object... values) {
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				Query query = getSession().createQuery(hql);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						query.setParameter(i, values[i]);
					}
				}
				query.setFirstResult(start);
				query.setMaxResults(count);
				List<T> list = (List<T>) query.list();
				session.clear();
				trans.commit();
				return list;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 根据查询条件返回指定个数的对象列表
	 * 
	 * @param hql
	 * @param start
	 * @param count
	 * @param values
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T> List<T> limitQuery(String hql, int start, int count, Object... values) {
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				Query query = getSession().createQuery(hql);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						query.setParameter(i, values[i]);
					}
				}
				query.setFirstResult(start);
				query.setMaxResults(count);
				List<T> list = (List<T>) query.list();
				session.clear();
				trans.commit();
				return list;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 执行异步队列
	 */
	private void executeAsyncList() {
		asyncLock.lock();
		try {
			entityList.clear();
			if (asyncList.size() > 0) {
				entityList.addAll(asyncList);
			}
			asyncMap.clear();
			asyncList.clear();
		} finally {
			asyncLock.unlock();
		}

		// 数据存储
		if (entityList != null) {
			for (Object entity : entityList) {
				update(entity);
			}
			entityList.clear();
		}
	}

	@Override
	public void run() {
		while (running) {
			try {
				executeAsyncList();
				
				HawkOSOperator.osSleep(asyncPeriod);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		// 剩余数据落地
		executeAsyncList();
	}
}
