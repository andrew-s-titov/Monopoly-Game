# Monopoly-Game

This is a pet-project - online Monopoly game.  
Here you can find both backend and frontend, using **Java** with Spring for the first 
and **React.js** with TypeScript for the second.  
> Previously the whole frontend was implemented using pure (vanilla) JavaScript.

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

At the current moment only one gaming room is available for players (max of 5 players at a time).  
Most of the communication between frontend and backend happens through Websocket for real-time view changes
for all connected players (clients) and for an in-game chat.

- - -
## Launch

To launch application locally do the following steps:
1. run `mvn clean install` (or `./mvnw clean install` if you don't have locally installed Maven)
2. run spring application class `MonopolyNewApplication.java` from IDE or run `monopoly-new-0.0.1.jar` from `/target` 
3. open `htpp://localhost:8080` in your browser  

This local run will allow you to play the game from different browsers on the same computer.

To launch application to play via internet on separate computers do the following:
1. download [ngrok](https://ngrok.com/download)
2. run ngrok with command `ngrok http 8080`
3. re-do steps 1-2 from local run
4. open url provided by ngrok in your browser

- - -
### _Special thanks:_
- _Denis Vasilkov_ - for game map and property cards images
- _Freepik from Flaticon_ - for player avatars and flags images (<a href="https://www.flaticon.com/free-icons/modern" title="modern icons">Modern icons created by Freepik - Flaticon</a>)
