# scaling-invention

## Run & Test

```sh
gh repo clone tellnobody1/scaling-invention
cd scaling-invention
git submodule update --init --remote --recursive
sbt run
curl -v -X POST "http://localhost:8080/fetch"
curl -v "http://localhost:8080/products?configId=1&size=5&page=1"
curl -v "http://localhost:8080/products?configId=2&size=5&page=1"
```

## Design

### 1) Deployment

It can be deployed anywhere. The most straighford way is create package
with run script and upload to server. In case of cluster setup it can be
deployed with zero downtime.

### 2) Store 'configId'

The task is not clear if is it required to implement or not. In any case,
I do not see any value to store 'configId'. It can be send with each request.

If I would implement storing of parameter I add it to REST API in same service.
Because it is very small functionality it will not effect service.

### 3) Scaling and Blocking

There are no blocking parts in solution.

Scaling is pretty easy with akka-cluster. Storage will be scaled to each node in
master-master or master-replica configuration. Because there is only one point
of new data it is acceptable to use master-replica.

It is possible to run this solution in cluster with data replication using akka-cluster.
Or use separate cluster of databases.

## Implementation

### Indexing of data

To support arbitrary large input data indexing is implemented with
streaming of JSON objects which are saved to storage one-by-one.

### Storage

Elasticsearch is proposed to be used as storage for data. But none of
the search capabilites will be used. That's why I won't use it for
this problem.

Redis/RDMS is proposed to save configuration. Because it is test task
it is overkill to setup these system. Instead, I've designed key-value store API.
Any database can be used as implementation of these API.

### 'page' parameter

There is no easy way to start streaming exactly on required page.
Solution is to skip the corresponding number of elements from the start.

That's why it is more efficient to implement Load More instead of Pagination.
In other words, send the last ID of the elements in previous response.

### JSON response

JSON format is verbose and inefficient. But it is used for response because
it is test task. Furthermore, it is pretty printed for easy testing. In any
case JSON should be transferred without spaces and compressed.

Because 'size' parameter can be quite large I've implemented chunked response.
That's means that stream is lazily evaluated and sent one-by-one object.

In real world it is better to replace JSON with binary format
which will be encoded much faster, be smaller which means less of trafic and
faster response. Also, there is no need for HTTP because TCP/WebSocket will do
better.

### Sorting

Sorting order is undefined but I've used ascending order. There are no limitations
to use descending order or both.

### Codecs

For storage Protobuf is used. To save more data LZ4 compression should be added.
To work with Protobuf I've added small library which creates codecs based on
Scala class using macros.

For working with JSON it used Argonaut. It tradeoffs speed for readiness. It could be
replaced with Jackson, for example. But at the moment, Jackson doesn't support Scala 3
fully. In any way, because it is test task the more important was readiness. There is
no problem to replace implementation with any other library.

### Akka HTTP DSL

I find proposed DSL is awkard and not native to Scala. I prefer match-case style of
defining of REST API. That's why small DSL was implemented for request matching.
