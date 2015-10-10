package org.hawk.util.services;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThread;

/**
 * 邮件服务
 * 
 * @author hawk
 */
public class HawkEmailService extends HawkThread {
	/**
	 * 服务信息
	 */
	private String emailHost = "";
	private int emailPort = 0;
	private String emailUser = "";
	private String emailPwd = "";
	/**
	 * 服务是否可用
	 */
	private boolean serviceEnable = true;

	/**
	 * 实例对象
	 */
	private static HawkEmailService instance = null;

	/**
	 * 获取全局实例对象
	 * 
	 * @return
	 */
	public static HawkEmailService getInstance() {
		if (instance == null) {
			instance = new HawkEmailService();
		}
		return instance;
	}

	/**
	 * 构造函数
	 */
	private HawkEmailService() {
	}

	/**
	 * 初始化服务
	 * 
	 * @return
	 */
	public boolean install(String host, int port, String user, String pwd) {
		this.emailHost = host;
		this.emailPort = port;
		this.emailUser = user;
		this.emailPwd = pwd;
		// 开启线程
		this.setName("EmailService");
		if (!isRunning()) {
			this.start();
		}
		return true;
	}

	/**
	 * 关闭服务
	 */
	@Override
	public boolean close(boolean waitBreak) {
		enableService(false);
		return super.close(waitBreak);
	}

	/**
	 * 开启或关闭服务
	 * 
	 * @param enable
	 */
	public void enableService(boolean enable) {
		this.serviceEnable = enable;
	}

	/**
	 * 检测是否有效
	 * 
	 * @return
	 */
	public boolean checkValid() {
		return serviceEnable && emailHost.length() > 0 && emailUser.length() > 0;
	}

	/**
	 * 发送邮件
	 * 
	 * @param title
	 * @param content
	 * @param receivers
	 */
	public synchronized void sendEmail(String title, String content, List<String> receivers) {
		if (checkValid()) {
			addTask(new EmailTask(title, content, receivers));
		}
	}

	/**
	 * 邮件任务
	 * 
	 * @author hawk
	 */
	class EmailTask extends HawkTask {
		// 邮件标题
		private String title;
		// 邮件内容
		private String content;
		// 邮件收件人
		private List<String> receivers;

		/**
		 * 构造邮件任务
		 * 
		 * @param title
		 * @param content
		 * @param receivers
		 */
		EmailTask(String title, String content, List<String> receivers) {
			this.title = title;
			this.content = content;
			this.receivers = new LinkedList<String>();
			this.receivers.addAll(receivers);
		}

		/**
		 * 邮件投递
		 */
		@Override
		protected int run() {
			try {
				Email email = new SimpleEmail();
				email.setHostName(emailHost);
				email.setSmtpPort(emailPort);
				email.setAuthentication(emailUser, emailPwd);
				email.setCharset("UTF-8");
				email.setFrom(emailUser);
				for (String receiver : receivers) {
					email.addTo(receiver);
				}
				email.setSubject(title);
				email.setMsg(content);
				email.send();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}
	}
}
