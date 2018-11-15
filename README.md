# csc472Project
#### How to start local server
1. Install node  (https://nodejs.org/en/)
2. Open terminal or cmd, go to directory ./AndroidServer
3. Run command `node app.js`
4. Sever will listen at localhost:8000

#### Choose server to connect in Android app
1. Open GameConfiguration.java
2. Set SERVER_ADDRESS = "10.0.2.2" if you run app in virutal device and want to connect to local server
3. Set SERVER_ADDRESS = "140.192.34.69" to connect remote server
