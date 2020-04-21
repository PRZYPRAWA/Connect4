# :red_circle::black_circle: Connect4 Game [![License](https://img.shields.io/badge/licence-MIT-blue)](https://choosealicense.com/licenses/mit/) [![Contributions welcome](https://img.shields.io/badge/contributions-welcome-orange.svg)](https://github.com/Ukasz09/Connect4)

Online multiplayer connetion board game

<br/>
- :white_check_mark: Pair programming <br/>
- :white_check_mark: TDD <br/>
- :white_check_mark: Event Driven Architecture (EDA) <br/>
- :white_check_mark: Client server publish/subscribe messaging pattern (by using MQTT 3.1.1 protocol) <br/>

## Rules
https://en.wikipedia.org/wiki/Connect_Four

## Gameplay
<p align="center"><img width=95% src="https://raw.githubusercontent.com/Ukasz09/Connect4/master/gameplay/gameplay.gif"></p>

## Screenshots 
![game screenshot](https://raw.githubusercontent.com/Ukasz09/Connect4/master/gameplay/game2.png)
![wrong column](https://raw.githubusercontent.com/Ukasz09/Connect4/master/gameplay/wrong2.png)
![win](https://raw.githubusercontent.com/Ukasz09/Connect4/master/gameplay/win2.png)

## How to use it ?
:zero:  If you dont have installed MQTT on your PC run this command:

```bash
pip install paho-mqtt
```
or downlad it by using this link: https://mosquitto.org/download/ <br/>

:one:  Download at least `bin` subdirectories, both from `Server` and `Client` directory <br/>

:two:  One of the players need to run a server:
1) open directory `Server/bin`
2) open one of the directories (`Linux` / `Windows`), according to your Operating System:

- Linux

Open it by console with command:

```bash
java -jar Server.jar
```

- Windows <br/>
Windows CMD not support ansi colors in console, so to run game properly, you need to do steps from a) OR b): <br/><br/>
  a) Open it using bash (e.g. `Git Bash`) identically as in Linux <br/>
  b) Open it using `CMD`: <br/>
  	 - Open one of directories (`x64` or `x86`), according to your system version
  	 - run `RunGame.bat` script (just clik on it)

:three:  Both players need to run a client application <br/>
1) open `Client/bin`
2) Repeat steps similiary as for `Server` application (for Linux with command):

```bash
java -jar Client.jar
```

:four:  Enter server ID (from Server console) into both clients application <br/>
:five:  Enjoy the game! <br/>

### TIPS
- If you don't see directories `x64` or `x86` inside `bin/Windows` after downloading it, or you have some other problems, try to disable `Windows Defender`, download directory again and repeat steps

---
## 📫 Authors

| <a href="https://github.com/Ukasz09" target="_blank"><img src="https://avatars0.githubusercontent.com/u/44710226?s=460&v=4" width="100px;"></a> | <a href="https://github.com/PRZYPRAWA" target="_blank"><img src="https://avatars3.githubusercontent.com/u/30748558?s=460&v=4" width="100px;"></a> |
| ------------- | ------------- |
| game logic    | game logic    |
| UI (CLI)      |   		|
| EDA with MQTT |  		|


<br/><br/>
Feel free to contact with us. And hope you enjoy the game 😎