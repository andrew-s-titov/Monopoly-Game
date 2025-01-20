import { configureStore } from '@reduxjs/toolkit';

import auth from './slice/auth';
import navigation from './slice/navigation';
import chatHistory from './slice/chatHistory';

const store = configureStore({
  reducer: {
    auth,
    navigation,
    chatHistory,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export default store;
