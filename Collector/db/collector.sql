DROP TABLE IF EXISTS `register`;
CREATE TABLE `register` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game` varchar(64) COLLATE utf8_bin NOT NULL,
  `platform` varchar(64) COLLATE utf8_bin NOT NULL,
  `server` varchar(64) COLLATE utf8_bin NOT NULL,
  `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  `puid` varchar(255) COLLATE utf8_bin NOT NULL,
  `device` varchar(255) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `game_index` (`game`) USING BTREE,
  KEY `platform_index` (`platform`) USING BTREE,
  KEY `server_index` (`server`) USING BTREE,
  KEY `channel_index` (`channel`) USING BTREE,
  KEY `puid_index` (`puid`) USING BTREE,
  KEY `device_index` (`device`) USING BTREE,
  KEY `date_index` (`date`) USING BTREE,
  UNIQUE KEY `unique_register` (`game`, `platform`, `server`, `puid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `login`;
CREATE TABLE `login` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game` varchar(64) COLLATE utf8_bin NOT NULL,
  `platform` varchar(64) COLLATE utf8_bin NOT NULL,
  `server` varchar(64) COLLATE utf8_bin NOT NULL,
  `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  `puid` varchar(255) COLLATE utf8_bin NOT NULL,
  `device` varchar(255) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `playerlevel` int(11) NOT NULL,
  `period` int(11) NOT NULL DEFAULT 0,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `game_index` (`game`) USING BTREE,
  KEY `platform_index` (`platform`) USING BTREE,
  KEY `server_index` (`server`) USING BTREE,
  KEY `channel_index` (`channel`) USING BTREE,
  KEY `puid_index` (`puid`) USING BTREE,
  KEY `device_index` (`device`) USING BTREE,
  KEY `date_index` (`date`) USING BTREE,
  UNIQUE KEY `unique_login` (`game`, `platform`, `server`, `puid`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `recharge`;
CREATE TABLE `recharge` (
  `myorder` varchar(255) COLLATE utf8_bin NOT NULL,
  `pforder` varchar(255) COLLATE utf8_bin NOT NULL,
  `game` varchar(64) COLLATE utf8_bin NOT NULL,
  `platform` varchar(64) COLLATE utf8_bin NOT NULL,
  `server` varchar(64) COLLATE utf8_bin NOT NULL,
  `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  `puid` varchar(255) COLLATE utf8_bin NOT NULL,
  `device` varchar(255) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `playername` varchar(255) COLLATE utf8_bin NOT NULL,
  `playerlevel` int(11) NOT NULL DEFAULT 0,
  `productid` varchar(255) COLLATE utf8_bin NOT NULL,
  `orderMoney` int(11) NOT NULL,
  `payMoney` int(11) NOT NULL,
  `addGold` int(11) NOT NULL,
  `giftGold` int(11) NOT NULL,
  `currency` varchar(255) COLLATE utf8_bin DEFAULT '',
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`myorder`),
  UNIQUE KEY `unique_pforder` (`pforder`),
  KEY `game_index` (`game`) USING BTREE,
  KEY `platform_index` (`platform`) USING BTREE,
  KEY `server_index` (`server`) USING BTREE,
  KEY `channel_index` (`channel`) USING BTREE,
  KEY `puid_index` (`puid`) USING BTREE,
  KEY `device_index` (`device`) USING BTREE,
  KEY `playerlevel_index` (`playerlevel`) USING BTREE,
  KEY `currency_index` (`currency`) USING BTREE,
  KEY `date_index` (`date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `data`;
CREATE TABLE `data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game` varchar(64) COLLATE utf8_bin NOT NULL,
  `platform` varchar(64) COLLATE utf8_bin NOT NULL,
  `server` varchar(64) COLLATE utf8_bin NOT NULL,
  `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  `puid` varchar(255) COLLATE utf8_bin NOT NULL,
  `device` varchar(255) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `arg1` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `arg2` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `arg3` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `arg4` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `arg5` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `arg6` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `arg7` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `arg8` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `arg9` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `game_index` (`game`) USING BTREE,
  KEY `platform_index` (`platform`) USING BTREE,
  KEY `server_index` (`server`) USING BTREE,
  KEY `channel_index` (`channel`) USING BTREE,
  KEY `puid_index` (`puid`) USING BTREE,
  KEY `device_index` (`device`) USING BTREE,
  KEY `date_index` (`date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game` varchar(64) COLLATE utf8_bin NOT NULL,  
  `device` varchar(255) COLLATE utf8_bin NOT NULL,
  `platform` varchar(64) COLLATE utf8_bin NOT NULL,
  `server` varchar(64) COLLATE utf8_bin NOT NULL,
  `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  `puid` varchar(255) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `game_index` (`game`) USING BTREE,  
  KEY `device_index` (`device`) USING BTREE,
  KEY `platform_index` (`platform`) USING BTREE,
  KEY `server_index` (`server`) USING BTREE,
  KEY `channel_index` (`channel`) USING BTREE,
  KEY `date_index` (`date`) USING BTREE,
  UNIQUE KEY `unique_device` (`game`, `device`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `puid`;
CREATE TABLE `puid` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game` varchar(64) COLLATE utf8_bin NOT NULL,  
  `puid` varchar(255) COLLATE utf8_bin NOT NULL,
  `platform` varchar(64) COLLATE utf8_bin NOT NULL,
  `server` varchar(64) COLLATE utf8_bin NOT NULL,
  `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  `device` varchar(255) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `game_index` (`game`) USING BTREE,  
  KEY `puid_index` (`puid`) USING BTREE,
  KEY `platform_index` (`platform`) USING BTREE,
  KEY `server_index` (`server`) USING BTREE,
  KEY `channel_index` (`channel`) USING BTREE,
  KEY `date_index` (`date`) USING BTREE,
  UNIQUE KEY `unique_device` (`game`, `puid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `server`;
CREATE TABLE `server` (
  `game` varchar(64) COLLATE utf8_bin NOT NULL,
  `platform` varchar(64) COLLATE utf8_bin NOT NULL,
  `server` varchar(64) COLLATE utf8_bin NOT NULL,
  `ip` varchar(64) COLLATE utf8_bin NOT NULL,
  `localip` varchar(64) COLLATE utf8_bin NOT NULL,
  `folder` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `listen_port` int(11) NOT NULL DEFAULT 0,
  `script_port` int(11) NOT NULL DEFAULT 0,
  `dburl` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `dbuser` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `dbpwd` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`game`, `platform`, `server`),
  KEY `game_index` (`game`) USING BTREE,
  KEY `platform_index` (`platform`) USING BTREE,
  KEY `server_index` (`server`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `gold`;
CREATE TABLE `gold` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game` varchar(64) COLLATE utf8_bin NOT NULL,
  `platform` varchar(64) COLLATE utf8_bin NOT NULL,
  `server` varchar(64) COLLATE utf8_bin NOT NULL,
  `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  `puid` varchar(255) COLLATE utf8_bin NOT NULL,
  `device` varchar(255) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `playerlevel` int(11) NOT NULL DEFAULT 0,
  `changetype` int(11) NOT NULL DEFAULT 0,
  `changeaction` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `goldtype` int(11) NOT NULL,
  `gold` int(11) NOT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `game_index` (`game`) USING BTREE,
  KEY `platform_index` (`platform`) USING BTREE,
  KEY `server_index` (`server`) USING BTREE,
  KEY `channel_index` (`channel`) USING BTREE,
  KEY `puid_index` (`puid`) USING BTREE,
  KEY `device_index` (`device`) USING BTREE,
  KEY `playerlevel_index` (`playerlevel`) USING BTREE,
  KEY `changetype_index` (`changetype`) USING BTREE,
  KEY `changeaction_index` (`changeaction`) USING BTREE,
  KEY `goldtype_index` (`goldtype`) USING BTREE,
  KEY `date_index` (`date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `tutorial`;
CREATE TABLE `tutorial` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game` varchar(64) COLLATE utf8_bin NOT NULL,
  `platform` varchar(64) COLLATE utf8_bin NOT NULL,
  `server` varchar(64) COLLATE utf8_bin NOT NULL,
  `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  `puid` varchar(255) COLLATE utf8_bin NOT NULL,
  `device` varchar(255) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `playerlevel` int(11) NOT NULL DEFAULT 0,
  `step` int(11) NOT NULL,
  `args` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `game_index` (`game`) USING BTREE,
  KEY `platform_index` (`platform`) USING BTREE,
  KEY `server_index` (`server`) USING BTREE,
  KEY `channel_index` (`channel`) USING BTREE,
  KEY `puid_index` (`puid`) USING BTREE,
  KEY `device_index` (`device`) USING BTREE,
  KEY `playerlevel_index` (`playerlevel`) USING BTREE,
  KEY `step_index` (`step`) USING BTREE,
  KEY `date_index` (`date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `statistics`;
CREATE TABLE `statistics` (
  `date` varchar(32) COLLATE utf8_bin NOT NULL,
  `game` varchar(64) COLLATE utf8_bin NOT NULL,
  `platform` varchar(64) COLLATE utf8_bin NOT NULL,
  `channel` varchar(64) COLLATE utf8_bin NOT NULL,
  `statistics` varchar(8192) DEFAULT NULL,
  PRIMARY KEY (`date`, `game`, `platform`, `channel`),
  KEY `date_index` (`date`) USING BTREE,
  KEY `game_index` (`game`) USING BTREE,
  KEY `platform_index` (`platform`) USING BTREE,
  KEY `channel_index` (`channel`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game` varchar(64) COLLATE utf8_bin NOT NULL,
  `platform` varchar(64) COLLATE utf8_bin NOT NULL,
  `server` varchar(64) COLLATE utf8_bin NOT NULL,
  `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  `puid` varchar(255) COLLATE utf8_bin NOT NULL,
  `device` varchar(255) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `playerlevel` int(11) NOT NULL DEFAULT 0,
  `activityid` int(11) NOT NULL,
  `activityno` int(11) NOT NULL,
  `jointimes` int(11) NOT NULL,
  `consumegold` int(11) NOT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `game_index` (`game`) USING BTREE,
  KEY `platform_index` (`platform`) USING BTREE,
  KEY `server_index` (`server`) USING BTREE,
  KEY `channel_index` (`channel`) USING BTREE,
  KEY `puid_index` (`puid`) USING BTREE,
  KEY `device_index` (`device`) USING BTREE,
  KEY `playerlevel_index` (`playerlevel`) USING BTREE,
  KEY `activityid_index` (`activityid`) USING BTREE,
  KEY `activityno_index` (`activityno`) USING BTREE,
  KEY `date_index` (`date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
