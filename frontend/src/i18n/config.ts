import i18next from 'i18next';
import { initReactI18next, useTranslation } from 'react-i18next';
import HttpBackend from 'i18next-http-backend';

import ru from './ru.json';
import en from './en.json';
import { BE_ENDPOINT } from "../config/api";

const NAMESPACE = 'main';
const defaultLang = 'en';
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
  return defaultLang;
}

i18next
  .use(initReactI18next)
  .use(HttpBackend)
  .init({
    backend: {
      loadPath: `${BE_ENDPOINT}/i18n/{{lng}}.json`,
    },
    supportedLngs: supportedLanguages,
    preload: supportedLanguages,
    fallbackLng: defaultLang,
    lng: initialLanguage(),
    ns: [NAMESPACE],
    debug: true,
    interpolation: {
      escapeValue: false,
    },
    partialBundledLanguages: true,
  });
i18next.on('initialized', () => {
  i18next.addResourceBundle('en', NAMESPACE, en);
  i18next.addResourceBundle('ru', NAMESPACE, ru);
})

export const useTranslations = () => useTranslation(NAMESPACE);

