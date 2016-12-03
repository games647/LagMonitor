# Changelog

##1.11.4

* Fix users don't receive a map on graph command
* Display error message for untracked ping players
* Fail silently if the jfc file already exists

##1.11.3

* Fix detecting socket connections (socket-block-detection) if the default proxy is null

##1.11.2

* Optimize plugin violations handling
* Fix security manager spams if enabled
* Fix log caused methods only once even if it's disabled

##1.11.1

* Add missing uri to the connection selector
* Fix plugin name detection and thread-safety (Fixes #17)

##1.11

* Added sigar as fallback when Oracle API isn't available (com.sun.management.OperatingSystemMXBean)

##1.10.1

* Fix thread safety check

##1.10

* Add hideStacktrace config property, which shows only two lines
* Add oncePerPlugin config property which report it only one time per startup and plugin
* Add a way to find the plugin source. [Experimental]

##1.9.1

* Allow blocking actions on server startup (Fixes #15)
* Clarify blocking action message
* Upgrade to Java 8 (requires now Java 8)

##1.9

* Add monitor pastes to http://paste.enginehub.org/ - Please support for this awesome service and please do not spam it
* Fix showing duplicate http blocking messages, because a http connection is also a socket connection
* Fix showing stacktrace on blocking action

##1.8

* Add /lagpage < save >  and /lagpage < all >

##1.7.2

* Fix traffic reader storage save
* Warn users who still use the outdated Java 7 to upgrade to a newer version

##1.7

* Fail safely on an error for traffic reader
* Add configurable table prefix
* Add debug code if the storage insert failed

##1.6

* Added whitelist for certain commands for specific users

##1.5

* Added a faster and less error-prone blocking http detection

##1.4

* Added monitoring to a MySQL database
* Added a unsupported java vendor hint to heap and thread dumps
* Speed up the native command by loading the native driver only on plugin load

##1.3.2

* Fix command permission for /ping player

##1.3.1

* Fix class not found in paper spigot timings parser if user is using normal spigot

##1.3

* Added PaperTimings head data
* Added percent values to the paper spigot timings
* Fixed combined plugin name
* Fixed unknown entries in paper spigot timings parser
* Fixed missing total second head data in spigot timings parser
* Fixed pagination error from the last page

##1.2

* Added support for Java Flight Recorder dump
* Added default configuration file for flight recorder
* Fixed permission of lagpage command has the paper command permission

##1.1

* Added thread dump to file option /thread dump
* Added heap dump to file option /heap dump
* Fix pagination error if the user is requesting a too high page number

## 1.0

* Added plugin injection (commands, listener and tasks)
* Added pagination
* Added /heap command for heap dumps
* Added world size to the system command
* Added tile entities count to the system command
* Added security manager for more efficient blocking checks
* Added combined graphs example: /graph cpu heap threads
* Added check if timings is enabled for PaperSpigot servers
* Improved performance of commands by caching them with the pagination
* Optimize Spigot timings parser

## 0.7

* Added /lag alias for the /tpshistory command
* Added swap to the environment command
* Added tasks command
* Added /vm command for class loading, garbage collectors, vm specifications
* Added basic PaperSpigot timings parser
* Added load average to the environment command
* Moved Java version to the vm command
* Optimized thread locking in monitor/profiler for better performance

## 0.6

* Added /native command to query native data like OS uptime, Network adapter speed, CPU MHZ, ...
* Added startup parameters to the system command
* Added thread-safety check
* Added blocking, waiting, sleeping check
* Added Thread id to the threads command
* Improved readability for tpshistory command in console
* Fixed very low tps value displayed as full tps
* Fixed scrolling tpsHistory
* Fixed NPE on plugin load at runtime
* Fixes ClassNotFoundException on reload if traffic reader is activated
* Fixed rounding issues for the average ping
* Fixed cleanup of monitor task on plugin disable

## 0.5

* Added Ping History -> displays average ping now
* Added traffic counter
* Added config
* Reduce memory usage by getting the stacktrace of only one thread
* Fixed thread safety

## 0.4

* Added world info to the system command
* Added lazy loading for thread monitor to reduce memory usage
* Added worlds, players and plugins count to the system command
* Added samples count for thread monitor
* Improved tons of command styling
* Fixed thread safety
* Fixed free memory value
* Fixed memory leak for thread monitor
* Fixed ping method only displaying the own ping

## 0.3

* Fixed: max memory output in the /system command
* Added color highlighting for performance intensive tasks in the timings report
* Fixed timings output
* Added warning if timings are deactivated
* Added classes graph
* Added command completion for all commands
* Updated to Minecraft 1.9
* Added missing permission node for /ping [player] to the plugin.yml

## 0.2

* Added environment command
* Added server version to the system command
* Added more graphs (Threads, CPU usage)
* Fixed CPU usage value
* Improved Command output styling
* Reduced delay start of ticks per second task

## 0.1.1

* Added command permissions
* Added online check for the ping command