import { createSlice } from '@reduxjs/toolkit'

import { isAuthenticated } from "../../utils/auth";

interface AuthState {
  isLoggedIn: boolean,
}

const initialState = (): AuthState => {
  const isLoggedIn = isAuthenticated();
  return {
    isLoggedIn,
  }
};

export const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    completeLogin: (authState) => {
      authState.isLoggedIn = true;
    },
  },
});

export const { completeLogin } = authSlice.actions;

export default authSlice.reducer;
