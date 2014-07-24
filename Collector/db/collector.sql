DROP TABLE IF EXISTS `register`;
CREATE TABLE `register` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game` varchar(256) COLLATE utf8_bin NOT NULL,
  `platform` varchar(256) COLLATE utf8_bin NOT NULL,
  `server` varchar(256) COLLATE utf8_bin NOT NULL,
  `puid` varchar(256) COLLATE utf8_bin NOT NULL,
  `device` varchar(256) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `login`;
CREATE TABLE `login` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game` varchar(256) COLLATE utf8_bin NOT NULL,
  `platform` varchar(256) COLLATE utf8_bin NOT NULL,
  `server` varchar(256) COLLATE utf8_bin NOT NULL,
  `puid` varchar(256) COLLATE utf8_bin NOT NULL,
  `device` varchar(256) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `recharge`;
CREATE TABLE `recharge` (
  `orderid` varchar(256) COLLATE utf8_bin NOT NULL,
  `game` varchar(256) COLLATE utf8_bin NOT NULL,
  `platform` varchar(256) COLLATE utf8_bin NOT NULL,
  `server` varchar(256) COLLATE utf8_bin NOT NULL,
  `puid` varchar(256) COLLATE utf8_bin NOT NULL,
  `device` varchar(256) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `playername` varchar(256) COLLATE utf8_bin NOT NULL,
  `playerlevel` int(11) NOT NULL DEFAULT 0,
  `pay` int(11) NOT NULL,
  `currency` varchar(256) COLLATE utf8_bin DEFAULT '',
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`orderid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `data`;
CREATE TABLE `data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game` varchar(256) COLLATE utf8_bin NOT NULL,
  `platform` varchar(256) COLLATE utf8_bin NOT NULL,
  `server` varchar(256) COLLATE utf8_bin NOT NULL,
  `puid` varchar(256) COLLATE utf8_bin NOT NULL,
  `device` varchar(256) COLLATE utf8_bin NOT NULL,
  `playerid` int(11) NOT NULL,
  `playername` varchar(256) COLLATE utf8_bin NOT NULL,
  `arg1` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `arg2` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `arg3` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `arg4` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `arg5` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `arg6` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `arg7` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `arg8` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `arg9` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
