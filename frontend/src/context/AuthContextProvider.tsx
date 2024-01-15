import { createContext, PropsWithChildren, useContext, useState } from "react";

import { isAuthenticated } from "../utils/auth";

interface IAuthContext {
  isLoggedIn: boolean;
  setLoggedIn: () => void;
}

const AuthContext = createContext({} as IAuthContext);

export const AuthContextProvider = ({ children }: PropsWithChildren) => {

  const [isLoggedIn, setIsLoggedIn] = useState(isAuthenticated);

  return (
    <AuthContext.Provider value={{ isLoggedIn, setLoggedIn: () => setIsLoggedIn(true) }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuthContext = () => useContext(AuthContext);
