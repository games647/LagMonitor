# Changelog

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