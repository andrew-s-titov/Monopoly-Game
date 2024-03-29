import GameMap from "./GameMap";
import PlayerContainer from "./player/PlayerContainer";
import LanguageSwitcher from "./LanguageSwitcher";

const GameContent = () => {

  return (
    <div className='game-content-container'>
      <PlayerContainer/>
      <GameMap/>
      <div className='header-right-block' style={{ height: '8vh' }}>
        <div className='header-lang'>
          <LanguageSwitcher/>
        </div>
      </div>
    </div>
  );
}

export default GameContent;
