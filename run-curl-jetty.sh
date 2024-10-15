#!/bin/sh 

BASE_URL=http://localhost:8080

# proxy servlet
curl "${BASE_URL}/proxy-servlet-internal-1/index.html"
curl "${BASE_URL}/proxy-servlet-internal-1/a.html"
curl "${BASE_URL}/proxy-servlet-internal-1/b.html"
curl "${BASE_URL}/proxy-servlet-internal-1/c.html"
curl "${BASE_URL}/proxy-servlet-internal-1/d.html"


# multi target proxy servlet
curl "${BASE_URL}/multi-proxy-servlet-1/index.html"
curl "${BASE_URL}/multi-proxy-servlet-2/index.html"
curl "${BASE_URL}/multi-proxy-servlet-3/index.html"
 

