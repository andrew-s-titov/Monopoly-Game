import i18next from 'i18next';
import { initReactI18next, useTranslation } from 'react-i18next';

import ru from './ru/translation.json';
import en from './en/translation.json';

const NAMESPACE = 'main';

i18next.use(initReactI18next).init({
  lng: 'ru', // if you're using a language detector, do not define the lng option
  debug: true,
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
