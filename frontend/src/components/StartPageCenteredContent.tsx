import { PropsWithChildren } from "react";

const StartPageCenteredContent = ({ children }: PropsWithChildren) => {
  return (
    <div className="start-page-centered-content">
      {children}
    </div>
  )
}

export default StartPageCenteredContent;
