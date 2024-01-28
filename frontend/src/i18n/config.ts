import i18next from 'i18next';
import { initReactI18next, useTranslation } from 'react-i18next';

import ru from './ru/translation.json';
import en from './en/translation.json';

const NAMESPACE = 'main';
export const supportedLanguages = ['en', 'ru'];
export const langStorageKey = 'i18nLang';

const initialLanguage = (): string => {
  const localStorageLanguage = localStorage.getItem(langStorageKey);
  if (localStorageLanguage && supportedLanguages.includes(localStorageLanguage)) {
    return localStorageLanguage;
  }
  const browserLanguage = navigator.language;
  if (browserLanguage.startsWith('ru')) {
    return 'ru';
  }
  return 'en'
}


i18next.use(initReactI18next).init({
  lng: initialLanguage(),
  debug: false,
  resources: {
    en: {
      [NAMESPACE]: en,
    },
    ru: {
      [NAMESPACE]: ru,
    }
  },
});

export const useTranslations = () => useTranslation(NAMESPACE);

