import React from "react";

import { MessageProvider } from "../context/MessageProvider";
import { AuthContextProvider } from "../context/AuthContextProvider";
import { PageRoutingProvider } from "../context/Routing";

const App = () => {
  return (
    <AuthContextProvider>
      <MessageProvider>
        <PageRoutingProvider/>
      </MessageProvider>
    </AuthContextProvider>
  );
}

export default App;
