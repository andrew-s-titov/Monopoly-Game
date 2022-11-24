import {PLAYER_COLORS} from './colors.js'
import {moveChip} from "./chip-movement.js";

const playersMap = new Map();
const chipsMap = new Map();

export function addPlayers(jsonPlayerArray) {
    for (let index = 0; index < jsonPlayerArray.length; index++) {
        let player = jsonPlayerArray[index];
        let playerObject = new Player(index, player.name, player.money, player.position, PLAYER_COLORS[index]);
        playersMap.set(player.id, playerObject);

        renderPlayerName(playerObject);
        renderPlayerMoney(playerObject);
        renderPlayerPicture(playerObject);
        renderPlayerChip(playerObject);
    }
}

export function changePlayerMoney(playerId, money) {
    let playerObject = playersMap.get(playerId);
    playerObject.money = money;
    renderPlayerMoney(playerObject);
}

export function getPlayerIndex(playerId) {
    return playersMap.get(playerId).index;
}

export function getPlayerColor(playerId) {
    return playersMap.get(playerId).color;
}

export function getPlayerName(playerId) {
    return playersMap.get(playerId).name;
}

export function movePlayerChip(playerId, fieldIndex) {
    let playerIndex = playersMap.get(playerId).index;
    let chip = chipsMap.get(playerIndex);
    moveChip(chip, fieldIndex);
}

function renderPlayerName(playerObject) {
    let playerNameField = document.getElementById(`player${playerObject.index}-name`);
    playerNameField.innerHTML = playerObject.name;
    playerNameField.style.backgroundColor = playerObject.color;
}

function renderPlayerMoney(playerObject) {
    let playerMoneyField = document.getElementById(`player${playerObject.index}-money`);
    playerMoneyField.innerHTML = `$ ${playerObject.money}`;
    playerMoneyField.style.backgroundColor = playerObject.color;
}

function renderPlayerPicture(playerObject) {
    let playerIcon = document.createElement('img');
    playerIcon.setAttribute('src', 'images/user.png')
    playerIcon.style.width = '90px';
    playerIcon.style.height = '90px';
    let playerIconField = document.getElementById(`player${playerObject.index}-icon`);
    playerIconField.appendChild(playerIcon);
    playerIconField.style.backgroundColor = playerObject.color;
}

function renderPlayerChip(playerObject) {
    let index = playerObject.index;

    let chip = document.createElement('div');
    chip.id = `chip${index}`;
    chip.style.width = '24px';
    chip.style.height = '24px';
    chip.style.borderRadius = '15px';
    chip.style.position = 'fixed';
    chip.style.boxShadow = '1px 1px 1px 0.8px black';
    chip.style.transition = 'left 0.4s, top 0.4s, right 0.4s, down 0.4s';
    chip.style.transitionTimingFunction = 'linear';
    chip.style.background = 'grey';
    chip.style.opacity = '0.8';

    let chipInnerCircle = document.createElement('div');
    chipInnerCircle.style.width = '20px';
    chipInnerCircle.style.height = '20px';
    chipInnerCircle.style.borderRadius = '10px';
    chipInnerCircle.style.margin = '2px';
    chipInnerCircle.style.background = playerObject.color;

    chip.appendChild(chipInnerCircle);
    chipsMap.set(index, chip);
    document.getElementById('map').appendChild(chip);
    moveChip(chip, playerObject.position);
}

class Player {
    index;
    name;
    money;
    position;
    color;

    constructor(index, name, money, position, color) {
        this.index = index;
        this.name = name;
        this.money = money;
        this.position = position;
        this.color = color;
    }
}