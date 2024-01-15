import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuthContext } from "../context/AuthContextProvider";

const AuthenticationLayout = () => {

  const { isLoggedIn } = useAuthContext();

  return (
    isLoggedIn
      ? <Outlet/>
      : <Navigate to={'/login'}/>
  );
}

export default AuthenticationLayout;
