import { memo, PropsWithChildren, useEffect, useRef } from "react";

const initialBackgroundImageWidth = 1086;
const initialBackgroundImageHeight = 610;

const StartPageBackground = ({ children }: PropsWithChildren) => {

  const background = useRef<HTMLDivElement>(null);

  const resize = () => {
    const widthProportion = window.innerWidth / initialBackgroundImageWidth;
    const heightProportion = window.innerHeight / initialBackgroundImageHeight;
    background.current?.style.setProperty('background-size',
      heightProportion > widthProportion ? 'auto 100vh' : '100vw');
  }

  useEffect(() => {
    resize();
    window.addEventListener('resize', resize);
    return () => window.removeEventListener('resize', resize);
  }, []);

  return (
    <div
      ref={background}
      className="full-size-background"
    >
      {children}
    </div>
  );
}

export default memo(StartPageBackground);
