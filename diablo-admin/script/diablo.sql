-- ----------------------------
-- Table structure for app_auth
-- ----------------------------
DROP TABLE IF EXISTS `app_auth`;
CREATE TABLE `app_auth` (
  `id` varchar(128) NOT NULL COMMENT '主键id',
  `app_key` varchar(32) NOT NULL COMMENT '应用标识key',
  `app_secret` varchar(128) NOT NULL COMMENT '加密算法secret',
  `enabled` tinyint(4) NOT NULL COMMENT '是否删除',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='应用授权表';

-- ----------------------------
-- Table structure for dashboard_user
-- ----------------------------
DROP TABLE IF EXISTS `dashboard_user`;
CREATE TABLE `dashboard_user` (
  `id` varchar(128)  NOT NULL COMMENT '主键id',
  `user_name` varchar(64)  NOT NULL COMMENT '用户名',
  `password` varchar(128)  DEFAULT NULL COMMENT '用户密码',
  `role` int(4) NOT NULL COMMENT '角色',
  `enabled` tinyint(4) NOT NULL COMMENT '是否删除',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='用户表';

-- ----------------------------
-- Table structure for plugin
-- ----------------------------
DROP TABLE IF EXISTS `plugin`;
CREATE TABLE `plugin` (
  `id` varchar(128)  NOT NULL COMMENT '主键id',
  `name` varchar(62)  NOT NULL COMMENT '插件名称',
  `config` text  COMMENT '插件配置',
  `role` int(4) NOT NULL COMMENT '插件角色',
  `enabled` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否开启（0，未开启，1开启）',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='插件表';

-- ----------------------------
-- Table structure for rule
-- ----------------------------
DROP TABLE IF EXISTS `rule`;
CREATE TABLE `rule` (
  `id` varchar(128) NOT NULL COMMENT '主键id',
  `selector_id` varchar(128) NOT NULL COMMENT '选择器id',
  `service_info_id` varchar(128) DEFAULT NULL COMMENT '所属应用id,service_info主键值',
  `match_mode` int(2) NOT NULL COMMENT '匹配方式（0 and  1 or)',
  `name` varchar(128) NOT NULL COMMENT '规则名称',
  `enabled` tinyint(4) NOT NULL COMMENT '是否开启',
  `loged` tinyint(4) NOT NULL COMMENT '是否记录日志',
  `sort` int(4) NOT NULL COMMENT '排序',
  `handle` varchar(1024) DEFAULT NULL COMMENT '处理逻辑（此处针对不同的插件，会有不同的字段来标识不同的处理，所有存储json格式数据）',
  `upstream_handle` varchar(1024) DEFAULT NULL COMMENT '路由主机配置',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_selector_id` (`selector_id`) USING BTREE,
  KEY `idx_service_info_id` (`service_info_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='规则表';

-- ----------------------------
-- Table structure for rule_backup
-- ----------------------------
DROP TABLE IF EXISTS `rule_backup`;
CREATE TABLE `rule_backup` (
  `id` varchar(128) NOT NULL COMMENT '主键id',
  `selector_id` varchar(128) NOT NULL COMMENT '选择器id',
  `service_info_id` varchar(128) DEFAULT NULL COMMENT '所属应用id,service_info主键值',
  `match_mode` int(2) NOT NULL COMMENT '匹配方式（0 and  1 or)',
  `name` varchar(128) NOT NULL COMMENT '规则名称',
  `enabled` tinyint(4) NOT NULL COMMENT '是否开启',
  `loged` tinyint(4) NOT NULL COMMENT '是否记录日志',
  `sort` int(4) NOT NULL COMMENT '排序',
  `handle` varchar(1024) DEFAULT NULL COMMENT '处理逻辑（此处针对不同的插件，会有不同的字段来标识不同的处理，所有存储json格式数据）',
  `upstream_handle` varchar(1024) DEFAULT NULL COMMENT '路由主机配置',
  `date_published` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`,`date_published`),
  KEY `idx_service_info_id` (`service_info_id`) USING BTREE,
  KEY `idx_selector_id_date_published` (`selector_id`,`date_published`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='规则备份历史表';

-- ----------------------------
-- Table structure for rule_condition
-- ----------------------------
DROP TABLE IF EXISTS `rule_condition`;
CREATE TABLE `rule_condition` (
  `id` varchar(128) NOT NULL COMMENT '主键id',
  `rule_id` varchar(128) NOT NULL COMMENT '规则id',
  `param_type` varchar(64) NOT NULL COMMENT '参数类型（post  query  uri等）',
  `operator` varchar(64) NOT NULL COMMENT '匹配符（=  > <  like match）',
  `param_name` varchar(64) NOT NULL COMMENT '参数名称',
  `param_value` varchar(64) NOT NULL COMMENT '参数值',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='规则条件表';

-- ----------------------------
-- Table structure for rule_condition_backup
-- ----------------------------
DROP TABLE IF EXISTS `rule_condition_backup`;
CREATE TABLE `rule_condition_backup` (
  `id` varchar(128) NOT NULL COMMENT '主键id',
  `rule_id` varchar(128) NOT NULL COMMENT '规则id',
  `param_type` varchar(64) NOT NULL COMMENT '参数类型（post  query  uri等）',
  `operator` varchar(64) NOT NULL COMMENT '匹配符（=  > <  like match）',
  `param_name` varchar(64) NOT NULL COMMENT '参数名称',
  `param_value` varchar(64) NOT NULL COMMENT '参数值',
  `date_published` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`,`date_published`),
  KEY `idx_ruleid_date_publised` (`rule_id`,`date_published`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='规则条件备份历史表';

-- ----------------------------
-- Table structure for selector
-- ----------------------------
DROP TABLE IF EXISTS `selector`;
CREATE TABLE `selector` (
  `id` varchar(128) NOT NULL COMMENT '主键id varchar',
  `plugin_id` varchar(128) NOT NULL COMMENT '插件id',
  `name` varchar(64) NOT NULL COMMENT '选择器名称',
  `match_mode` int(2) NOT NULL COMMENT '匹配方式（0 and  1 or)',
  `type` int(4) NOT NULL COMMENT '类型（0，全流量，1自定义流量）',
  `sort` int(4) NOT NULL COMMENT '排序',
  `handle` varchar(1024) DEFAULT NULL COMMENT '处理逻辑（此处针对不同的插件，会有不同的字段来标识不同的处理，所有存储json格式数据）',
  `enabled` tinyint(4) NOT NULL COMMENT '是否开启',
  `loged` tinyint(4) NOT NULL COMMENT '是否打印日志',
  `continued` tinyint(4) NOT NULL COMMENT '是否继续执行',
  `date_published` timestamp NULL DEFAULT NULL COMMENT '发布时间',
  `date_rollbacked` timestamp NULL DEFAULT NULL COMMENT '回滚时间',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='选择器表';

-- ----------------------------
-- Table structure for selector_backup
-- ----------------------------
DROP TABLE IF EXISTS `selector_backup`;
CREATE TABLE `selector_backup` (
  `id` varchar(128) NOT NULL COMMENT '主键id varchar',
  `plugin_id` varchar(128) NOT NULL COMMENT '插件id',
  `name` varchar(64) NOT NULL COMMENT '选择器名称',
  `match_mode` int(2) NOT NULL COMMENT '匹配方式（0 and  1 or)',
  `type` int(4) NOT NULL COMMENT '类型（0，全流量，1自定义流量）',
  `sort` int(4) NOT NULL COMMENT '排序',
  `handle` varchar(1024) DEFAULT NULL COMMENT '处理逻辑（此处针对不同的插件，会有不同的字段来标识不同的处理，所有存储json格式数据）',
  `enabled` tinyint(4) NOT NULL COMMENT '是否开启',
  `loged` tinyint(4) NOT NULL COMMENT '是否打印日志',
  `continued` tinyint(4) NOT NULL COMMENT '是否继续执行',
  `remark` text,
  `date_published` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `date_rollbacked` timestamp NULL DEFAULT NULL COMMENT '回滚时间',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`,`date_published`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='选择器备份历史表';

-- ----------------------------
-- Table structure for selector_condition
-- ----------------------------
DROP TABLE IF EXISTS `selector_condition`;
CREATE TABLE `selector_condition` (
  `id` varchar(128) NOT NULL COMMENT '主键id',
  `selector_id` varchar(128) NOT NULL COMMENT '选择器id',
  `param_type` varchar(64) NOT NULL COMMENT '参数类型（post  query  uri等）',
  `operator` varchar(64) NOT NULL COMMENT '匹配符（=  > <  like match）',
  `param_name` varchar(64) NOT NULL COMMENT '参数名称',
  `param_value` varchar(64) NOT NULL COMMENT '参数值',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_selector_id` (`selector_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='选择器条件表';

-- ----------------------------
-- Table structure for selector_condition_backup
-- ----------------------------
DROP TABLE IF EXISTS `selector_condition_backup`;
CREATE TABLE `selector_condition_backup` (
  `id` varchar(128) NOT NULL COMMENT '主键id',
  `selector_id` varchar(128) NOT NULL COMMENT '选择器id',
  `param_type` varchar(64) NOT NULL COMMENT '参数类型（post  query  uri等）',
  `operator` varchar(64) NOT NULL COMMENT '匹配符（=  > <  like match）',
  `param_name` varchar(64) NOT NULL COMMENT '参数名称',
  `param_value` varchar(64) NOT NULL COMMENT '参数值',
  `date_published` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`,`date_published`),
  KEY `idx_selector_id_date_published` (`selector_id`,`date_published`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='选择器条件历史备份表';

/*plugin*/
INSERT IGNORE INTO `diablo_pre`.`plugin` (`id`, `name`,`role`, `enabled`, `date_created`, `date_updated`) VALUES ('1', 'sign','0', '0', '2018-06-14 10:17:35', '2018-06-14 10:17:35');
INSERT IGNORE INTO `diablo_pre`.`plugin` (`id`, `name`,`role`,`enabled`, `date_created`, `date_updated`) VALUES ('2', 'waf', '0','1', '2018-06-23 10:26:30', '2018-06-13 15:43:10');
INSERT IGNORE INTO `diablo_pre`.`plugin` (`id`, `name`,`role`,`enabled`, `date_created`, `date_updated`) VALUES ('3', 'rate_limiter','0', '1', '2018-06-23 10:26:37', '2018-06-13 15:34:48');
INSERT IGNORE INTO `diablo_pre`.`plugin` (`id`, `name`,`role`, `enabled`, `date_created`, `date_updated`) VALUES ('4', 'divide', '0','1', '2018-06-25 10:19:10', '2018-06-13 13:56:04');
INSERT IGNORE INTO `diablo_pre`.`plugin` (`id`, `name`,`role`,`enabled`, `date_created`, `date_updated`) VALUES ('5', 'dubbo','0', '1', '2018-06-23 10:26:41', '2018-06-11 10:11:47');

/**user**/
INSERT IGNORE INTO `diablo_pre`.`dashboard_user` (`id`, `user_name`, `password`, `role`, `enabled`, `date_created`, `date_updated`) VALUES ('1', 'admin', '123456', '1', '1', '2018-06-23 15:12:22', '2018-06-23 15:12:23');

