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

## Design Notes

### 'page' parameter

There is no easy way to start streaming exactly on required page.
Solution is to skip the corresponding number of elements from the start.
That's why it is more efficient to implement Load More instead of Pagination.
In other words, send the last ID of the previous response.

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
