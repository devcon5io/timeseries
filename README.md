# timeseries
Tools for recording and retrieving timeseries data.

The central component of this toolsuite is the timeseries collector. It provides a highly scalable routing and 
data transformation solution based on Vertx. It accepts measurement data, transforms it and forwards it to the 
configured timeseries stores.

The collector can be extended by deploying additional transformation logic written in a broad set of languages due
to the polyglott nature of Vertx.

# Building and Running

To build the collector using Maven, execute
  
    mvn clean package
    
This will create a fat-jar with all the dependencies. You may run the fat-jar with a configuration

    java -jar target\ts-collector-1.0-SNAPSHOT-fat.jar -conf src\main\resources\collector.json
