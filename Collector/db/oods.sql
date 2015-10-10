DROP TABLE IF EXISTS `game`;
CREATE TABLE `game` (
  `game` varchar(64) COLLATE utf8_bin NOT NULL,
  `platform` varchar(64) COLLATE utf8_bin NOT NULL,
  `channel` varchar(2048) COLLATE utf8_bin DEFAULT NULL,
  `logUserName` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `logUserPwd` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `logPath` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `sshPort` int(11) NOT NULL DEFAULT '0',
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`game`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

