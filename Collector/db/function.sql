# 注册信息导入设备信息
insert into device(device) select distinct device from register;
update device set game = (select game from register where register.device = device.device limit 1);
update device set platform = (select platform from register where register.device = device.device limit 1);
update device set server = (select server from register where register.device = device.device limit 1);
update device set puid = (select puid from register where register.device = device.device limit 1);
update device set playerid = (select playerid from register where register.device = device.device limit 1);
update device set time = (select time from register where register.device = device.device limit 1);

# 添加date字段用于查询
alter table register add column `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '';
alter table login add column `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '';
alter table recharge add column `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '';
alter table data add column `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '';
alter table device add column `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '';
alter table gold add column `date` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '';

alter table register add index `date_index`(`date`);
alter table login add index `date_index`(`date`);
alter table recharge add index `date_index`(`date`);
alter table data add index `date_index`(`date`);
alter table device add index `date_index`(`date`);
alter table gold add index `date_index`(`date`);

update register set date = substring(time,1,10);
update login set date = substring(time,1,10);
update recharge set date = substring(time,1,10);
update data set date = substring(time,1,10);
update device set date = substring(time,1,10);
update gold set date = substring(time,1,10);

# 添加channel字段用于查询
alter table register add column `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '';
alter table login add column `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '';
alter table recharge add column `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '';
alter table data add column `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '';
alter table device add column `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '';
alter table gold add column `channel` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '';

alter table register add index `channel_index`(`channel`);
alter table login add index `channel_index`(`channel`);
alter table recharge add index `channel_index`(`channel`);
alter table data add index `channel_index`(`channel`);
alter table device add index `channel_index`(`channel`);
alter table gold add index `channel_index`(`channel`);

update register set channel = substring(puid,1,LOCATE('_', puid)-1);
update login set channel = substring(puid,1,LOCATE('_', puid)-1);
update recharge set channel = substring(puid,1,LOCATE('_', puid)-1);
update data set channel = substring(puid,1,LOCATE('_', puid)-1);
update device set channel = substring(puid,1,LOCATE('_', puid)-1);
update gold set channel = substring(puid,1,LOCATE('_', puid)-1);

alter table login add column `period` int(11) NOT NULL DEFAULT 0;

