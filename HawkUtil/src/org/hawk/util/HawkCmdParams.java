package org.hawk.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.os.HawkException;

/**
 * 通用命令行参数
 * 
 * @author hawk
 */
public class HawkCmdParams {
	public String cmd;
	public Map<String, String> params;

	private HawkCmdParams() {
		cmd = "";
		params = new HashMap<String, String>();
	}

	public String getCmd() {
		return cmd;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getParam(String key) {
		if (params.containsKey(key)) {
			return params.get(key);
		}
		return null;
	}

	public boolean existParam(String key) {
		return params.containsKey(key);
	}

	public void removeParam(String key) {
		params.remove(key);
	}
	
	public boolean isValid() {
		return cmd != null && cmd.length() > 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(4096);
		builder.append(cmd).append(" ");
		for (Entry<String, String> entry : params.entrySet()) {
			builder.append("--").append(entry.getKey()).append(" ");
			if (entry.getValue() != null && entry.getValue().length() > 0) {
				builder.append(entry.getValue()).append(" ");
			}
		}
		return builder.toString();
	}
	
	public static HawkCmdParams valueOf(String args) {
		HawkCmdParams commandParams = new HawkCmdParams();
		try {
			args = args.trim();
			int pos = args.indexOf(" ");
			if (pos > 0) {
				commandParams.cmd = args.substring(0, pos);
				String options[] = args.substring(pos + 1).trim().split("--");
				for (String opt : options) {
					opt = opt.trim();
					if (opt.length() > 0) {
						pos = opt.indexOf(" ");
						if (pos > 0) {
							commandParams.params.put(opt.substring(0, pos), opt.substring(pos + 1).trim());
						} else {
							commandParams.params.put(opt, null);
						}
					}
				}
			} else {
				commandParams.cmd = args;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		if (commandParams.isValid()) {
			return commandParams;
		}
		return null;
	}
}
