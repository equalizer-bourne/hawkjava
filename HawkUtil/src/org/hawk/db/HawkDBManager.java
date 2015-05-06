package org.hawk.db;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkClassScaner;
import org.hawk.log.HawkLog;
import org.hawk.nativeapi.HawkNativeApi;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;

/**
 * 数据库管理器
 * 
 * @author hawk
 */
public class HawkDBManager implements Runnable {
	/**
	 * 运行状态
	 */
	protected volatile boolean running;
	/**
	 * 配置对象
	 */
	protected Configuration conf;
	/**
	 * db连接会话工厂
	 */
	protected SessionFactory sessionFactory;
	/**
	 * 异步队列存储周期
	 */
	protected int asyncPeriod;
	/**
	 * 更新线程
	 */
	protected Thread asyncThread;
	/**
	 * 异步线程存储队列
	 */
	protected LinkedList<HawkDBEntity> asyncList;
	/**
	 * 数据落地线程池
	 */
	protected HawkThreadPool threadPool;
	/**
	 * 立即落地所有对象
	 */
	protected boolean landImmediately;
	/**
	 * 保留删除数据
	 */
	protected boolean retainDeleteEntity;
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
		asyncList = new LinkedList<HawkDBEntity>();
		retainDeleteEntity = true;
	}

	/**
	 * 初始化数据库配置
	 * 
	 * @param hbmXml
	 * @param connUrl
	 * @param userName
	 * @param passWord
	 * @param entityPackages, 多个package之间用逗号分隔
	 * @return
	 */
	public boolean init(String hbmXml, String connUrl, String userName, String passWord, String entityPackages) {
		// 检测
		if (!HawkNativeApi.checkHawk()) {
			return false;
		}
		
		//加载驱动程序
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		
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
					for (Class<?> entityClass : classList) {
						if (entityClass.getAnnotation(Table.class) != null) {
							this.conf.addAnnotatedClass(entityClass);
							HawkLog.logPrintln("scan database entity: " + entityClass.getSimpleName());
						}
					}
				}
			}

			this.running = true;
			this.sessionFactory = this.conf.buildSessionFactory();
			if (HawkApp.getInstance().getAppCfg().isDebug()) {
				HawkLog.logPrintln(String.format("init database, connUrl: %s, userName: %s, pwd: %s, hbmXml: %s", connUrl, userName, passWord, hbmXml));
			} else {
				HawkLog.logPrintln(String.format("init database, connUrl: %s, userName: ******, pwd: ******, hbmXml: %s", connUrl, hbmXml));
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 开启异步线程
	 * 
	 * @param periodMs
	 * @return
	 */
	public boolean startAsyncThread(int periodMs, int poolSize) {
		if (asyncThread == null) {
			asyncPeriod = periodMs;
			asyncThread = new Thread(this);
			asyncThread.setName("DBManager");
			asyncThread.start();
			
			// 线程池落地存储
			if (poolSize > 1) {
				threadPool = new HawkThreadPool("DBExecutor");
				threadPool.initPool(poolSize);
				threadPool.start();
			}
			return true;
		}
		return false;
	}

	/**
	 * 设置落地周期
	 */
	public void setAsyncPeriod(int asyncPeriod) {
		this.asyncPeriod = asyncPeriod;
	}
	
	/**
	 * 获取落地周期
	 */
	public int getAsyncPeriod() {
		return asyncPeriod;
	}
	
	/**
	 * 获取正常存储的对象列表
	 * 
	 * @return
	 */
	public LinkedList<HawkDBEntity> getAsyncList() {
		return asyncList;
	}
	
	/**
	 * 停止db管理器，并立即db落地处理
	 */
	public void stop() {
		running = false;
		if (asyncThread != null) {
			try {
				asyncThread.join();
				asyncThread = null;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 关闭db管理器
	 */
	public void close() {
		running = false;
		if (asyncThread != null) {
			try {
				asyncThread.join();
				asyncThread = null;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		if (threadPool != null) {
			try {
				threadPool.close(true);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (sessionFactory != null) {
			sessionFactory.close();
			sessionFactory = null;
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
	 * 通知立即落地
	 */
	public void landImmediately() {
		landImmediately = true;
	}
	
	/**
	 * 是否保留删除数据
	 * 
	 * @return
	 */
	public boolean isRetainDeleteEntity() {
		return retainDeleteEntity;
	}

	/**
	 * 设置保留删除数据
	 * 
	 * @param retainDeleteEntity
	 */
	public void setRetainDeleteEntity(boolean retainDeleteEntity) {
		this.retainDeleteEntity = retainDeleteEntity;
	}
	
	/**
	 * 创建对象
	 * 
	 * @param entity
	 */
	public boolean create(HawkDBEntity entity) {
		long beginTimeMs = HawkTime.getMillisecond();
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				session.save(entity);
				trans.commit();
				return true;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > HawkApp.getInstance().getAppCfg().getDbOpTimeout()) {
				HawkLog.logPrintln("dbmanager.create timeout, entity: " + entity.getClass().getSimpleName() + ", costtime: " + costTimeMs);
			}
		}
		return false;
	}

	/**
	 * 删除对象
	 * 
	 * @param entity
	 */
	public boolean delete(HawkDBEntity entity) {
		long beginTimeMs = HawkTime.getMillisecond();
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
				HawkLog.errPrintln("dbmanager delete entity failed: " + entity.getClass().getName() + ", entity: " + entity);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > HawkApp.getInstance().getAppCfg().getDbOpTimeout()) {
				HawkLog.logPrintln("dbmanager.delete timeout, entity: " + entity.getClass().getSimpleName() + ", costtime: " + costTimeMs);
			}
		}
		return false;
	}

	/**
	 * 更新对象
	 * 
	 * @param entity
	 */
	public boolean update(HawkDBEntity entity) {
		if (entity != null) {
			long beginTimeMs = HawkTime.getMillisecond();
			try {
				Session session = getSession();
				Transaction trans = session.beginTransaction();
				try {
					// 对象存储
					if (entity.isInvalid() && !isRetainDeleteEntity()) {
						session.delete(entity);
					} else {
						session.saveOrUpdate(entity);
					}
					trans.commit();
					return true;
				} catch (Exception e) {
					trans.rollback();
					HawkException.catchException(e);
					
					if (entity.isInvalid()) {
						HawkLog.errPrintln("dbmanager update entity failed: " + entity.getClass().getName() + ", entity: " + entity);
					} else {
						HawkLog.errPrintln("dbmanager delete entity failed: " + entity.getClass().getName() + ", entity: " + entity);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			} finally {
				// 检测存储时间
				long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
				if (costTimeMs > HawkApp.getInstance().getAppCfg().getDbOpTimeout()) {
					HawkLog.logPrintln("dbmanager.update timeout, entity: " + entity.getClass().getSimpleName() + ", costtime: " + costTimeMs);
				}
			}
		}
		return false;
	}

	/**
	 * 异步对象更新
	 * 
	 * @param entity
	 */
	public boolean updateAsync(HawkDBEntity entity) {
		// 非异步模式, 切换存储接口
		if (asyncThread == null) {
			return update(entity);
		}
		
		long landTime = HawkTime.getMillisecond() + asyncPeriod;
		try {
			int offset = asyncPeriod / 5;
			landTime += (HawkRand.randInt(offset * 2) - offset); 
		} catch (Exception e) {
		}
		
		if (entity.getEntityState().compareAndSet(0, landTime)) {
			synchronized (asyncList) {
				int index = 0;
				try {
					Iterator<HawkDBEntity> iterator = asyncList.iterator();				
					while (iterator.hasNext()) {
						HawkDBEntity nodeEntity = iterator.next();
						long nodeState = nodeEntity.getEntityState().get();
						if (landTime >= nodeState) {
							if (nodeState <= 0) {
								HawkLog.errPrintln("updateAsync stateError: entity: " + nodeEntity.getClass().getSimpleName());
							}
							break;
						}
						index ++;
					}
				} catch (Exception e) {
					index = 0;
					HawkException.catchException(e);
				}
				
				// 添加到异步队列
				asyncList.add(index, entity);
			}
		} else if (HawkTime.getMillisecond() - entity.getEntityState().get() >= asyncPeriod * 2) {
			HawkLog.errPrintln("updateAsync timeout: entity: " + entity.getClass().getSimpleName() + ", state: " + entity.getEntityState().get());
			// 必须落地避免数据丢失
			update(entity);
		}
		return true;
	}

	/**
	 * 执行sql语句
	 * 
	 * @param hql
	 * @return
	 */
	public int update(String hql) {
		long beginTimeMs = HawkTime.getMillisecond();
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				Query query = getSession().createQuery(hql);
				int result = query.executeUpdate();
				trans.commit();
				return result;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > HawkApp.getInstance().getAppCfg().getDbOpTimeout()) {
				HawkLog.logPrintln("dbmanager.update timeout, hql: " + hql + ", costtime: " + costTimeMs);
			}
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
		long beginTimeMs = HawkTime.getMillisecond();
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
				trans.commit();
				return (T) entity;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > HawkApp.getInstance().getAppCfg().getDbOpTimeout()) {
				HawkLog.logPrintln("dbmanager.fetch timeout, entity: " + entityClass.getSimpleName() + ", hql: " + hql + ", costtime: " + costTimeMs);
			}
		}
		return null;
	}

	/**
	 * 根据查询条件返回指定个数的对象列表。先在远程库中进行查询，返回的对象列表将会被遍历
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> query(String hql, Object... values) {
		long beginTimeMs = HawkTime.getMillisecond();
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
				trans.commit();
				return list;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > HawkApp.getInstance().getAppCfg().getDbOpTimeout()) {
				HawkLog.logPrintln("dbmanager.query timeout, hql: " + hql + ", costtime: " + costTimeMs);
			}
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
	public <T> List<T> limitQuery(String hql, int start, int count, Object... values) {
		long beginTimeMs = HawkTime.getMillisecond();
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
				trans.commit();
				return list;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > HawkApp.getInstance().getAppCfg().getDbOpTimeout()) {
				HawkLog.logPrintln("dbmanager.limitquery timeout, hql: " + hql + ", costtime: " + costTimeMs);
			}
		}
		return null;
	}

	/**
	 * 统计数量
	 * 
	 * @param hql
	 * @param values
	 * @return
	 */
	public long count(String hql, Object... values) {
		long beginTimeMs = HawkTime.getMillisecond();
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
				long count = (Long) query.uniqueResult();
				trans.commit();
				return count;
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > HawkApp.getInstance().getAppCfg().getDbOpTimeout()) {
				HawkLog.logPrintln("dbmanager.count timeout, hql: " + hql + ", costtime: " + costTimeMs);
			}
		}
		return 0;
	}

	/**
	 * 直接执行sql语句
	 * 
	 * @param sql
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> executeQuery(String sql) {
		long beginTimeMs = HawkTime.getMillisecond();
		List<T> list = null;
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				SQLQuery query = session.createSQLQuery(sql);
				list = (List<T>) query.list();
				trans.commit();
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > HawkApp.getInstance().getAppCfg().getDbOpTimeout()) {
				HawkLog.logPrintln("dbmanager.executequery timeout, sql: " + sql + ", costtime: " + costTimeMs);
			}
		}
		return list;
	}

	/**
	 * 直接执行sql语句
	 * 
	 * @param sql
	 * @return
	 */
	public int executeUpdate(String sql) {
		long beginTimeMs = HawkTime.getMillisecond();
		int effectCount = 0;
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				SQLQuery query = session.createSQLQuery(sql);
				effectCount = query.executeUpdate();
				trans.commit();
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > HawkApp.getInstance().getAppCfg().getDbOpTimeout()) {
				HawkLog.logPrintln("dbmanager.executeupdate timeout, sql: " + sql + ", costtime: " + costTimeMs);
			}
		}
		return effectCount;
	}

	/**
	 * 执行insert 语句
	 * 
	 * @param sql
	 * @return
	 */
	public List<Long> executeInsert(final String sql) {
		long beginTimeMs = HawkTime.getMillisecond();
		final List<Long> primaryKeyList = new LinkedList<Long>();
		try {
			Session session = getSession();
			Transaction trans = session.beginTransaction();
			try {
				Work work = new Work() {
					public void execute(Connection connection) throws SQLException {
						PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
						statement.executeUpdate();
						ResultSet rs = statement.getGeneratedKeys();
						rs.first();
						do{
							long id = rs.getLong(1);
							primaryKeyList.add(id);
						}while (rs.next()); 
					}
				};
				
				session.doWork(work);				
				trans.commit();
			} catch (Exception e) {
				trans.rollback();
				HawkException.catchException(e);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > HawkApp.getInstance().getAppCfg().getDbOpTimeout()) {
				HawkLog.logPrintln("dbmanager.executeinsert timeout, sql: " + sql + ", costtime: " + costTimeMs);
			}
		}
		return primaryKeyList;
	}

	/**
	 * 执行异步队列
	 */
	private boolean executeAsyncList() {
		if (landImmediately) {
			landImmediately = false;
			synchronized (asyncList) {
				// 立即落地所有存储对象
				for (HawkDBEntity entity : asyncList) {
					innerAsyncUpdate(entity);
				}
				
				HawkLog.logPrintln("dbmanager.landImmediately, entity count: " + asyncList.size());
				asyncList.clear();
			}			
			return true;
		} else {
			long curTime = HawkTime.getMillisecond();
			// 最近可存储时间
			HawkDBEntity lastEntity = asyncList.peekLast();
			if (lastEntity != null && curTime >= lastEntity.getEntityState().get()) {
				HawkDBEntity entity = null;
				synchronized (asyncList) {
					// 当前需要存储对象
					entity = asyncList.pollLast();
					if (entity != null) {
						// 首先重置状态, 避免并发时差引起落地丢失
						entity.getEntityState().getAndSet(0);
					}
				}
				
				if (entity != null) {
					// 线程存储
					innerAsyncUpdate(entity);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 异步线程调用的内部更新接口, 最终实际更新由此接口产生
	 * 
	 * @param entity
	 */
	private void innerAsyncUpdate(final HawkDBEntity entity) {
		if (threadPool == null) {
			// 直接db落地更新
			update(entity);
		} else {
			// 线程池任务模式存储
			threadPool.addTask(new HawkTask(true) {
				@Override
				protected int run() {
					update(entity);
					return 0;
				}
			});
		}
	}
	
	/**
	 * 数据落地线程
	 */
	@Override
	public void run() {
		while (running) {
			try {
				// 异步队列落地
				if (!executeAsyncList()) {
					HawkOSOperator.osSleep(5);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		// 剩余数据落地
		HawkLog.logPrintln("hawk dbmanager exit dataland start");
		landImmediately = true;
		executeAsyncList();
		HawkLog.logPrintln("hawk dbmanager exit dataland complete");
	}
}
