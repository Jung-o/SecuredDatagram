#!/bin/bash

# Define a cleanup function to kill the background processes
cleanup() {
  echo "Stopping the services..."
  kill $SERVER_PID
  exit 0
}

# Trap SIGINT (CTRL+C) and call the cleanup function
trap cleanup SIGINT

cd WorkingTools/TFTP || exit
javac TFTPClient/src/*.java
javac TFTPServer/src/*.java

echo "Testing the TFTP File sending service."
echo "Will download file server1.jpg from server."
echo "Then will upload file client1.jpg to server."

# Start the TFTP Server in the background
cd TFTPServer/src
java TFTPServer &
SERVER_PID=$!

# Give the receiver some time to start
sleep 2

# Start the Streaming in the background
cd ../../TFTPClient/src
java TFTPClient localhost R server1.jpg &

java TFTPClient localhost W client1.jpg &

# Wait indefinitely to keep the script running until interrupted
wait