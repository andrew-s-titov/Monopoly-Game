import GameMap from "./GameMap";
import PlayerContainer from "./player/PlayerContainer";
import Snowfall from "react-snowfall";

const GameContent = () => {

  return (
    <div className='game-content-container'>
      <Snowfall
        snowflakeCount={250}
      />
      <PlayerContainer/>
      <GameMap/>
    </div>
  );
}

export default GameContent;
