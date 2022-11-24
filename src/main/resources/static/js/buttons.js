import {sendGetHttpRequest} from "./http.js";

export const PROPERTY_MANAGEMENT_PREFIX = "management";
export const PROPERTY_MANAGEMENT_BASE_URL = 'http://localhost:8080/game/field';

const ACTION_BUTTONS_CONTAINER_ID = 'action_container';
const PROPERTY_MANAGEMENT_CONTAINER_ID = ACTION_BUTTONS_CONTAINER_ID + '_container';

export function addClickEvent(button, listenerFunction) {
    button.addEventListener('click', () => listenerFunction());
}

export function createActionButton(text, url, able) {
    let actionButton = document.createElement('button');
    actionButton.innerText = text;
    actionButton.style.display = 'block';
    actionButton.style.width = '150px';
    actionButton.style.margin = 'auto';
    if (!able) {
        actionButton.disabled = true;
    }
    addClickEvent(actionButton, () => sendGetHttpRequest(url, true));
    return actionButton;
}

export function removeOldActionContainer() {
    let oldContainer = document.getElementById(ACTION_BUTTONS_CONTAINER_ID);
    if (oldContainer) {
        oldContainer.remove();
    }
}

export function renderActionContainer(text, button1, button2) {
    let actionContainer = document.createElement('div');
    actionContainer.id = ACTION_BUTTONS_CONTAINER_ID;
    actionContainer.style.width = '20%';
    actionContainer.style.opacity = '0.9'
    actionContainer.style.position = 'fixed';
    actionContainer.style.left = '45%';
    actionContainer.style.top = '35%';
    actionContainer.style.backgroundColor = 'black';

    let actionPhrase = document.createElement('div');
    actionPhrase.innerText = text;
    actionPhrase.style.fontSize = '15px';
    actionPhrase.style.color = 'white';
    actionPhrase.style.textAlign = 'center';
    actionPhrase.style.marginTop = '20px';
    actionPhrase.style.marginBottom = '20px';
    actionContainer.appendChild(actionPhrase);
    for (let button of [button1, button2]) {
        if (button != null) {
            addClickEvent(button, () => actionContainer.remove());
            button.style.marginBottom = '10px';
            actionContainer.appendChild(button);
        }
    }
    document.getElementById('map').appendChild(actionContainer);
}

export function renderPropertyManagementContainer(htmlPropertyField, fieldIndex, availableActions) {
    let managementContainer = document.createElement("div");
    managementContainer.id = PROPERTY_MANAGEMENT_CONTAINER_ID;
    managementContainer.style.position = 'absolute';
    managementContainer.style.width = '100%';
    managementContainer.style.height = '100%';
    managementContainer.style.opacity = '0.8';
    managementContainer.style.background = 'black';
    managementContainer.style.writingMode = '';
    managementContainer.style.transform = '';
    document.addEventListener('click', event => {
        if (event.target.id !== PROPERTY_MANAGEMENT_CONTAINER_ID) {
            managementContainer.remove();
        }
    });
    for (let action of availableActions) {
        let button = document.createElement('button');
        button.style.width = '100%';
        button.style.color = 'black';
        button.style.fontSize = '10px';
        if (action === 'INFO') {
            // TODO: show info card atop of everything;
            button.innerHTML = 'Info';
            button.id = `${PROPERTY_MANAGEMENT_PREFIX}-info`;
            // TODO: actual info implementation
            addClickEvent(button, () => alert('info should be here'));
        } else if (action === 'MORTGAGE') {
            button.innerHTML = 'Mortgage';
            button.id = `${PROPERTY_MANAGEMENT_PREFIX}-mortgage`;
            addClickEvent(button, () =>
                sendGetHttpRequest( `${PROPERTY_MANAGEMENT_BASE_URL}/${fieldIndex}/mortgage`, true));
        } else if (action === 'REDEEM') {
            button.innerHTML = 'Redeem';
            button.id = `${PROPERTY_MANAGEMENT_PREFIX}-redeem`;
            addClickEvent(button, () =>
                sendGetHttpRequest( `${PROPERTY_MANAGEMENT_BASE_URL}/${fieldIndex}/redeem`, true));
        } else if (action === 'BUY_HOUSE') {
            button.innerHTML = 'Buy a house';
            button.id = `${PROPERTY_MANAGEMENT_PREFIX}-buy-house`;
            addClickEvent(button, () =>
                sendGetHttpRequest(`${PROPERTY_MANAGEMENT_BASE_URL}/${fieldIndex}/buy_house`, true));
        } else if (action === 'SELL_HOUSE') {
            button.innerHTML = 'Sell a house';
            button.id = `${PROPERTY_MANAGEMENT_PREFIX}-sell-house`;
            addClickEvent(button, () =>
                sendGetHttpRequest( `${PROPERTY_MANAGEMENT_BASE_URL}/${fieldIndex}/sell_house`, true));
        }
        addClickEvent(button, () => managementContainer.remove());
        managementContainer.appendChild(button);
    }
    htmlPropertyField.appendChild(managementContainer);
}