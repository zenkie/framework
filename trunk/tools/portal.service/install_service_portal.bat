rem e:\portal422\bin\JBossService.exe -install Portal4 e:\jdk15\jre\bin\server\jvm.dll -Xmx1024M -Dnds.config.path=e:/act/conf/portal.properties -Djava.class.path=e:\jdk15\lib\tools.jar;e:\portal422\bin\run.jar -Duser.timezone=GMT+8 -Dorg.mortbay.http.HttpRequest.maxFormContentSize=10240000 -start org.jboss.Main -stop org.jboss.Main -method systemExit -out e:\portal422\bin\out.log -err e:\portal422\bin\err.log -current e:\portal422\bin -depends OracleServiceORCL -auto

set JAVA_HOME=e:\jdk15
set JBOSS_HOME=e:\portal422
set JAVA_OPTS=-Xms512m -Xmx1024M -Xss512k -XX:PermSize=256m -XX:MaxPermSize=256m -XX:+PrintGCDetails -Duser.timezone=GMT+8 -Dorg.mortbay.http.HttpRequest.maxFormContentSize=10240000 -Dnds.config.path=e:/act/conf/portal.properties

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
