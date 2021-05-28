# scaling-invention

## Run & Test

```sh
gh repo clone tellnobody1/scaling-invention
cd scaling-invention
git submodule update --init --remote --recursive
sbt run
curl -v -X POST "http://localhost:8080/fetch"
curl -v "http://localhost:8080/products?configId=1&size=5&page=1"
```
