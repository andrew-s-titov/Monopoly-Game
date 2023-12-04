import GameMap from "./GameMap";
import PlayerContainer from "./PlayerContainer";

const GameContent = () => {

  return (
    <div className='game-content-container'>
      <div className="fireworks" id="fireworks"/>
      <PlayerContainer/>
      <GameMap/>
    </div>
  );
}

export default GameContent;