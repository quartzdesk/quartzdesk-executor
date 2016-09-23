# Installation Instructions For Apache Tomcat

QuartzDesk Executor (QE) requires a database. The database contains Quartz JDBC job store tables as well as other objects required by QE itself. QE supports the following popular database management systems (DBMS):
 
 | DBMS                       | Minimum Version          |
 |:---------------------------|:-------------------------|
 | DB2                        | 10.1                     |
 | H2                         | 1.3.174                  |
 | Microsoft SQL Server       | 2008 R2 SP1              |
 | MySQL                      | 5.6.4                    |
 | Oracle                     | 10.2 (10g R2)            |
 | PostgreSQL                 | 9.1                      |


## 1. Database
Create a new and empty database in your DBMS. The name of the database should be `quartzdesk_executor`.

 
## 2. JDBC Driver 
Copy the JDBC driver of the used database to the Tomcat's shared lib directory (`TOMCAT_HOME/lib`).


## 3. Data Source
Open `TOMCAT_HOME/conf/server.xml` and add a new data source definition in the `GlobalNamingResources` element.

#### DB2
```xml
<Resource name="jdbc/QuartzDeskExecutorDS"
       auth="Container"
       type="javax.sql.DataSource"
       removeAbandoned="true"
       removeAbandonedTimeout="30"
       maxActive="10"
       maxIdle="1"
       maxWait="2000"
       validationQuery="select 1 from sysibm.sysdummy1"
       poolPreparedStatements="true"
       username="DB_USER"
       password="DB_PASSWORD"
       driverClassName="com.ibm.db2.jcc.DB2Driver"
       url="jdbc:db2://DB_HOST:DB_PORT/DB_NAME"/>
```

#### H2
```xml
<Resource name="jdbc/QuartzDeskExecutorDS"
      auth="Container"
      type="javax.sql.DataSource"
      removeAbandoned="true"
      removeAbandonedTimeout="30"
      maxActive="10"
      maxIdle="1"
      maxWait="2000"
      validationQuery="select 1"
      poolPreparedStatements="true"
      username="DB_USER"      
      password="DB_PASSWORD"
      driverClassName="org.h2.Driver"
      url="jdbc:h2:file:H2_DB_FILE_PATH;FILE_LOCK=NO"/>
```

#### Microsoft SQL Server
```xml
<Resource name="jdbc/QuartzDeskExecutorDS"
      auth="Container"
      type="javax.sql.DataSource"
      removeAbandoned="true"
      removeAbandonedTimeout="30"
      maxActive="10"
      maxIdle="1"
      maxWait="2000"
      validationQuery="select 1"
      poolPreparedStatements="true"
      username="DB_USER"
      password="DB_PASSWORD"
      driverClassName="com.microsoft.sqlserver.jdbc.SQLServerDriver"
      url="jdbc:sqlserver://DB_HOST:DB_PORT;databaseName=quartzdesk_executor"/>
```

#### MySQL
```xml
<Resource name="jdbc/QuartzDeskExecutorDS"
      auth="Container"
      type="javax.sql.DataSource"
      removeAbandoned="true"
      removeAbandonedTimeout="30"
      maxActive="10"
      maxIdle="1"
      maxWait="2000"
      validationQuery="select 1"
      poolPreparedStatements="true"
      username="DB_USER"
      password="DB_PASSWORD"
      driverClassName="com.mysql.jdbc.Driver"
      url="jdbc:mysql://DB_HOST:DB_PORT/quartzdesk_executor?cachePrepStmts=true"/>
```

#### Oracle
```xml
<Resource name="jdbc/QuartzDeskExecutorDS"
      auth="Container"
      type="javax.sql.DataSource"
      removeAbandoned="true"
      removeAbandonedTimeout="30"
      maxActive="10"
      maxIdle="1"
      maxWait="2000"
      validationQuery="select 1 from dual"
      poolPreparedStatements="true"
      username="DB_USER"
      password="DB_PASSWORD"
      driverClassName="oracle.jdbc.OracleDriver"
      url="jdbc:oracle:thin:@DB_HOST:DB_PORT:DB_NAME"/>
```

#### PostgreSQL
```xml
<Resource name="jdbc/QuartzDeskExecutorDS"
      auth="Container"
      type="javax.sql.DataSource"
      removeAbandoned="true"
      removeAbandonedTimeout="30"
      maxActive="50"
      maxIdle="5"
      maxWait="2000"
      validationQuery="select 1"
      poolPreparedStatements="true"
      username="DB_USER"
      password="DB_PASSWORD"
      driverClassName="org.postgresql.Driver"
      url="jdbc:postgresql://DB_HOST:DB_PORT/quartzdesk_executor"/>
```

Please ensure the configured DB user (`DB_USER`) is the owner of the `quartzdesk_executor` database or has sufficient rights to create and modify all database objects in the database.


## 4. Work Directory

Create an empty work directory where QE will looks for its configuration files and where it will store its log files.
          
Copy the contents of the `work` directory located inside the QE project (https://github.com/quartzdesk/quartzdesk-executor/tree/master/work) to the created work directory.
 
Edit the copied quartzdesk-executor.properties file in the work directory and adjust the value of the `db.profile` configuration property to match the type of your DB.

Add the following JVM system property at the top of `TOMCAT_HOME/bin/catalina.bat` (Windows) or `TOMCAT_HOME/bin/catalina.sh` (Unix, Linux, Mac). Alternatively, you can add the property to `TOMCAT_HOME/bin/setenv.bat` (Windows) or `TOMCAT_HOME/bin/setenv.sh` (Unix, Linux, Mac). 

#### Windows
```
set CATALINA_OPTS=%CATALINA_OPTS% -Dquartzdesk-executor.work.dir=<WORK_DIR>
```

#### Unix, Linux, Mac
```
CATALINA_OPTS="${CATALINA_OPTS} -Dquartzdesk-executor.work.dir=<WORK_DIR>"
```



## 5. Enable Remote JMX Access

In order to connect and manage the Quartz scheduler embedded in the QE application from the QuartzDesk GUI, you need to enable remote JMX access to the JVM the QE application will be running on. The steps depend on the QuartzDesk edition that you intend to use.

### QuartzDesk Lite Edition

Add the following JVM system properties to `TOMCAT_HOME/bin/catalina.bat` (Windows) or `TOMCAT_HOME/bin/catalina.sh`. Alternatively, you can add the properties to `TOMCAT_HOME/bin/setenv.bat` (Windows) or `TOMCAT_HOME/bin/setenv.sh` (Unix, Linux, Mac).

#### Windows
```
set CATALINA_OPTS=%CATALINA_OPTS% -Djava.rmi.server.hostname=JMX_HOST
set CATALINA_OPTS=%CATALINA_OPTS% -Djavax.management.builder.initial
set CATALINA_OPTS=%CATALINA_OPTS% -Dcom.sun.management.jmxremote=true
set CATALINA_OPTS=%CATALINA_OPTS% -Dcom.sun.management.jmxremote.port=JMX_PORT
set CATALINA_OPTS=%CATALINA_OPTS% -Dcom.sun.management.jmxremote.ssl=false
set CATALINA_OPTS=%CATALINA_OPTS% -Dcom.sun.management.jmxremote.authenticate=false
```

#### Unix, Linux, Mac
```
CATALINA_OPTS="${CATALINA_OPTS} -Djava.rmi.server.hostname=JMX_HOST" 
CATALINA_OPTS="${CATALINA_OPTS} -Djavax.management.builder.initial" 
CATALINA_OPTS="${CATALINA_OPTS} -Dcom.sun.management.jmxremote=true" 
CATALINA_OPTS="${CATALINA_OPTS} -Dcom.sun.management.jmxremote.port=JMX_PORT" 
CATALINA_OPTS="${CATALINA_OPTS} -Dcom.sun.management.jmxremote.ssl=false"
CATALINA_OPTS="${CATALINA_OPTS} -Dcom.sun.management.jmxremote.authenticate=false"
```

### QuartzDesk Standard / Enterprise Edition
 
If you are going to install the QuartzDesk JVM Agent (see the next installation step), you can make use of one of the available JMX connectors provided by the agent. These connectors can be enabled in the QuartzDesk JVM Agent configuration properties file. 

Otherwise add the JVM system properties used for the QuartzDesk Lite Edition.  



## 6. Install QuartzDesk JVM Agent (optional, but strongly recommended)

In order to use all of the advanced QuartzDesk platform features, such as access to the persistent job execution history access, job execution notifications, job chaining, execution statistics, etc., you need to install the QuartzDesk JVM Agent on the JVM running the QE application. Please refer to the [QuartzDesk JVM Agent Installation and Upgrade Guide](https://www.quartzdesk.com/documentation/installation-and-upgrade-guides) for details


## 7. Deploy QE WAR 

Deploy the QE application by copying the `quartzdesk-executor-web-<version>.war` file to the `TOMCAT_HOME/webapps` directory.

Start Tomcat and check its logs (`TOMCAT_HOME/logs`) if the QE application was started successfully. You may also want to check the QE log files created in the configured work directory. 
