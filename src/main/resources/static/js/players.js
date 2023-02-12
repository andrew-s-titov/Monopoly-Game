import {moveChip} from './chip-movement.js';
import {addClickEvent, renderGiveUpConfirmation} from './buttons.js';
import {get, baseGameUrl} from './http.js';
import {startOfferProcess} from './offer.js';

const PLAYER_MAP = new Map();
const CHIP_MAP = new Map();
const PLAYER_COLORS = [
    'cornflowerblue',
    'crimson',
    'mediumseagreen',
    'purple',
    'orange'
];

export function addPlayers(jsonPlayerArray) {
    const arrayLength = jsonPlayerArray.length;
    for (let index = 0; index < arrayLength; index++) {
        const jsonPlayer = jsonPlayerArray[index];
        const playerColor = PLAYER_COLORS[index];
        const playerId = jsonPlayer.id;
        const playerName = jsonPlayer.name;
        const playerObject = new Player(index, playerName, playerColor);
        PLAYER_MAP.set(playerId, playerObject);

        document.getElementById(`player${index}-name`).textContent = playerName;
        document.getElementById(`player${index}-money`).textContent = `$ ${jsonPlayer.money}`;
        renderPlayerPicture(index, playerId, playerColor);
        if (jsonPlayer.hasOwnProperty('bankrupt') && !jsonPlayer.bankrupt) {
            renderPlayerChip(index, playerColor, jsonPlayer.position);
        } else {
            document.getElementById(`player${index}-money`).style.color = 'grey';
            document.getElementById(`player${index}-name`).style.color = 'grey';
        }
    }
}

export function bankruptPlayer(playerId) {
    const playerIndex = getPlayerIndexById(playerId);
    document.getElementById(`player${playerIndex}-icon`).style.boxShadow = 'none';
    document.getElementById(`player${playerIndex}-money`).style.color = 'grey';
    document.getElementById(`player${playerIndex}-name`).style.color = 'grey';
    CHIP_MAP.get(playerIndex).remove();
}

export function changePlayerMoney(playerId, money) {
    document.getElementById(`player${getPlayerIndexById(playerId)}-money`).textContent = `$ ${money}`;
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
    const chip = CHIP_MAP.get(getPlayerIndexById(playerId));
    moveChip(chip, fieldIndex);
}

function renderPlayerPicture(playerIndex, playerId, playerColor) {
    const playerIconField = document.getElementById(`player${playerIndex}-icon`);
    playerIconField.classList.add('player-icon');
    playerIconField.style.boxShadow = `0 0 0px 5px ${playerColor}`;

    applyPlayerManagementEvents(playerIconField, playerIndex, playerId);
}

function renderPlayerChip(index, color, position) {
    const chip = document.createElement('div');
    chip.id = `chip${index}`;
    chip.className = 'chip-outer';
    const chipInnerCircle = document.createElement('div');
    chipInnerCircle.className = 'chip-inner';
    chipInnerCircle.style.background = color;

    chip.appendChild(chipInnerCircle);
    CHIP_MAP.set(index, chip);
    document.getElementById('mapTable').appendChild(chip);
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

    const closeOnClickOutsideListener = function(event) {
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