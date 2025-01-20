import React from "react";

import { ToastMessageProvider } from "../context/ToastMessageProvider";
import { PageSwitcher } from "../pages";

const App = () => {
  return (
    <ToastMessageProvider>
      <PageSwitcher/>
    </ToastMessageProvider>
  );
}

export default App;
