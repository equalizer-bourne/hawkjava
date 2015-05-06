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
			/*
			List<String> cmds = new ArrayList<String>();
			cmds.add("sh");
			cmds.add("-c");
			cmds.add(cmd);
			*/	
			String line = "";
			Process process = Runtime.getRuntime().exec(cmd);
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
