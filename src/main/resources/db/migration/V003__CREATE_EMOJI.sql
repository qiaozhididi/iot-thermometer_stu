CREATE TABLE `emoji` (
	`id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
	`name` VARCHAR(256) NOT NULL COMMENT '名称' COLLATE 'utf8mb4_general_ci',
	`face` VARCHAR(100) NOT NULL COMMENT '表情' COLLATE 'utf8mb4_general_ci',
	PRIMARY KEY (`id`) USING BTREE
)
COMMENT='emoji表情'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
ROW_FORMAT=DYNAMIC
AUTO_INCREMENT=6
;