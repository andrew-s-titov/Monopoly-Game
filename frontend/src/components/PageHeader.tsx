import { useMemo } from "react";

import PlayerAvatar from "./player/PlayerAvatar";
import { getUserAvatar } from "../utils/auth";
import { NavigatorLink } from "../context/Routing";

const PageHeader = () => {

  const avatarName = useMemo(getUserAvatar, []);

  return (
    <div className="page-header">
      <div className="header-buttons">
        <NavigatorLink to='home'>Home</NavigatorLink>
      </div>
      <PlayerAvatar
        avatarName={avatarName}
        className="header-avatar"
      />
    </div>
  );
}

export default PageHeader;