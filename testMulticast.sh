#!/bin/bash

# Define a cleanup function to kill the background processes
cleanup() {
  echo "Stopping the Multicast services..."
  kill $RECEIVER_PID $SENDER_PID
  exit 0
}

# Trap SIGINT (CTRL+C) and call the cleanup function
trap cleanup SIGINT

cd WorkingTools/TestMulticast || exit
javac *.java

echo "Testing the Multicast program."

# Start the MulticastReceiver in the background
java MulticastReceiver 224.0.0.1 8888 &
RECEIVER_PID=$!

# Give the receiver some time to start
sleep 2

# Start the MulticastSender in the background
java MulticastSender 224.0.0.1 8888 1 &
SENDER_PID=$!

# Wait indefinitely to keep the script running until interrupted
wait