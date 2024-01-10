import { createContext, PropsWithChildren, useContext, useState } from "react";

import { isAuthenticated, setAuthData } from "../utils/auth";
import useQuery from "../hooks/useQuery";
import { LoginData, LoginResponse } from "../types/interfaces";
import { BE_ENDPOINT } from "../config/api";

interface IAuthContext {
  isLoggedIn: boolean;
  login: (loginData: LoginData) => void;
  isLoginInProgress: boolean;
}

const AuthContext = createContext({} as IAuthContext);

export const AuthContextProvider = ({children}: PropsWithChildren) => {

  const [isLoggedIn, setIsLoggedIn] = useState(isAuthenticated);
  const { post } = useQuery();
  const { execute: executeLogin, isLoading: isLoginInProgress } = post({
    url: `${BE_ENDPOINT}`,
    responseHandler: (loginResponse: LoginResponse) => {
      setAuthData(loginResponse);
      setIsLoggedIn(true);
    }
  });
  const login = (loginData: LoginData) => executeLogin(loginData);

  return (
    <AuthContext.Provider value={{ isLoggedIn, login, isLoginInProgress }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuthContext = () => useContext(AuthContext);
