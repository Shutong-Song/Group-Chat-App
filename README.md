# A simple group chat application using Java

### background
* A GUI application for studying client-server, socket, threadpool, and concurrency
    - It uses java swing to create user interface, and applies client-server to support multi-users group chat.
    - It set a fixed thread pool with 10 threads, you can increase it if you need more.
    - this project is inspired by the two blogs.
        * check out [reference1](https://cs.lmu.edu/~ray/notes/javanetexamples/)
        * check out [reference2](https://ashishmyles.com/tutorials/tcpchat/index.html)

### functionalities
* Connection panels with hostIP and port
    - support client and server connect and disconnect.
    - all disconnected
    ![connection panel](images/clientserver01.png)
    - server ON
    ![serverON](images/clientserver02.png)
    - one user ON
    ![userConnected](images/clientserver05.png)

* connection status bar 
    - support status report on connecting: connected, disconnected, etc.

* chatter name selection
    - support name for each connected chatter
    - keep asking for name if input name is not unique
    ![username](images/clientserver08.png)

* total chatters online
    - support display of the number of current chatters in the room
    ![usercCount](images/clientserver09.png)

* message formatting
    - support system messages with blue color shown in the middle
    - support other chatters messages shown on the left of the panel
    - support self message display on the right side
    ![systemMSG](images/clientserver18.png)

* multi-users group chat
    - support multiple users join, leave, and rejoin
    ![userLeave](images/clientserver22.png)

### A demo with three users in the group chat
![groupchat](images/client_server.gif)

### Happy exploring! Star if you like it!