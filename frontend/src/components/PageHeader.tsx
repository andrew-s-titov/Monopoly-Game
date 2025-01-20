import { useMemo } from "react";

import PlayerAvatar from "./player/PlayerAvatar";
import { getUserAvatar } from "../utils/auth";
import { NavigatorLink } from "./NavigatorLink";
import { useTranslations } from "../i18n/config";
import LanguageSwitcher from "./LanguageSwitcher";
import { AppPage } from "../store/slice/navigation";

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
              <NavigatorLink to={AppPage.HOME}>{t('header.home')}</NavigatorLink>
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
