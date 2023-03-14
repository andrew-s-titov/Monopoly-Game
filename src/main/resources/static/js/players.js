import {moveChip, moveToStart} from './chip-movement.js';
import {addClickEvent, renderGiveUpConfirmation} from './buttons.js';
import {get, baseGameUrl} from './http.js';
import {startOfferProcess} from './offer.js';

const MAX_PLAYERS = 5;
const PLAYER_MAP = new Map();
const PLAYER_INFO_HTML_FIELDS = [];
const CHIPS = [];
const PLAYER_COLORS = [
    'cornflowerblue',
    'crimson',
    'mediumseagreen',
    'purple',
    'orange'
];

export function clearPlayerInfo() {
    if (PLAYER_INFO_HTML_FIELDS.length !== 0) {
        for (let playerHtmlInfo of PLAYER_INFO_HTML_FIELDS) {
            playerHtmlInfo.iconField.className = '';
            playerHtmlInfo.iconField.style.boxShadow = 'none';
            playerHtmlInfo.nameField.textContent = '';
            playerHtmlInfo.moneyField.textContent = '';
        }
    }
    if (CHIPS.length !== 0) {
        for (let chip of CHIPS) {
            moveToStart(chip);
            chip.style.display = 'none';
        }
    }
}

export function addPlayers(jsonPlayerArray) {
    const arrayLength = jsonPlayerArray.length;
    for (let index = 0; index < arrayLength; index++) {
        const jsonPlayer = jsonPlayerArray[index];
        const playerColor = PLAYER_COLORS[index];
        const playerId = jsonPlayer.id;
        const playerName = jsonPlayer.name;
        const playerObject = new Player(index, playerName, playerColor);
        PLAYER_MAP.set(playerId, playerObject);

        const playerInfoHTMLFields = getPlayerInfoHTMLFields(index);
        playerInfoHTMLFields.nameField.textContent = playerName;
        playerInfoHTMLFields.moneyField.textContent = `$ ${jsonPlayer.money}`;
        renderPlayerPicture(index, playerId, playerColor);
        if (jsonPlayer.hasOwnProperty('bankrupt') && !jsonPlayer.bankrupt) {
            renderPlayerChip(index, jsonPlayer.position);
            playerInfoHTMLFields.nameField.style.color = 'white';
            playerInfoHTMLFields.moneyField.style.color = 'white';
        } else {
            renderPlayerAsBankrupt(playerInfoHTMLFields);
        }
    }
}

export function bankruptPlayer(playerId) {
    const playerIndex = getPlayerIndexById(playerId);
    const playerInfoHTMLFields = getPlayerInfoHTMLFields(playerIndex);
    renderPlayerAsBankrupt(playerInfoHTMLFields);
    getChip(playerIndex).style.display = 'none';
}

export function changePlayerMoney(playerId, money) {
    const playerIndex = getPlayerIndexById(playerId);
    getPlayerInfoHTMLFields(playerIndex).moneyField.textContent = `$ ${money}`;
}

export function getPlayerIndexById(playerId) {
    return PLAYER_MAP.get(playerId).index;
}

export function getPlayerColorById(playerId) {
    return PLAYER_MAP.get(playerId).color;
}

export function getPlayerNameById(playerId) {
    return PLAYER_MAP.get(playerId).name;
}

export function movePlayerChip(playerId, fieldIndex) {
    moveChip(getChip(getPlayerIndexById(playerId)), fieldIndex);
}

function renderPlayerAsBankrupt(playerInfoHTMLFields) {
    playerInfoHTMLFields.nameField.style.color = 'grey';
    playerInfoHTMLFields.moneyField.style.color = 'grey';
    playerInfoHTMLFields.iconField.style.boxShadow = 'none';
}

function initialisePlayers() {
    for (let i = 0; i < MAX_PLAYERS; i++) {
        PLAYER_INFO_HTML_FIELDS.push(new PlayerInfoHTMLFields(
            document.getElementById(`player${i}-icon`),
            document.getElementById(`player${i}-name`),
            document.getElementById(`player${i}-money`)
        ));
    }
}

function getPlayerInfoHTMLFields(index) {
    if (PLAYER_INFO_HTML_FIELDS.length === 0) {
        initialisePlayers();
    }
    return PLAYER_INFO_HTML_FIELDS[index];
}

function initialiseChips() {
    for (let index = 0; index < MAX_PLAYERS; index++) {
        const chip = document.getElementById(`chip${index}`);
        chip.getElementsByClassName('chip-inner')[0].style.background = PLAYER_COLORS[index];
        CHIPS.push(chip);
    }
}

function getChip(index) {
    if (CHIPS.length === 0) {
        initialiseChips();
    }
    return CHIPS[index];
}

function renderPlayerPicture(playerIndex, playerId, playerColor) {
    const playerIconField = getPlayerInfoHTMLFields(playerIndex).iconField;
    playerIconField.classList.add('player-icon');
    playerIconField.style.boxShadow = `0 0 0px 5px ${playerColor}`;
    applyPlayerManagementEvents(playerIconField, playerIndex, playerId);
}

function renderPlayerChip(index, position) {
    const chip = getChip(index);
    chip.style.display = 'block';
    moveChip(chip, position);
}

function applyPlayerManagementEvents(playerIconField, playerIndex, playerId) {
    playerIconField.addEventListener('click', (event) => {
        const eventTargetId = event.target.id;
        if (eventTargetId !== `player${playerIndex}-icon` && eventTargetId.startsWith(`player${playerIndex}`)) {
            return;
        }
        get(`${baseGameUrl()}/player/${playerId}/management`,
            (managementActions) => {
                if (managementActions.length > 0) {
                    renderPlayerManagementContainer(playerIndex, playerId, managementActions);
                }
            });
    });
}

function renderPlayerManagementContainer(playerIndex, playerId, availableActions) {
    const containerId = `player${playerIndex}-action-container`;
    const managementContainer = document.createElement('div');
    managementContainer.className = 'management-container';
    managementContainer.id = containerId;

    const closeOnClickOutsideListener = function (event) {
        if (event.target.id !== containerId) {
            finishPlayerAction(managementContainer, closeOnClickOutsideListener);
        }
    }
    document.addEventListener('click', closeOnClickOutsideListener);
    let actionsAmount = availableActions.length;
    for (let actionIndex = 0; actionIndex < actionsAmount; actionIndex++) {
        const action = availableActions[actionIndex];
        const button = document.createElement('button');
        button.id = `player${playerIndex}-action-button-${actionIndex}`;
        button.className = 'manage-player-button';
        if (action === 'GIVE_UP') {
            button.textContent = 'Give up';
            addClickEvent(button, renderGiveUpConfirmation);
        } else if (action === 'OFFER') {
            button.textContent = 'Offer a contract';
            addClickEvent(button, () => startOfferProcess(playerId, getPlayerNameById(playerId)));
        } else {
            finishPlayerAction(managementContainer, closeOnClickOutsideListener);
            console.error('unknown action type');
            return;
        }
        addClickEvent(button, () => finishPlayerAction(managementContainer, closeOnClickOutsideListener));
        managementContainer.appendChild(button);
    }
    document.getElementById(`player${playerIndex}-group`).appendChild(managementContainer);
}

function finishPlayerAction(managementContainer, closeOnClickListener) {
    managementContainer.remove();
    document.removeEventListener('click', closeOnClickListener)
}

class Player {
    index;
    name;
    color;

    constructor(index, name, color) {
        this.index = index;
        this.name = name;
        this.color = color;
    }
}

class PlayerInfoHTMLFields {
    iconField;
    nameField;
    moneyField;

    constructor(iconField, nameField, moneyField) {
        this.iconField = iconField;
        this.nameField = nameField;
        this.moneyField = moneyField;
    }
}