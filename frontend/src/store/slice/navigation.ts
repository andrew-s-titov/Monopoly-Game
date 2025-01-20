import { createSlice, PayloadAction } from '@reduxjs/toolkit'

export enum AppPage {
  GAME = 'game',
  HOME = 'home',
}

interface NavigationState {
  currentPage: AppPage,
  currentGameId?: string,
}

const initialState: NavigationState = {
  currentPage: AppPage.HOME,
};

export const navigationSlice = createSlice({
  name: 'navigation',
  initialState,
  reducers: {
    navigate: (state: NavigationState, action: PayloadAction<AppPage>) => {
      state.currentPage = action.payload;
    },
    navigateToGame: (state: NavigationState, action: PayloadAction<string>) => {
      state.currentPage = AppPage.GAME;
      state.currentGameId = action.payload;
    },
  },
});

export const { navigate, navigateToGame } = navigationSlice.actions;

export default navigationSlice.reducer;
