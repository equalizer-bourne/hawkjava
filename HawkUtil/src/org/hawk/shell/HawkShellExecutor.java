package org.hawk.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.hawk.os.HawkException;

public class HawkShellExecutor {
	/**
	 * 执行shell命令
	 * 
	 * @param cmd
	 * @return
	 */
	public static String execute(String cmd, long timeout) {
		try {
			String[] cmds = null;
			if (cmd.startsWith("sh -c ")) {
				cmd = cmd.substring(6).trim();
				cmds = new String[]{"sh", "-c", cmd};
			}
			
			String line = "";
			Process process = null;
			if (cmds == null) {
				process = Runtime.getRuntime().exec(cmd);
			} else {
				process = Runtime.getRuntime().exec(cmds);
			}
			
			try {
				// 等待进程退出
				if (timeout <= 0) {
					process.waitFor();
				} else {
					synchronized (process) {
						process.wait(timeout);
					}
				}
				
				StringBuilder builder = new StringBuilder(4096);
				BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
				while ((line = input.readLine()) != null) {
					builder.append(line).append("\r\n");
				}
				input.close();
				
				String result = builder.toString();
				if (result == null || result.length() <= 0) {
					result = "success";
				}
				return result;
			} catch (Exception e) {
				HawkException.catchException(e);
			} finally {
				process.destroy();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return "failed";
	}
}
