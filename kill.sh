kill -9 `ps -aux | grep system.FilterDaemon | grep -v grep | awk {'print $2'}`
