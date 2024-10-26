#!/bin/bash

cd WorkingTools/TestMulticast || exit
javac *.java
echo "Testing the Multicast program."
# Start the MulticastReceiver in the background
java MulticastReceiver 224.0.0.1 8888 &
RECEIVER_PID=$!
# Give the receiver some time to start
sleep 2
# Start the MulticastSender
java MulticastSender 224.0.0.1 8888 2
kill $RECEIVER_PID