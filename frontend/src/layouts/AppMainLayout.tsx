import React from "react";
import { Outlet } from "react-router-dom";

import { MessageProvider } from "../context/MessageProvider";

const AppMainLayout = () => {
  return (
    <MessageProvider>
      <Outlet/>
    </MessageProvider>
  );
}

export default AppMainLayout;
