# Monopoly-Game

This is my own pet-project - online Monopoly game.  
Here I'm developing both backend and frontend, using Java and Spring for the first and HTML, CSS and Javascript for the second.

All main game features and rules are already implemented, such as:
- dice rolling;
- purchasing property fields;
- auction process on a refusal to purchase a property field;
- purchasing and selling houses and hotels on owned property fields;
- mortgaging owned property fields;
- 'Chance' card deck;
- imprisonment and jail release process;
- making offers and deals between players;
- player bankruptcy due to the lack of assets;
- giving up at any time during the game.

For now, only one gaming room is available for players (max of 5 players at a time).  
Most of the communication between frontend and backend happens through Websocket for real-time view changes for all connected players (clients) and for an in-game chat.