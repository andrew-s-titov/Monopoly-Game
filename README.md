# Monopoly-Game

This is a pet-project - online Monopoly game.  
Here you can find both backend and frontend, using **Java** with Spring for the first and **React.js** for the second.  
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
