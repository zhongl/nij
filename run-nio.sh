java -Xmx2g -Xms2g -cp target/classes:/home/jushi/.m2/repository/ch/qos/logback/logback-classic/0.9.28/logback-classic-0.9.28.jar:/home/jushi/.m2/repository/ch/qos/logback/logback-core/0.9.28/logback-core-0.9.28.jar:/home/jushi/.m2/repository/junit/junit/4.8.1/junit-4.8.1.jar:/home/jushi/.m2/repository/org/apache/mina/mina-core/2.0.2/mina-core-2.0.2.jar:/home/jushi/.m2/repository/org/glassfish/external/management-api/3.0.0-b012/management-api-3.0.0-b012.jar:/home/jushi/.m2/repository/org/glassfish/gmbal/gmbal-api-only/3.0.0-b023/gmbal-api-only-3.0.0-b023.jar:/home/jushi/.m2/repository/org/glassfish/grizzly/grizzly-framework/2.0/grizzly-framework-2.0.jar:/home/jushi/.m2/repository/org/hamcrest/hamcrest-all/1.1/hamcrest-all-1.1.jar:/home/jushi/.m2/repository/org/jboss/netty/netty/3.2.3.Final/netty-3.2.3.Final.jar:/home/jushi/.m2/repository/org/scala-lang/scala-library/2.8.1/scala-library-2.8.1.jar:/home/jushi/.m2/repository/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar:/home/jushi/.m2/repository/org/xsocket/xSocket/2.8.15/xSocket-2.8.15.jar -Dthread.pool.size=$6 com.github.zhongl.nij.nio.Server $1 $2 $3 $4 $5
