import React from "react";

import { MessageProvider } from "../context/MessageProvider";
import { AuthContextProvider } from "../context/AuthContextProvider";
import { PageRoutingProvider } from "../context/Routing";
import { LanguageContextProvider } from "../context/LanguageContextProvider";

const App = () => {
  return (
    <LanguageContextProvider>
      <AuthContextProvider>
        <MessageProvider>
          <PageRoutingProvider/>
        </MessageProvider>
      </AuthContextProvider>
    </LanguageContextProvider>
  );
}

export default App;
