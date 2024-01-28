import { memo, PropsWithChildren, useEffect, useRef } from "react";
import PageHeader from "./PageHeader";

const initialBackgroundImageWidth = 1086;
const initialBackgroundImageHeight = 610;

interface IStartPageProps extends PropsWithChildren {
  headerLangOnly?: boolean;
}

const StartPageBackground = ({ children, headerLangOnly = false }: IStartPageProps) => {

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
      <PageHeader
        langOnly={headerLangOnly}
      />
      <div className="start-page-content">
        {children}
      </div>
    </div>
  );
}

export default memo(StartPageBackground);
