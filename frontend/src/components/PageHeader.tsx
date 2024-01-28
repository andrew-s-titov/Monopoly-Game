import { useMemo } from "react";

import PlayerAvatar from "./player/PlayerAvatar";
import { getUserAvatar } from "../utils/auth";
import { NavigatorLink } from "../context/Routing";
import { useTranslations } from "../i18n/config";
import LanguageSwitcher from "./LanguageSwitcher";

interface IHeaderProps {
  langOnly?: boolean,
}

const PageHeader = ({ langOnly = false }: IHeaderProps) => {

  const { t } = useTranslations();
  const avatarName = useMemo(getUserAvatar, []);

  return (
    <div className="page-header">
      {!langOnly &&
        <div className="header-buttons">
          <NavigatorLink to='home'>{t('header.home')}</NavigatorLink>
        </div>
      }

      <div className='header-right-block'>
        <div className='header-lang'>
          <LanguageSwitcher/>
        </div>
        {!langOnly &&
          <PlayerAvatar
            avatarName={avatarName}
            className="header-avatar"
          />
        }
      </div>
    </div>
  );
}

export default PageHeader;
