import React from "react";
import { Outlet } from "react-router-dom";

import { MessageProvider } from "./context/MessageProvider";
import { AuthContextProvider } from "./context/AuthContextProvider";

const App = () => {
  return (
    <AuthContextProvider>
      <MessageProvider>
        <Outlet/>
      </MessageProvider>
    </AuthContextProvider>
  );
}

export default App;
