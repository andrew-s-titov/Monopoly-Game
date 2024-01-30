import { useLanguageContext } from "../context/LanguageContextProvider";
import { Button } from "primereact/button";
import { supportedLanguages } from "../i18n/config";
import { useState } from "react";

const LanguageSwitcher = () => {

  const { currentLanguage, changeLanguage } = useLanguageContext();
  const [contentHidden, setContentHidden] = useState(true);
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
              style={{ padding: '0' }}
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
