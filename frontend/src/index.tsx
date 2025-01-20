import React from "react";
import ReactDOM from "react-dom/client";
import { Provider } from "react-redux";

import App from "./layouts/App";
import store from "./store";

import "./assets/styles/index.css";
import "./assets/styles/chat.css";
import "./assets/styles/game-content.css";
import "./assets/styles/game-map.css";
import "./assets/styles/game-room.css"
import "./assets/styles/start-page.css";

import "primereact/resources/themes/lara-light-indigo/theme.css";
import "primeicons/primeicons.css";

import "./i18n/config";

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

root.render(
  <React.StrictMode>
    <Provider store={store}>
      <App/>
    </Provider>
  </React.StrictMode>
);
