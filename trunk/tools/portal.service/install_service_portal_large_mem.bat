rem http://java.sun.com/performance/reference/whitepapers/tuning.html#section4.1.3
set JAVA_HOME=e:\jdk15
set JBOSS_HOME=e:\portal422
set JAVA_OPTS=-Xmx2506m -Xms2506m -Xmn1536m -Xss128k -XX:LargePageSizeInBytes=256m -XX:+AggressiveOpts -XX:+UseParallelGC -XX:+UseBiasedLocking -Duser.timezone=GMT+8 -Dorg.mortbay.http.HttpRequest.maxFormContentSize=10240000 -Dnds.config.path=e:/act/conf/portal.properties

rem set JAVA_OPTS=%JAVA_OPTS% -Dcom.sun.management.jmxremote.port=7777
rem set JAVA_OPTS=%JAVA_OPTS% -Dcom.sun.management.jmxremote.authenticate=false
rem set JAVA_OPTS=%JAVA_OPTS% -Dcom.sun.management.jmxremote.ssl=false

set JBOSS_ENDORSED_DIRS=%JBOSS_HOME%\lib\endorsed
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%JBOSS_HOME%\bin\run.jar
set JBOSS_CLASSPATH=%CLASSPATH%
set javadll=%JAVA_HOME%\jre\bin\server\jvm.dll
set javatool=%JAVA_HOME%\lib\tools.jar
set javarun=%JBOSS_HOME%\bin\run.jar
set outlog=%JBOSS_HOME%\bin\out.log
set errlog=%JBOSS_HOME%\bin\err.log

copy /y JavaService.exe %JBOSS_HOME%\bin\JBossService.exe

%JBOSS_HOME%\bin\JBossService.exe -install Portal4 "%javadll%" -Djboss.java.opts="%JAVA_OPTS%" -Djava.class.path=%CLASSPATH% -Djava.endorsed.dirs="%JBOSS_ENDORSED_DIRS%" -server %JAVA_OPTS% -start org.jboss.Main -stop org.jboss.Main -method systemExit -out "%outlog%" -err "%errlog%" -current "%JBOSS_HOME%\bin" -depends OracleServiceORCL -auto

@pause
