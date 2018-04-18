# LagMonitor table
CREATE TABLE IF NOT EXISTS `{prefix}tps` (
  tps_id  INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  tps     FLOAT UNSIGNED NOT NULL,
  updated TIMESTAMP      NOT NULL      DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `{prefix}monitor` (
  monitor_id      INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  process_usage   FLOAT UNSIGNED     NOT NULL,
  os_usage        FLOAT UNSIGNED     NOT NULL,
  free_ram        MEDIUMINT UNSIGNED NOT NULL,
  free_ram_pct    FLOAT UNSIGNED     NOT NULL,
  os_free_ram     MEDIUMINT UNSIGNED NOT NULL,
  os_free_ram_pct FLOAT UNSIGNED     NOT NULL,
  load_avg        FLOAT UNSIGNED     NOT NULL,
  updated         TIMESTAMP          NOT NULL  DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `{prefix}worlds` (
  world_id      INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  monitor_id    INTEGER UNSIGNED  NOT NULL,
  world_name    VARCHAR(255)      NOT NULL,
  chunks_loaded SMALLINT UNSIGNED NOT NULL,
  tile_entities SMALLINT UNSIGNED NOT NULL,
  world_size    MEDIUMINT UNSIGNED NOT NULL,
  entities      INT UNSIGNED      NOT NULL,
  FOREIGN KEY (monitor_id) REFERENCES `{prefix}monitor` (monitor_id)
);

CREATE TABLE IF NOT EXISTS `{prefix}players` (
  world_id INTEGER UNSIGNED,
  uuid     CHAR(40)          NOT NULL,
  NAME     VARCHAR(16)       NOT NULL,
  ping     SMALLINT UNSIGNED NOT NULL,
  PRIMARY KEY (world_id, uuid),
  FOREIGN KEY (world_id) REFERENCES `{prefix}worlds` (world_id)
);

CREATE TABLE IF NOT EXISTS `{prefix}native` (
  native_id      INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  mc_read        SMALLINT UNSIGNED,
  mc_write       SMALLINT UNSIGNED,
  free_space     INT UNSIGNED,
  free_space_pct FLOAT UNSIGNED,
  disk_read      SMALLINT UNSIGNED,
  disk_write     SMALLINT UNSIGNED,
  net_read       SMALLINT UNSIGNED,
  net_write      SMALLINT UNSIGNED,
  updated        TIMESTAMP NOT NULL           DEFAULT CURRENT_TIMESTAMP
);
