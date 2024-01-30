import { createContext, PropsWithChildren, useContext } from "react";
import { langStorageKey, supportedLanguages, useTranslations } from "../i18n/config";

interface ILanguageContext {
  currentLanguage: string,
  changeLanguage: (language: string) => void,
}

const LanguageContext = createContext<ILanguageContext>({
  currentLanguage: 'en',
} as ILanguageContext);

export const LanguageContextProvider = ({ children }: PropsWithChildren) => {

  const { i18n } = useTranslations();
  const currentLanguage: string = i18n.language;

  const changeLanguage = (language: string) => {
    if (supportedLanguages.includes(language)) {
      localStorage.setItem(langStorageKey, language);
      i18n.changeLanguage(language);
    }
  }

  return (
    <LanguageContext.Provider value={{ currentLanguage, changeLanguage }}>
      {children}
    </LanguageContext.Provider>
  );
}

export const useLanguageContext = () => useContext(LanguageContext);
