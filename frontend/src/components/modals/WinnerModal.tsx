import { memo, useEffect } from "react";
import { Fireworks} from "fireworks-js";

interface IWinnerModalProps {
  name: string,
}

const WinnerModal = ({ name }: IWinnerModalProps) => {

  useEffect(() => {
    const fireworksContainer = document.getElementById('fireworks');

    const fireWorks = fireworksContainer && new Fireworks(fireworksContainer,
      {
        hue: {
          min: 0,
          max: 325
        },
        acceleration: 1,
        brightness: {
          min: 30,
          max: 80
        },
        sound: {
          enabled: true,
          files: [
            'https://fireworks.js.org/sounds/explosion0.mp3',
            'https://fireworks.js.org/sounds/explosion1.mp3',
            'https://fireworks.js.org/sounds/explosion2.mp3'
          ],
          volume: {
            min: 2,
            max: 24,
          }
        },
        delay: {
          min: 15,
          max: 20
        },
        rocketsPoint: {
          min: 50,
          max: 50
        },
        friction: 0.99,
        gravity: 1,
        particles: 30,
        intensity: 30,
      });
    fireWorks && fireWorks.start();
    return () => {
      fireWorks && fireWorks.stop(true)
    };
  }, []);

  return (
    <div className="winner-modal" id="winner-modal">
      <i className="pi pi-star-fill pi-spin icon"/>
        <span>{`${name} is the winner!`}</span>
      <i className="pi pi-star-fill pi-spin animation-backwards"/>
    </div>
  );
}

export default memo(WinnerModal);
