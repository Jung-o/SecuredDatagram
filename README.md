# Project Overview

## Test Scripts

This project includes bash scripts to test each application:

- **`testMulticast.sh`**: This script compiles the Java classes in `WorkingTools/TestMulticast` and then executes first `MulticastReceiver` and then `MulticastSender`.

- **`testStreaming.sh`**: This script compiles the Java classes in `WorkingTools/StreamingService` and starts first the UDP Proxy and then the Streaming service. It will stream to `127.0.0.1:1234`.

- **`testTFTP.sh`**: This script compiles the Java classes in `WorkingTools/TFTP` and starts first the TFTP Server and then downloads a sample file from the server and upload a file from the client.

- **`testDSTP.sh`**: This script compiles the Java classes in `src/`. It's made to test a basic use of the DSTP implementation by sending two messages to a server.

## DSTP Implementation

The DSTP (Data Secure Transport Protocol) implementation in this project ensures secure and reliable data transmission. Key features include:

- **Packet Structure**: Each packet includes a version number (16 bits), a release number (8 bits), and the size of the encrypted payload (16 bits) at the beginning. These fields are not encrypted.

- **Encryption and Integrity**: The payload is encrypted using a symmetric key algorithm specified in the configuration. Integrity is ensured using either HMAC or a hash function, also specified in the configuration.

- **Packet Validation**: On the receiving end, packets are validated to ensure that the version number, release number, and payload size match the expected values. Packets failing these checks are discarded.

- **Sequence Numbers**: Each packet includes a sequence number to prevent replay attacks and ensure the correct order of packets.

### Key Classes

- **`DSTPPacket`**: Handles the creation and parsing of packets, including encryption, integrity checks, and adding metadata (version number, release number, payload size).

- **`DSTPSocket`**: Manages sending and receiving packets, including validation and handling of sequence numbers.

- **`DSTPConfig`**: Parses the configuration file and provides the necessary cryptographic keys and algorithms for encryption and integrity checks.

### Test benches

The folder `WorkingTools/` contains three applications that were modified to take advantage of the secure communication provided by DSTP.
Each folder contains a README with the changes that were made to that application.
Use the bash scripts at the root of the project to test each application.