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
<div class="map-parts">
    <div class="gm-players-container">
        <table style="border-collapse: collapse">
            <tbody>
                <tr>
                    <td>
                        <div id="player0-info-container" class="player-info-container">
                            <div id="player0-icon"></div>
                            <div id="player0-name" class="gm-player-name"></div>
                            <div id="player0-money" class="player-money"></div>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <div id="player1-info-container" class="player-info-container">
                            <div id="player1-icon"></div>
                            <div id="player1-name" class="gm-player-name"></div>
                            <div id="player1-money" class="player-money"></div>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <div id="player2-info-container" class="player-info-container">
                            <div id="player2-icon"></div>
                            <div id="player2-name" class="gm-player-name"></div>
                            <div id="player2-money" class="player-money"></div>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <div id="player3-info-container" class="player-info-container">
                            <div id="player3-icon"></div>
                            <div id="player3-name" class="gm-player-name"></div>
                            <div id="player3-money" class="player-money"></div>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <div id="player4-info-container" class="player-info-container">
                            <div id="player4-icon"></div>
                            <div id="player4-name" class="gm-player-name"></div>
                            <div id="player4-money" class="player-money"></div>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
    
    <div class="map-container">
        <div class="map">
            <div class="gm-map-row">
                <div id="field0" class="gm-field gm-corner"></div>
                <div class="vertical-gap"></div>
                <div id="field1" class="gm-field vertical-field">
                    <div id="field1-mortgage" class="mortgage-tag stick-top"></div>
                    <div id="field1-houses" class="house-container stick-top stick-right"></div>
                    <div id="field1-cover" class="vertical-cover stick-top"></div>
                    <div id="field1-price" class="price-tag price-straight stick-bottom"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field2" class="gm-field vertical-field"></div>
                <div class="vertical-gap"></div>
                <div id="field3" class="gm-field vertical-field">
                    <div id="field3-mortgage" class="mortgage-tag stick-top"></div>
                    <div id="field3-houses" class="house-container stick-top stick-right"></div>
                    <div id="field3-cover" class="vertical-cover stick-top"></div>
                    <div id="field3-price" class="price-tag price-straight stick-bottom"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field4" class="gm-field vertical-field"></div>
                <div class="vertical-gap"></div>
                <div id="field5" class="gm-field vertical-field">
                    <div id="field5-mortgage" class="mortgage-tag stick-top"></div>
                    <div id="field5-houses" class="house-container stick-top stick-right"></div>
                    <div id="field5-cover" class="vertical-cover stick-top"></div>
                    <div id="field5-price" class="price-tag price-straight stick-bottom"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field6" class="gm-field vertical-field">
                    <div id="field6-mortgage" class="mortgage-tag stick-top"></div>
                    <div id="field6-houses" class="house-container stick-top stick-right"></div>
                    <div id="field6-cover" class="vertical-cover stick-top"></div>
                    <div id="field6-price" class="price-tag price-straight stick-bottom"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field7" class="gm-field vertical-field"></div>
                <div class="vertical-gap"></div>
                <div id="field8" class="gm-field vertical-field">
                    <div id="field8-mortgage" class="mortgage-tag stick-top"></div>
                    <div id="field8-houses" class="house-container stick-top stick-right"></div>
                    <div id="field8-cover" class="vertical-cover stick-top"></div>
                    <div id="field8-price" class="price-tag price-straight stick-bottom"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field9" class="gm-field vertical-field">
                    <div id="field9-mortgage" class="mortgage-tag stick-top"></div>
                    <div id="field9-houses" class="house-container stick-top stick-right"></div>
                    <div id="field9-cover" class="vertical-cover stick-top"></div>
                    <div id="field9-price" class="price-tag price-straight stick-bottom"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field10" class="gm-field gm-corner"></div>
            </div>
            <div class="horizontal-gap"></div>
            <div class="gm-center-row">
                <div class="gm-column">
                    <div id="field39" class="gm-field horizontal-field">
                        <div id="field39-mortgage" class="mortgage-tag stick-left"></div>
                        <div id="field39-houses" class="house-container stick-bottom stick-left"></div>
                        <div id="field39-cover" class="horizontal-cover stick-left"></div>
                        <div id="field39-price" class="price-tag price-side stick-right"></div>
                    </div>
                    <div class="horizontal-gap"></div>
                    <div id="field38" class="gm-field horizontal-field"></div>
                    <div class="horizontal-gap"></div>
                    <div id="field37" class="gm-field horizontal-field">
                        <div id="field37-mortgage" class="mortgage-tag stick-left"></div>
                        <div id="field37-houses" class="house-container stick-bottom stick-left"></div>
                        <div id="field37-cover" class="horizontal-cover stick-left"></div>
                        <div id="field37-price" class="price-tag price-side stick-right"></div>
                    </div>
                    <div class="horizontal-gap"></div>
                    <div id="field36" class="gm-field horizontal-field"></div>
                    <div class="horizontal-gap"></div>
                    <div id="field35" class="gm-field horizontal-field">
                        <div id="field35-mortgage" class="mortgage-tag stick-left"></div>
                        <div id="field35-houses" class="house-container stick-bottom stick-left"></div>
                        <div id="field35-cover" class="horizontal-cover stick-left"></div>
                        <div id="field35-price" class="price-tag price-side stick-right"></div>
                    </div>
                    <div class="horizontal-gap"></div>
                    <div id="field34" class="gm-field horizontal-field">
                        <div id="field34-mortgage" class="mortgage-tag stick-left"></div>
                        <div id="field34-houses" class="house-container stick-bottom stick-left"></div>
                        <div id="field34-cover" class="horizontal-cover stick-left"></div>
                        <div id="field34-price" class="price-tag price-side stick-right"></div>
                    </div>
                    <div class="horizontal-gap"></div>
                    <div id="field33" class="gm-field horizontal-field"></div>
                    <div class="horizontal-gap"></div>
                    <div id="field32" class="gm-field horizontal-field">
                        <div id="field32-mortgage" class="mortgage-tag stick-left"></div>
                        <div id="field32-houses" class="house-container stick-bottom stick-left"></div>
                        <div id="field32-cover" class="horizontal-cover stick-left"></div>
                        <div id="field32-price" class="price-tag price-side stick-right"></div>
                    </div>
                    <div class="horizontal-gap"></div>
                    <div id="field31" class="gm-field horizontal-field">
                        <div id="field31-mortgage" class="mortgage-tag stick-left"></div>
                        <div id="field31-houses" class="house-container stick-bottom stick-left"></div>
                        <div id="field31-cover" class="horizontal-cover stick-left"></div>
                        <div id="field31-price" class="price-tag price-side stick-right"></div>
                    </div>
                </div>
                <div class="vertical-gap"></div>
                <div class="gm-map-center">
                    <div id="offer-info-box" class="offer-info-box"></div>
                    <div id="proposal-info-box" class="offer-info-box"></div>
                    <div id="chat-message-container" class="chat-container">
                        <div class="chat-height-filler"></div>
                    </div>
                    <div id="player-message-container" class="chat-input-container">
                        <input id="player-message-input" class="player-chat-input"
                                autocomplete="false" placeholder="Enter a message..."/>
                        <button id="player-message-button" class="player-chat-send">send</button>
                    </div>
                </div>
                <div class="vertical-gap"></div>
                <div class="gm-column">
                    <div id="field11" class="gm-field horizontal-field">
                        <div id="field11-mortgage" class="mortgage-tag stick-right"></div>
                        <div id="field11-houses" class="house-container stick-top stick-right"></div>
                        <div id="field11-cover" class="horizontal-cover stick-right"></div>
                        <div id="field11-price" class="price-tag price-side right-text stick-left"></div>
                    </div>
                    <div class="horizontal-gap"></div>
                    <div id="field12" class="gm-field horizontal-field">
                        <div id="field12-mortgage" class="mortgage-tag stick-right"></div>
                        <div id="field12-houses" class="house-container stick-top stick-right"></div>
                        <div id="field12-cover" class="horizontal-cover stick-right"></div>
                        <div id="field12-price" class="price-tag price-side right-text stick-left"></div>
                    </div>
                    <div class="horizontal-gap"></div>
                    <div id="field13" class="gm-field horizontal-field">
                        <div id="field13-mortgage" class="mortgage-tag stick-right"></div>
                        <div id="field13-houses" class="house-container stick-top stick-right"></div>
                        <div id="field13-cover" class="horizontal-cover stick-right"></div>
                        <div id="field13-price" class="price-tag price-side right-text stick-left"></div>
                    </div>
                    <div class="horizontal-gap"></div>
                    <div id="field14" class="gm-field horizontal-field">
                        <div id="field14-mortgage" class="mortgage-tag stick-right"></div>
                        <div id="field14-houses" class="house-container stick-top stick-right"></div>
                        <div id="field14-cover" class="horizontal-cover stick-right"></div>
                        <div id="field14-price" class="price-tag price-side right-text stick-left"></div>
                    </div>
                    <div class="horizontal-gap"></div>
                    <div id="field15" class="gm-field horizontal-field">
                        <div id="field15-mortgage" class="mortgage-tag stick-right"></div>
                        <div id="field15-houses" class="house-container stick-top stick-right"></div>
                        <div id="field15-cover" class="horizontal-cover stick-right"></div>
                        <div id="field15-price" class="price-tag price-side right-text stick-left"></div>
                    </div>
                    <div class="horizontal-gap"></div>
                    <div id="field16" class="gm-field horizontal-field">
                        <div id="field16-mortgage" class="mortgage-tag stick-right"></div>
                        <div id="field16-houses" class="house-container stick-top stick-right"></div>
                        <div id="field16-cover" class="horizontal-cover stick-right"></div>
                        <div id="field16-price" class="price-tag price-side right-text stick-left"></div>
                    </div>
                    <div class="horizontal-gap"></div>
                    <div id="field17" class="gm-field horizontal-field"></div>
                    <div class="horizontal-gap"></div>
                    <div id="field18" class="gm-field horizontal-field">
                        <div id="field18-mortgage" class="mortgage-tag stick-right"></div>
                        <div id="field18-houses" class="house-container stick-top stick-right"></div>
                        <div id="field18-cover" class="horizontal-cover stick-right"></div>
                        <div id="field18-price" class="price-tag price-side right-text stick-left"></div>
                    </div>
                    <div class="horizontal-gap"></div>
                    <div id="field19" class="gm-field horizontal-field">
                        <div id="field19-mortgage" class="mortgage-tag stick-right"></div>
                        <div id="field19-houses" class="house-container stick-top stick-right"></div>
                        <div id="field19-cover" class="horizontal-cover stick-right"></div>
                        <div id="field19-price" class="price-tag price-side right-text stick-left"></div>
                    </div>
                </div>
            </div>
            <div class="horizontal-gap"></div>
            <div class="gm-map-row">
                <div id="field30"  class="gm-field gm-corner"></div>
                <div class="vertical-gap"></div>
                <div id="field29" class="gm-field vertical-field">
                    <div id="field29-mortgage" class="mortgage-tag stick-bottom"></div>
                    <div id="field29-houses" class="house-container stick-bottom stick-left"></div>
                    <div id="field29-cover" class="vertical-cover stick-bottom"></div>
                    <div id="field29-price" class="price-tag price-straight stick-top"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field28" class="gm-field vertical-field">
                    <div id="field28-mortgage" class="mortgage-tag stick-bottom"></div>
                    <div id="field28-houses" class="house-container stick-bottom stick-left"></div>
                    <div id="field28-cover" class="vertical-cover stick-bottom"></div>
                    <div id="field28-price" class="price-tag price-straight stick-top"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field27" class="gm-field vertical-field">
                    <div id="field27-mortgage" class="mortgage-tag stick-bottom"></div>
                    <div id="field27-houses" class="house-container stick-bottom stick-left"></div>
                    <div id="field27-cover" class="vertical-cover stick-bottom"></div>
                    <div id="field27-price" class="price-tag price-straight stick-top"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field26" class="gm-field vertical-field">
                    <div id="field26-mortgage" class="mortgage-tag stick-bottom"></div>
                    <div id="field26-houses" class="house-container stick-bottom stick-left"></div>
                    <div id="field26-cover" class="vertical-cover stick-bottom"></div>
                    <div id="field26-price" class="price-tag price-straight stick-top"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field25" class="gm-field vertical-field">
                    <div id="field25-mortgage" class="mortgage-tag stick-bottom"></div>
                    <div id="field25-houses" class="house-container stick-bottom stick-left"></div>
                    <div id="field25-cover" class="vertical-cover stick-bottom"></div>
                    <div id="field25-price" class="price-tag price-straight stick-top"></div>
                </div>
                  <div class="vertical-gap"></div>
                <div id="field24" class="gm-field vertical-field">
                    <div id="field24-mortgage" class="mortgage-tag stick-bottom"></div>
                    <div id="field24-houses" class="house-container stick-bottom stick-left"></div>
                    <div id="field24-cover" class="vertical-cover stick-bottom"></div>
                    <div id="field24-price" class="price-tag price-straight stick-top"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field23" class="gm-field vertical-field">
                    <div id="field23-mortgage" class="mortgage-tag stick-bottom"></div>
                    <div id="field23-houses" class="house-container stick-bottom stick-left"></div>
                    <div id="field23-cover" class="vertical-cover stick-bottom"></div>
                    <div id="field23-price" class="price-tag price-straight stick-top"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field22" class="gm-field vertical-field"></div>
                <div class="vertical-gap"></div>
                <div id="field21" class="gm-field vertical-field">
                    <div id="field21-mortgage" class="mortgage-tag stick-bottom"></div>
                    <div id="field21-houses" class="house-container stick-bottom stick-left"></div>
                    <div id="field21-cover" class="vertical-cover stick-bottom"></div>
                    <div id="field21-price" class="price-tag price-straight stick-top"></div>
                </div>
                <div class="vertical-gap"></div>
                <div id="field20" class="gm-field gm-corner"></div>
            </div>
            <div id="chips" class="chips-container">
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
        </div>
    </div>

    <div id="dice-container" class="dice-container screen-centered">
        <img src="/images/dice-left.gif" alt="left dice" class="dice">
        <img src="/images/dice-right.gif" alt="right dice" class="dice">
    </div>
</div>
    `;
}
