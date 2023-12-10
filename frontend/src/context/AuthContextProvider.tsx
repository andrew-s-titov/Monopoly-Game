import { createContext, PropsWithChildren, useContext, useState } from "react";

import { isAuthenticated, setAuthData } from "../utils/auth";
import useQuery from "../hooks/useQuery";
import { LoginData, LoginResponse } from "../types/interfaces";
import { BE_ENDPOINT } from "../api/config";

interface IAuthContext {
  isLoggedIn: boolean;
  login: (loginData: LoginData) => void;
  isLoginInProgress: boolean;
}

const AuthContext = createContext({} as IAuthContext);

export const AuthContextProvider = ({children}: PropsWithChildren) => {

  const [isLoggedIn, setIsLoggedIn] = useState(isAuthenticated());
  const { post, isLoading: isLoginInProgress } = useQuery();

  const login = (loginData: LoginData) => {
    post({
      url: `${BE_ENDPOINT}`,
      body: loginData,
      // onSuccess: () => setIsLoggedIn(true),
      responseHandler: ({ id }: LoginResponse) => {
        setAuthData(loginData, id);
        setIsLoggedIn(true);
      }
    });
  };

  return (
    <AuthContext.Provider value={{ isLoggedIn, login, isLoginInProgress }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuthContext = () => useContext(AuthContext);
