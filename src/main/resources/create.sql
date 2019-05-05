CREATE TABLE IF NOT EXISTS `{prefix}monitor` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `process_usage` float unsigned NOT NULL,
  `os_usage` float unsigned NOT NULL,
  `free_ram` mediumint(8) unsigned NOT NULL,
  `free_ram_pct` float unsigned NOT NULL,
  `os_free_ram` mediumint(8) unsigned NOT NULL,
  `os_free_ram_pct` float unsigned NOT NULL,
  `load_avg` float unsigned NOT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `{prefix}native` (
  `native_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `mc_read` smallint(5) unsigned DEFAULT NULL,
  `mc_write` smallint(5) unsigned DEFAULT NULL,
  `free_space` int(10) unsigned DEFAULT NULL,
  `free_space_pct` float unsigned DEFAULT NULL,
  `disk_read` smallint(5) unsigned DEFAULT NULL,
  `disk_write` smallint(5) unsigned DEFAULT NULL,
  `net_read` smallint(5) unsigned DEFAULT NULL,
  `net_write` smallint(5) unsigned DEFAULT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`native_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `{prefix}tps` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `tps` float unsigned NOT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `{prefix}player` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `name` varchar(16) NOT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `{prefix}world` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `{prefix}player_data` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `player` int(10) unsigned NOT NULL,
  `world` int(10) unsigned NOT NULL,
  `ping` int(10) unsigned NOT NULL DEFAULT '0',
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`player`) REFERENCES `{prefix}player` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`world`) REFERENCES `{prefix}world` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `{prefix}world_data` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `world` int(10) unsigned NOT NULL,
  `chunks_loaded` smallint(5) unsigned NOT NULL,
  `tile_entities` smallint(5) unsigned NOT NULL,
  `world_size` mediumint(8) unsigned NOT NULL,
  `entities` int(10) unsigned NOT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`world`) REFERENCES `{prefix}world` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
