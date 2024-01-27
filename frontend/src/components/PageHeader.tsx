import { useMemo } from "react";

import PlayerAvatar from "./player/PlayerAvatar";
import { getUserAvatar } from "../utils/auth";
import { NavigatorLink } from "../context/Routing";
import { useTranslations } from "../i18n/config";

const PageHeader = () => {

  const { t } = useTranslations();
  const avatarName = useMemo(getUserAvatar, []);

  return (
    <div className="page-header">
      <div className="header-buttons">
        <NavigatorLink to='home'>{t('header.home')}</NavigatorLink>
      </div>
      <PlayerAvatar
        avatarName={avatarName}
        className="header-avatar"
      />
    </div>
  );
}

export default PageHeader;
