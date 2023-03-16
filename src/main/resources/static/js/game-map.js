import * as HttpUtils from './http.js';
import * as Buttons from './buttons.js';
import * as PlayerService from './players.js';
import * as Dice from './dice.js';

let _ATOP_MAP_CONTAINER = null;
let _FULLSCREEN_SHADOW = null;
let _GAME_MAP_CONTAINER = null;
let _SEND_PLAYER_MESSAGE_BUTTON = null;
let _PLAYER_MESSAGE_INPUT = null;
let _CHAT_MESSAGE_CONTAINER = null;
let _MESSAGE_CONTAINER_HEIGHT_FILLER = null;
let _THROW_DICE_BUTTON = null;

let _rendered = false;
let _appended = false;

export function renderThrowDiceButton() {
    getThrowDiceButton().style.display = 'block';
}

export function hideThrowDiceButton() {
    getThrowDiceButton().style.display = 'none';
}

function getThrowDiceButton() {
    if (_THROW_DICE_BUTTON === null) {
        createThrowDiceButton();
    }
    return _THROW_DICE_BUTTON;
}

async function createThrowDiceButtonAsync() {
    createThrowDiceButton();
}

function createThrowDiceButton() {
    if (_THROW_DICE_BUTTON === null) {
        _THROW_DICE_BUTTON = Buttons.createActionButton('Roll the dice!', `${HttpUtils.baseGameUrl()}/dice/roll`, false);
        _THROW_DICE_BUTTON.classList.add('roll-the-dice-button', 'screen-centered', 'flashing');
        Buttons.addClickEvent(_THROW_DICE_BUTTON, hideThrowDiceButton);
        getGameMapContainer().firstElementChild.appendChild(_THROW_DICE_BUTTON);
    }
}

export function displayAtopMapMessage(content) {
    if (content === undefined || content === null) {
        console.error('Cannot display empty content');
        return;
    }
    const messageContainer = getAtopMapContainer();
    if (content instanceof HTMLElement) {
        messageContainer.appendChild(content);
    } else {
        getAtopMapContainer().innerHTML = content;
    }
    getFullscreenShadowElement().style.setProperty('display', 'flex', 'important');
}

export function hideAtopMapMessage() {
    getAtopMapContainer().innerHTML = '';
    getFullscreenShadowElement().style.display = 'none';
}

function getAtopMapContainer() {
    if (_ATOP_MAP_CONTAINER === null) {
        _ATOP_MAP_CONTAINER = getFullscreenShadowElement().firstElementChild;
    }
    return _ATOP_MAP_CONTAINER;
}

function getFullscreenShadowElement() {
    if (_FULLSCREEN_SHADOW === null) {
        _FULLSCREEN_SHADOW = document.getElementById('fullscreenShadow');
    }
    return _FULLSCREEN_SHADOW;
}

export function clearMessages() {
    const messageContainer = getChatMessageContainer();
    messageContainer.innerHTML = '';
    if (_MESSAGE_CONTAINER_HEIGHT_FILLER === null) {
        _MESSAGE_CONTAINER_HEIGHT_FILLER = document.createElement('div');
        _MESSAGE_CONTAINER_HEIGHT_FILLER.style.height = '100%';
    }
    messageContainer.appendChild(_MESSAGE_CONTAINER_HEIGHT_FILLER);
}

export function addMessageToChat(htmlDivMessage) {
    const messageContainer = getChatMessageContainer();
    messageContainer.appendChild(htmlDivMessage);
    messageContainer.scrollTop = messageContainer.scrollHeight;
}

export function isRendered() {
    return _rendered;
}

export function render(parentElement) {
    if (_rendered) {
        return;
    }
    parentElement.appendChild(getGameMapContainer());
    if (!_appended) {
        _appended = true;
        Promise.allSettled([
            createThrowDiceButtonAsync(),
            initialisePlayerMessageInputAsync(),
            initialiseChatMessageContainerAsync(),
            PlayerService.initialisePlayerChipsAsync(),
            PlayerService.initialisePlayerInfoAsync(),
            Dice.preloadDiceImages(),
            Dice.initialiseDiceAsync(),
        ])
            .catch(error => console.error(`failed to asynchronously initialise game-map fata: ${error}`));
    }
    _rendered = true;
}

export function hide(parentContainer) {
    if (!_rendered) {
        return;
    }
    parentContainer.innerHTML = '';
    clearMessages();
    _rendered = false;
}

export function getSendPlayerMessageButton() {
    if (!_rendered) {
        console.error('failed to get sendPlayerMessage button - game map is not rendered');
        return;
    }
    if (_SEND_PLAYER_MESSAGE_BUTTON === null) {
        _SEND_PLAYER_MESSAGE_BUTTON = getPlayerMessageInput().nextElementSibling;
    }
    return _SEND_PLAYER_MESSAGE_BUTTON;
}

export function getPlayerMessage() {
    const playerMessageInput = getPlayerMessageInput();
    const message = playerMessageInput.value;
    playerMessageInput.value = '';
    return message;
}

function getPlayerMessageInput() {
    if (!_rendered) {
        console.error('failed to get playerMessage input - game map is not rendered');
        return;
    }
    if (_PLAYER_MESSAGE_INPUT === null) {
        initialisePlayerMessageInput();
    }
    return _PLAYER_MESSAGE_INPUT;
}

async function initialisePlayerMessageInputAsync() {
    initialisePlayerMessageInput();
}

function initialisePlayerMessageInput() {
    if (_PLAYER_MESSAGE_INPUT === null) {
        _PLAYER_MESSAGE_INPUT = document.getElementById('player-message-input');
    }
}

function getChatMessageContainer() {
    if (!_rendered) {
        console.error('failed to get messageContainer - game map is not rendered');
        return;
    }
    if (_CHAT_MESSAGE_CONTAINER === null) {
        initialiseChatMessageContainer();
    }
    return _CHAT_MESSAGE_CONTAINER;
}

async function initialiseChatMessageContainerAsync() {
    initialiseChatMessageContainer();
}

function initialiseChatMessageContainer() {
    if (_CHAT_MESSAGE_CONTAINER === null) {
        _CHAT_MESSAGE_CONTAINER = document.getElementById('chat-message-container');
    }
}

function getGameMapContainer() {
    if (_GAME_MAP_CONTAINER == null) {
        _GAME_MAP_CONTAINER = document.createElement('div');
        _GAME_MAP_CONTAINER.innerHTML = getGameMapHTMLContent();
    }
    return _GAME_MAP_CONTAINER;
}

function getGameMapHTMLContent() {
    return `
<div id="map">
    <table style="margin-left: 300px">
        <tr>
            <td>
                <table id="playersTable" style="border-collapse: collapse; margin-right: 10px">
                    <tbody>
                        <tr>
                            <td>
                                <div id="player0-info-container" class="player-info-container">
                                    <div id="player0-icon"></div>
                                    <div id="player0-name" class="player-name"></div>
                                    <div id="player0-money" class="player-money"></div>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div id="player1-info-container" class="player-info-container">
                                    <div id="player1-icon"></div>
                                    <div id="player1-name" class="player-name"></div>
                                    <div id="player1-money" class="player-money"></div>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div id="player2-info-container" class="player-info-container">
                                    <div id="player2-icon"></div>
                                    <div id="player2-name" class="player-name"></div>
                                    <div id="player2-money" class="player-money"></div>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div id="player3-info-container" class="player-info-container">
                                    <div id="player3-icon"></div>
                                    <div id="player3-name" class="player-name"></div>
                                    <div id="player3-money" class="player-money"></div>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div id="player4-info-container" class="player-info-container">
                                    <div id="player4-icon"></div>
                                    <div id="player4-name" class="player-name"></div>
                                    <div id="player4-money" class="player-money"></div>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </td>
            <td style="position: relative">
                <table id="mapTable" class="map-table">
                    <tbody>
                        <tr>
                            <td class="price-corner-square-field"></td>
                            <td id="field0-price" class="price-corner-horizontal-field"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="price-corner-horizontal-field"></td>
                            <td class="price-corner-square-field"></td>
                        </tr>
                        <tr>
                            <td class="price-corner-vertical-field"></td>
                            <td id="field0" class="corner-field"></td>
                            <td>
                                <div id="field1" class="vertical-field">
                                    <div id='field1-name' class="name-holder"></div>
                                    <div id="field1-price" class="price-tag price-straight stick-bottom"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field2" class="vertical-field">
                                    <div id='field2-name' class="name-holder"></div>
                                    <div id="field2-price" class="price-tag price-straight stick-bottom"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field3" class="vertical-field">
                                    <div id='field3-name' class="name-holder"></div>
                                    <div id="field3-price" class="price-tag price-straight stick-bottom"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field4" class="vertical-field">
                                    <div id='field4-name' class="name-holder"></div>
                                    <div id="field4-price" class="price-tag price-straight stick-bottom"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field5" class="vertical-field">
                                    <div id='field5-name' class="name-holder"></div>
                                    <div id="field5-price" class="price-tag price-straight stick-bottom"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field6" class="vertical-field">
                                    <div id='field6-name' class="name-holder"></div>
                                    <div id="field6-price" class="price-tag price-straight stick-bottom"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field7" class="vertical-field">
                                    <div id='field7-name' class="name-holder"></div>
                                    <div id="field7-price" class="price-tag price-straight stick-bottom"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field8" class="vertical-field">
                                    <div id='field8-name' class="name-holder"></div>
                                    <div id="field8-price" class="price-tag price-straight stick-bottom"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field9" class="vertical-field">
                                    <div id='field9-name' class="name-holder"></div>
                                    <div id="field9-price" class="price-tag price-straight stick-bottom"></div>
                                </div>
                            </td>
                            <td id="field10" class="corner-field"></td>
                            <td class="price-corner-vertical-field"></td>
                        </tr>
                        <tr>
                            <td class="vertical-premap-filler"></td>
                            <td>
                                <div id="field39" class="horizontal-field">
                                    <div id='field39-name' class="name-holder"></div>
                                    <div id="field39-price" class="price-tag text-left stick-right"></div>
                                </div>
                            </td>
                            <td colspan="9" rowspan="8">
                                <div id="chat-message-container">
                                    <div id="height-filler" style="height: 100%"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field11" class="horizontal-field">
                                    <div id='field11-name' class="name-holder"></div>
                                    <div id="field11-price" class="price-tag text-right stick-left"></div>
                                </div>
                            </td>
                            <td class="vertical-premap-filler"></td>
                        </tr>
                        <tr>
                            <td class="vertical-premap-filler"></td>
                            <td>
                                <div id="field38" class="horizontal-field">
                                    <div id='field38-name' class="name-holder"></div>
                                    <div id="field38-price" class="price-tag text-left stick-right"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field12" class="horizontal-field">
                                    <div id='field12-name' class="name-holder"></div>
                                    <div id="field12-price" class="price-tag text-right stick-left"></div>
                                </div>
                            </td>
                            <td class="vertical-premap-filler"></td>
                        </tr>
                        <tr>
                            <td class="vertical-premap-filler"></td>
                            <td>
                                <div id="field37" class="horizontal-field">
                                    <div id='field37-name' class="name-holder"></div>
                                    <div id="field37-price" class="price-tag text-left stick-right"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field13" class="horizontal-field">
                                    <div id='field13-name' class="name-holder"></div>
                                    <div id="field13-price" class="price-tag text-right stick-left"></div>
                                </div>
                            </td>
                            <td class="vertical-premap-filler"></td>
                        </tr>
                        <tr>
                            <td class="vertical-premap-filler"></td>
                            <td>
                                <div id="field36" class="horizontal-field">
                                    <div id='field36-name' class="name-holder"></div>
                                    <div id="field36-price" class="price-tag text-left stick-right"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field14" class="horizontal-field">
                                    <div id='field14-name' class="name-holder"></div>
                                    <div id="field14-price" class="price-tag text-right stick-left"></div>
                                </div>
                            </td>
                            <td class="vertical-premap-filler"></td>
                        </tr>
                        <tr>
                            <td class="vertical-premap-filler"></td>
                            <td>
                                <div id="field35" class="horizontal-field">
                                    <div id='field35-name' class="name-holder"></div>
                                    <div id="field35-price" class="price-tag text-left stick-right"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field15" class="horizontal-field">
                                    <div id='field15-name' class="name-holder"></div>
                                    <div id="field15-price" class="price-tag text-right stick-left"></div>
                                </div>
                            </td>
                            <td class="vertical-premap-filler"></td>
                        </tr>
                        <tr>
                            <td class="vertical-premap-filler"></td>
                            <td>
                                <div id="field34" class="horizontal-field">
                                    <div id='field34-name' class="name-holder"></div>
                                    <div id="field34-price" class="price-tag text-left stick-right"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field16" class="horizontal-field">
                                    <div id='field16-name' class="name-holder"></div>
                                    <div id="field16-price" class="price-tag text-right stick-left"></div>
                                </div>
                            </td>
                            <td class="vertical-premap-filler"></td>
                        </tr>
                        <tr>
                            <td class="vertical-premap-filler"></td>
                            <td>
                                <div id="field33" class="horizontal-field">
                                    <div id='field33-name' class="name-holder"></div>
                                    <div id="field33-price" class="price-tag text-left stick-right"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field17" class="horizontal-field">
                                    <div id='field17-name' class="name-holder"></div>
                                    <div id="field17-price" class="price-tag text-right stick-left"></div>
                                </div>
                            </td>
                            <td class="vertical-premap-filler"></td>
                        </tr>
                        <tr>
                            <td class="vertical-premap-filler"></td>
                            <td>
                                <div id="field32" class="horizontal-field">
                                    <div id='field32-name' class="name-holder"></div>
                                    <div id="field32-price" class="price-tag text-left stick-right"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field18" class="horizontal-field">
                                    <div id='field18-name' class="name-holder"></div>
                                    <div id="field18-price" class="price-tag text-right stick-left"></div>
                                </div>
                            </td>
                            <td class="vertical-premap-filler"></td>
                        </tr>
                        <tr>
                            <td class="vertical-premap-filler"></td>
                            <td>
                                <div id="field31" class="horizontal-field">
                                    <div id='field31-name' class="name-holder"></div>
                                    <div id="field31-price" class="price-tag text-left stick-right"></div>
                                </div>
                            </td>
                            <td colspan="9">
                                <div id="player-message-container">
                                    <input id="player-message-input" autocomplete="false"
                                           placeholder="Enter a message..."/>
                                    <button id="player-message-button">send</button>
                                </div>
                            </td>
                            <td>
                                <div id="field19" class="horizontal-field">
                                    <div id='field19-name' class="name-holder"></div>
                                    <div id="field19-price" class="price-tag text-right stick-left"></div>
                                </div>
                            </td>
                            <td class="vertical-premap-filler"></td>
                        </tr>
                        <tr>
                            <td class="price-corner-vertical-field"></td>
                            <td id="field30" class="corner-field"></td>
                            <td>
                                <div id="field29" class="vertical-field">
                                    <div id='field29-name' class="name-holder"></div>
                                    <div id="field29-price" class="price-tag price-straight stick-top"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field28" class="vertical-field">
                                    <div id='field28-name' class="name-holder"></div>
                                    <div id="field28-price" class="price-tag price-straight stick-top"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field27" class="vertical-field">
                                    <div id='field27-name' class="name-holder"></div>
                                    <div id="field27-price" class="price-tag price-straight stick-top"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field26" class="vertical-field">
                                    <div id='field26-name' class="name-holder"></div>
                                    <div id="field26-price" class="price-tag price-straight stick-top"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field25" class="vertical-field">
                                    <div id='field25-name' class="name-holder"></div>
                                    <div id="field25-price" class="price-tag price-straight stick-top"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field24" class="vertical-field">
                                    <div id='field24-name' class="name-holder"></div>
                                    <div id="field24-price" class="price-tag price-straight stick-top"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field23" class="vertical-field">
                                    <div id='field23-name' class="name-holder"></div>
                                    <div id="field23-price" class="price-tag price-straight stick-top"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field22" class="vertical-field">
                                    <div id='field22-name' class="name-holder"></div>
                                    <div id="field22-price" class="price-tag price-straight stick-top"></div>
                                </div>
                            </td>
                            <td>
                                <div id="field21" class="vertical-field">
                                    <div id='field21-name' class="name-holder"></div>
                                    <div id="field21-price" class="price-tag price-straight stick-top"></div>
                                </div>
                            </td>
                            <td id="field20" class="corner-field"></td>
                            <td class="price-corner-vertical-field"></td>
                        </tr>
                        <tr>
                            <td class="price-corner-square-field"></td>
                            <td id="field30-price" class="price-corner-horizontal-field"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td class="horizontal-premap-filler"></td>
                            <td id="field20-price" class="price-corner-horizontal-field"></td>
                            <td class="price-corner-square-field"></td>
                        </tr>
                    </tbody>
                </table>
                <div id="chips">
                    <div id="chip0" class="chip-outer">
                        <div class="chip-inner"></div>
                    </div>
                    <div id="chip1" class="chip-outer">
                        <div class="chip-inner"></div>
                    </div>
                    <div id="chip2" class="chip-outer">
                        <div class="chip-inner"></div>
                    </div>
                    <div id="chip3" class="chip-outer">
                        <div class="chip-inner"></div>
                    </div>
                    <div id="chip4" class="chip-outer">
                        <div class="chip-inner"></div>
                    </div>
                </div>
            </td>
        </tr>
    </table>
    <div id="dice-container" class="dice-container, screen-centered">
        <img src="/images/dice-left.gif" alt="left dice" class="dice">
        <img src="/images/dice-right.gif" alt="right dice" class="dice">
    </div>
</div>
    `;
}
