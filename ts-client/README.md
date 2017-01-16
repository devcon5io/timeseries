# TimeSeries Client

This is a lightweight library for sending timeseries data to a remote
timeseries collector via HTTP. The library provides a datastructure
for defining a datapoint and a client that sends the datapoints to 
remote collector via HTTP.

# Usage

## Client
Create a time series client for a remote collector. You have to define the
hostname and the port of the timeseries collector as well as an endpoint.
The endpoint to use depends on datapoint verticles defined on the collector.
Some compute the datapoints directly and store them in a timeseries DB,
while other Verticles may perform a transformation on the data and delegate
them to other Verticles for further processing.  

    TimeSeriesClient client = TimeSeriesClient.forTarget("localhost", 9090, "/store/test");

You may configure the client's underlying thread pool. By default it uses 1
worker thread. To send the timeseries data in blocking mode, using the same
thread, initialize the client with a same-thread executor:

    TimeSeriesClient client = TimeSeriesClient.forTarget("localhost", 9090, "/store/test", Runnable::run);

To send a datapoint, simply call the `store` method of the client.
Which is a fire-and-forget method. 

    client.store(dp);

## Datapoints

A datapoint is created by a constructor. By default, each datapoint is
associated with the timeseries "measure" and has the system's current
timestamp, unless other specified. Each datapoint can have 0..n name-value
pairs as tags must have 1..n name-value pairs as actual values.

    Datapoint dp = new Datapoint("test").addValue("test", 123)
    
As the datapoint is multivalued and multi-taggable, you may add additional
measures and context information. For example, storing the temperature and humidity
information for a particular window in a particular building, you may use

    Datapoint dp = new Datapoint("sensors").addTag("building", "1")
                                           .addTag("windows","N-E-01")
                                           .addValue("temp", 25.6)
                                           .addValue("humidity", 0.56);

    

