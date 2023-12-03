import { createContext, PropsWithChildren, useContext, useState } from "react";

import { isAuthenticated, setAuthData } from "../utils/auth";
import useQuery from "../hooks/useQuery";
import { AuthData } from "../types/interfaces";
import { BE_ENDPOINT } from "../api/config";

interface IAuthContext {
  isLoggedIn: boolean;
  loginWithName: (name: string) => void;
  isLoginInProgress: boolean;
}

const AuthContext = createContext({} as IAuthContext);

export const AuthContextProvider = ({children}: PropsWithChildren) => {

  const [isLoggedIn, setIsLoggedIn] = useState(isAuthenticated());
  const { post, isLoading: isLoginInProgress } = useQuery();

  const loginWithName = (name: string) => {
    post({
      url: `${BE_ENDPOINT}?name=${name}`,
      // onSuccess: () => setIsLoggedIn(true),
      responseHandler: (authData: AuthData) => {
        setAuthData(authData);
        setIsLoggedIn(true);
      }
    });
  };

  return (
    <AuthContext.Provider value={{ isLoggedIn, loginWithName, isLoginInProgress }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuthContext = () => useContext(AuthContext);
