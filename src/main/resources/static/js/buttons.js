import {getBaseGameUrl, sendGetHttpRequest} from "./http.js";

export const PROPERTY_MANAGEMENT_PREFIX = "management";

const ACTION_CONTAINER_ID = 'action_container';
const PROPERTY_MANAGEMENT_CONTAINER_ID = `${PROPERTY_MANAGEMENT_PREFIX}_${ACTION_CONTAINER_ID}`;
const THROW_DICE_BUTTON_ID = 'throw-dice-button';

export function addClickEvent(button, listenerFunction) {
    button.addEventListener('click', () => listenerFunction());
}

export function createActionButton(text, url, disabled) {
    const actionButton = document.createElement('button');
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

export function renderThrowDiceButton() {
    const throwDiceButton = createActionButton('Roll the dice!', `${getBaseGameUrl()}/dice/roll`, false);
    throwDiceButton.id = THROW_DICE_BUTTON_ID;
    throwDiceButton.classList.add('roll-the-dice-button', 'flashing');
    addClickEvent(throwDiceButton, () => throwDiceButton.remove());
    document.getElementById('message-container').appendChild(throwDiceButton);
}

export function hideThrowDiceButton() {
    const throwDiceButton = document.getElementById(THROW_DICE_BUTTON_ID);
    if (throwDiceButton) {
        throwDiceButton.remove();
    }
}

export function removeOldActionContainer() {
    const oldContainer = document.getElementById(ACTION_CONTAINER_ID);
    if (oldContainer) {
        oldContainer.remove();
    }
}

export function renderActionContainer(text, button1, button2) {
    const actionContainer = document.createElement('div');
    actionContainer.id = ACTION_CONTAINER_ID;
    actionContainer.className = 'action-container';

    const actionPhrase = document.createElement('div');
    actionPhrase.className = 'action-phrase';
    actionPhrase.innerText = text;
    actionContainer.appendChild(actionPhrase);
    for (let button of [button1, button2]) {
        if (button != null) {
            addClickEvent(button, () => actionContainer.remove());
            actionContainer.appendChild(button);
        }
    }
    document.getElementById('message-container').appendChild(actionContainer);
}

export function renderPropertyManagementContainer(htmlPropertyField, fieldIndex, availableActions) {
    if (availableActions.length <= 0) {
        return;
    }
    const managementContainer = document.createElement("div");
    managementContainer.id = PROPERTY_MANAGEMENT_CONTAINER_ID;
    managementContainer.className = 'management-container';
    htmlPropertyField.appendChild(managementContainer);

    const closeOnClickedOutsideListener = function (event) {
        if (event.target.id !== PROPERTY_MANAGEMENT_CONTAINER_ID) {
            finishPropertyManagement(managementContainer, closeOnClickedOutsideListener);
        }
    };
    document.addEventListener('click', closeOnClickedOutsideListener);
    for (let action of availableActions) {
        let buttonText;
        let buttonOnClickFunction;
        let idPostfix;
        if (action === 'INFO') {
            buttonText = 'Info';
            idPostfix = buttonText;
            // TODO: show info card atop of everything - actual implementation;
            buttonOnClickFunction = () => alert('info should be here');
        } else if (action === 'MORTGAGE') {
            buttonText = 'Mortgage';
            idPostfix = buttonText;
            buttonOnClickFunction = () => sendGetHttpRequest(`${getBaseGameUrl()}/field/${fieldIndex}/mortgage`, true);
        } else if (action === 'REDEEM') {
            buttonText = 'Redeem';
            idPostfix = buttonText;
            buttonOnClickFunction = () => sendGetHttpRequest(`${getBaseGameUrl()}/field/${fieldIndex}/redeem`, true);
        } else if (action === 'BUY_HOUSE') {
            buttonText = 'Buy a house';
            idPostfix = 'buy';
            buttonOnClickFunction = () => sendGetHttpRequest(`${getBaseGameUrl()}/field/${fieldIndex}/buy_house`, true);
        } else if (action === 'SELL_HOUSE') {
            buttonText = 'Sell a house';
            idPostfix = 'sell';
            buttonOnClickFunction = () => sendGetHttpRequest(`${getBaseGameUrl()}/field/${fieldIndex}/sell_house`, true);
        } else {
            finishPropertyManagement(managementContainer, closeOnClickedOutsideListener);
            console.error('unknown action type');
            return;
        }
        const button = createManagementButton(managementContainer, buttonText, idPostfix, buttonOnClickFunction);
        addClickEvent(button, () => finishPropertyManagement(managementContainer, closeOnClickedOutsideListener));
    }
}

export function renderGiveUpConfirmation() {
    const confirmationShadow = document.createElement('div');
    confirmationShadow.className = 'fullscreen-shadow-container';
    document.body.appendChild(confirmationShadow);
    const confirmContent = document.createElement('div');
    confirmContent.className = 'center-screen-container';
    confirmationShadow.appendChild(confirmContent);
    const confirmTextElement = document.createElement('p');
    confirmTextElement.innerText = 'Do you really want to give up?';
    confirmContent.appendChild(confirmTextElement);

    const confirmGiveUpButton = createActionButton('Give up', `${getBaseGameUrl()}/player/give_up`, false);
    confirmGiveUpButton.style.backgroundColor = 'red';
    confirmGiveUpButton.style.color = 'white';
    const cancelGiveUpButton = createActionButton('Cancel');
    for (let button of [confirmGiveUpButton, cancelGiveUpButton]) {
        confirmContent.appendChild(button);
        addClickEvent(button, () => confirmationShadow.remove());
    }
}

function createManagementButton(managementContainer, buttonText, idPostfix, onclickFunction) {
    const button = document.createElement('button');
    button.className = 'manage-field-button';
    button.textContent = buttonText;
    button.id = `${PROPERTY_MANAGEMENT_PREFIX}-${idPostfix}`;
    managementContainer.appendChild(button);
    addClickEvent(button, () => onclickFunction());
    return button;
}

function finishPropertyManagement(managementContainer, closeOnClickListener) {
    managementContainer.remove();
    document.removeEventListener('click', closeOnClickListener);
}