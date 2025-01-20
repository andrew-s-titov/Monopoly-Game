import { configureStore } from '@reduxjs/toolkit';

import auth from './slice/auth';
import navigation from './slice/navigation';

const store = configureStore({
  reducer: {
    auth,
    navigation,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export default store;
