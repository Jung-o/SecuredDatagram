#!/bin/bash

# Define a cleanup function to kill the background processes
cleanup() {
  echo "Stopping the DSTP Test Servers..."
  kill $RECEIVER_PID
  exit 0
}

# Trap SIGINT (CTRL+C) and call the cleanup function
trap cleanup SIGINT

cd src || exit
javac *.java

echo "Testing the DSTP implementation with basic client and server"

# Start the Test DSTP Server in the background
java TestServer &
RECEIVER_PID=$!

# Give the receiver some time to start
sleep 2

# Start the MulticastSender in the background
java TestClient &
SENDER_PID=$!

# Wait indefinitely to keep the script running until interrupted
wait
