docker build -t workbench-nginx:1.0 .

docker run -t -p 4005:80 --name workbench-nginx workbench-nginx:1.0
