# jmx-script

Small console java library to query server with jmx.

## Usage
```
usage: jmxScript [-a <arg>] [-c <arg>] [-d <arg>] [-f] [-h] [-l <arg>] [-lc] [-o <arg>] [-p <arg>] [-r <arg>] [-s <arg>]

This program allows to fetch jmx data from a weblogic server.
 -a,--attribute <arg>     the fetched attribute name
 -c,--command <arg>       send a command to to weblogic. Like an evict cache or whatever.
 -d,--credentials <arg>   the credentials file that contains the 'login', 'password', 'server' and 'port' for connection
 -f,--force               force a cpu consuming jmx command to execute.
 -h,--help                Print this help message
 -l,--login <arg>         the server access login. Defaults to weblogic
 -lc,--listcommands       List available commands , and exits. (Needs a connection)
 -o,--port <arg>          the server port to connect to. Defaults to 9080
 -p,--password <arg>      the server access password
 -r,--runtime <arg>       the runtime to query
 -s,--server <arg>        the server address

The couple attribute/runtime is exclusive with command/listcommands.
It means that if a command/listcommands is used, it will not execute the runtime even if defined.

The credential file should respect the java properties file format, i.e.

credential.properties :

login=mylogin
password=my_super_S3c|_|R3_p4ssW0rd
server=acetp-prd1-m1.fr.net.intra
port=9080

Here is the list of possible error exit codes :
  0 ) ok, everything went good.
 -1 ) unparseable command line.
 -2 ) no runtime.
 -3 ) credential file problem.
 -4 ) missing password in command line.
 -5 ) missing server in command line.
 -6 ) missing attribute in command line.
 -7 ) unparseable port in credentials.
 -8 ) unparseable port in command line.
 -9 ) malformed jmx url.
-10 ) missing login in credentials.
-11 ) missing password in credentials.
-12 ) missing server in credentials.
-13 ) missing port in credentials.
-14 ) [removed]
-15 ) jmx connection to the server failed
-16 ) malformed object
-17 ) attribute not found
-18 ) mbean getter exception
-19 ) mbean setter exception
-20 ) instance not found
-21 ) unable to find command


Here are some example calls assuming credentials.properties connects to uir-m1 :
jmxScript -h

jmxScript -d credentials.properties -a HeapFreeCurrent -r
com.bea:ServerRuntime=acetp-uir-m1,Name=acetp-uir-m1,Type=JVMRuntime

jmxScript -d credentials.properties -lc

jmxScript -d credentials.properties -c "Generate Thread Dump and Filter" "waitForNext"
```
