import { useState } from "react";
import { Button } from "primereact/button";
import { langStorageKey, supportedLanguages, useTranslations } from "../i18n/config";

const LanguageSwitcher = () => {

  const { i18n } = useTranslations();
  const [ contentHidden, setContentHidden ] = useState(true);

  const currentLanguage: string = i18n.language;
  const changeLanguage = (language: string) => {
    if (supportedLanguages.includes(language)) {
      localStorage.setItem(langStorageKey, language);
      i18n.changeLanguage(language);
    }
  }
  const showContent = () => setContentHidden(false);
  const hideContent = () => setContentHidden(true);

  return (
    <div className='dropdown' onMouseOver={showContent} onMouseOut={hideContent}>
      <div className={`flag flag-${currentLanguage}`}/>
      <div className={`dropdown-content ${contentHidden ? 'hidden' : ''}`}>
        {supportedLanguages
          .filter(lang => lang !== currentLanguage)
          .map(lang =>
            <Button
              key={lang}
              text
              className="flag-button"
              onClick={() => {
                changeLanguage(lang);
                hideContent();
              }}
            >
              <div className={`flag flag-${lang}`}/>
            </Button>
          )
        }
      </div>
    </div>
  );
}

export default LanguageSwitcher;
