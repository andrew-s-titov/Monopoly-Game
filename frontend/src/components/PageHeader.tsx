import { Link } from "react-router-dom";
import PlayerAvatar from "./player/PlayerAvatar";
import { useMemo } from "react";
import { getUserAvatar } from "../utils/auth";

const PageHeader = () => {

  const avatarName = useMemo(getUserAvatar, []);

  return (
    <div className="page-header">
      <div className="header-buttons">
        <Link to='/'>Home</Link>
      </div>
      <PlayerAvatar
        avatarName={avatarName}
        className="header-avatar"
      />
    </div>
  );
}

export default PageHeader;