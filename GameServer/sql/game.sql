/*
Navicat MySQL Data Transfer

Source Server         : 本地mysql
Source Server Version : 50525
Source Host           : localhost:3306
Source Database       : game

Target Server Type    : MYSQL
Target Server Version : 50525
File Encoding         : 65001

Date: 2014-06-24 17:58:12
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `player`
-- ----------------------------
DROP TABLE IF EXISTS `player`;
CREATE TABLE `player` (
  `id` varchar(128) NOT NULL DEFAULT '0',
  `level` int(11) DEFAULT NULL,
  `name` varchar(2048) DEFAULT NULL,
  `updateTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `invalid` int(11) unsigned zerofill DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

