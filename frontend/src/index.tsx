import React from "react";
import ReactDOM from "react-dom/client";
import PageSwitcher from "./components/PageSwitcher";
import { MessageProvider } from "./context/MessageProvider";

import "./assets/styles/index.css"; // TODO: can we import folder with styles?
import "./assets/styles/chat.css";
import "./assets/styles/game-content.css";
import "./assets/styles/game-map.css";
import "./assets/styles/game-room.css"
import "./assets/styles/start-page.css";

import "primereact/resources/themes/lara-light-indigo/theme.css";
import "primeicons/primeicons.css";
import { AuthContextProvider } from "./context/AuthContextProvider";

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

root.render(
  <React.StrictMode>
    <MessageProvider>
      <AuthContextProvider>
        <PageSwitcher/>
      </AuthContextProvider>
    </MessageProvider>
  </React.StrictMode>
);
