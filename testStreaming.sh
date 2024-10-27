#!/bin/bash

# Define a cleanup function to kill the background processes
cleanup() {
  echo "Stopping the services..."
  kill $PROXY_PID $STREAM_PID
  exit 0
}

# Trap SIGINT (CTRL+C) and call the cleanup function
trap cleanup SIGINT

cd WorkingTools/StreamingService || exit
javac hjStreamServer/*.java
javac hjUDPproxy/*.java

echo "Testing the Streaming service."
echo "Will stream to port 1234 on localhost"

# Start the UDP Proxy in the background
cd hjUDPproxy
java hjUDPproxy 127.0.0.1:1337 127.0.0.1:1234 &
PROXY_PID=$!

# Give the receiver some time to start
sleep 2

# Start the Streaming in the background
cd ../hjStreamServer
java hjStreamServer movies/cars.dat 127.0.0.1 1337 &
STREAM_PID=$!

# Wait indefinitely to keep the script running until interrupted
wait