import {getBaseGameUrl, sendGetHttpRequest} from "./http.js";

export const PROPERTY_MANAGEMENT_PREFIX = "management";

const ACTION_BUTTONS_CONTAINER_ID = 'action_container';
const PROPERTY_MANAGEMENT_CONTAINER_ID = `${PROPERTY_MANAGEMENT_PREFIX}_${ACTION_BUTTONS_CONTAINER_ID}`;

export function addClickEvent(button, listenerFunction) {
    button.addEventListener('click', () => listenerFunction());
}

export function createActionButton(text, url, disabled) {
    let actionButton = document.createElement('button');
    actionButton.innerText = text;
    actionButton.className = 'action-button';
    if (url != null) {
        addClickEvent(actionButton, () => sendGetHttpRequest(url, true));
    }
    if (disabled != null && disabled) {
        actionButton.disabled = true;
    }
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
    actionContainer.className = 'action-container';

    let actionPhrase = document.createElement('div');
    actionPhrase.className = 'action-phrase';
    actionPhrase.innerText = text;
    actionContainer.appendChild(actionPhrase);
    for (let button of [button1, button2]) {
        if (button != null) {
            addClickEvent(button, () => actionContainer.remove());
            actionContainer.appendChild(button);
        }
    }
    document.getElementById('map').appendChild(actionContainer);
}

export function renderPropertyManagementContainer(htmlPropertyField, fieldIndex, availableActions) {
    let managementContainer = document.createElement("div");
    managementContainer.id = PROPERTY_MANAGEMENT_CONTAINER_ID;
    managementContainer.className = 'management-container';

    document.addEventListener('click', event => {
        if (event.target.id !== PROPERTY_MANAGEMENT_CONTAINER_ID) {
            managementContainer.remove();
        }
    });
    for (let action of availableActions) {
        let button = document.createElement('button');
        button.className = 'manage-field-button';
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
                sendGetHttpRequest( `${getBaseGameUrl()}/field/${fieldIndex}/mortgage`, true));
        } else if (action === 'REDEEM') {
            button.innerHTML = 'Redeem';
            button.id = `${PROPERTY_MANAGEMENT_PREFIX}-redeem`;
            addClickEvent(button, () =>
                sendGetHttpRequest( `${getBaseGameUrl()}/field/${fieldIndex}/redeem`, true));
        } else if (action === 'BUY_HOUSE') {
            button.innerHTML = 'Buy a house';
            button.id = `${PROPERTY_MANAGEMENT_PREFIX}-buy-house`;
            addClickEvent(button, () =>
                sendGetHttpRequest(`${getBaseGameUrl()}/field/${fieldIndex}/buy_house`, true));
        } else if (action === 'SELL_HOUSE') {
            button.innerHTML = 'Sell a house';
            button.id = `${PROPERTY_MANAGEMENT_PREFIX}-sell-house`;
            addClickEvent(button, () =>
                sendGetHttpRequest( `${getBaseGameUrl()}/field/${fieldIndex}/sell_house`, true));
        } else {
            managementContainer.remove();
            console.error('unknown action type');
            return;
        }
        addClickEvent(button, () => managementContainer.remove());
        managementContainer.appendChild(button);
    }
    htmlPropertyField.appendChild(managementContainer);
}

export function renderGiveUpConfirmation() {
    let confirmationShadow = document.createElement('div');
    confirmationShadow.className = 'fullscreen-shadow-container';
    document.body.appendChild(confirmationShadow);
    let confirmContent = document.createElement('div');
    confirmContent.className = 'center-screen-container';
    confirmationShadow.appendChild(confirmContent);
    let confirmTextElement = document.createElement('p');
    confirmTextElement.innerText = 'Do you really want to give up?';
    confirmContent.appendChild(confirmTextElement);

    let confirmGiveUpButton = createActionButton('Give up', `${getBaseGameUrl()}/player/give_up`, false);
    confirmGiveUpButton.style.backgroundColor = 'red';
    confirmGiveUpButton.style.color = 'white';
    let cancelGiveUpButton = createActionButton('Cancel');
    for (let button of [confirmGiveUpButton, cancelGiveUpButton]) {
        confirmContent.appendChild(button);
        addClickEvent(button, () => confirmationShadow.remove());
    }
}