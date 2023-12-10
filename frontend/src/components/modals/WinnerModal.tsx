import { memo, useEffect, useRef } from "react";
import { Fireworks} from "fireworks-js";

interface IWinnerModalProps {
  name: string,
}

const WinnerModal = ({ name }: IWinnerModalProps) => {

  const fireworksContainer = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const container = fireworksContainer.current;

    const fireworks = container && new Fireworks(container,
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
    fireworks && fireworks.start();
    return () => {
      fireworks && fireworks.stop(true)
    };
  }, []);

  return (
    <div className="winner-modal" id="winner-modal">
      <div className="fireworks" id="fireworks" ref={fireworksContainer}/>
      <i className="pi pi-star-fill pi-spin icon"/>
        <span>{`${name} is the winner!`}</span>
      <i className="pi pi-star-fill pi-spin animation-backwards"/>
    </div>
  );
}

export default memo(WinnerModal);
