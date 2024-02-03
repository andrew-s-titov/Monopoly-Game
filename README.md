# Online Monopoly Game

#### Classic Monopoly game features:
- dice rolling;
- purchasing property fields;
- property auction process;
- purchasing and selling houses and hotels on owned property (streets);
- property mortgage and redeem;
- 'Chance' card deck;
- imprisonment and jail release process;
- making offers and deals between players;
- player bankruptcy due to the lack of assets;
- giving up at any time during the game.

#### Additional features:
- in-game chat  

- - -
Multiple game rooms are available (max 5 players each).  

Language support:  
<img src="frontend/src/assets/images/flags/en.png" alt="English" width="30"/>
<img src="frontend/src/assets/images/flags/ru.png" alt="Russian" width="30"/>

- - -
## Launch

To launch application locally do the following steps:
1. Make sure Java is installed on your computer
2. Download the [latest game release](https://github.com/andrew-s-titov/Monopoly-Game/releases/latest) (`*.jar` archive)
3. Run application via CLI using `java -jar path-to-downloaded-jar` command
4. Open http://localhost:8080 in your browser

This local run will allow you to play the game from different browsers on the same computer.

To launch application to play via Internet on separate computers do the following:
1. Download [ngrok](https://ngrok.com/download)
2. Open ngrok, execute command `ngrok http 8080`
3. Re-do steps 1-3 from local run on one of the computers (server)
4. Open url provided by ngrok

- - -
### _Special thanks to:_
- _Denis Vasilkov_ - for game map and property cards images
