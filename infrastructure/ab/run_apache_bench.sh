docker build -t benchmark-ab:1.0 .

docker run --rm benchmark-ab:1.0 -n 2000 http://172.17.0.1:4005/employees.json

docker image rm benchmark-ab:1.0

$SHELL
