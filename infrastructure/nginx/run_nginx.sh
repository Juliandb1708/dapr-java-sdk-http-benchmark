docker build -t benchmark-nginx:1.0 .

docker run -d -p 4005:80 --name benchmark-nginx benchmark-nginx:1.0