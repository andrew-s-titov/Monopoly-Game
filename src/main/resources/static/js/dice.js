import * as Utils from './utils.js';

const DICE_CONTAINER_ID = 'dice_container';
const DICE_LEFT_ID = 'dice_left';
const DICE_RIGHT_ID = 'dice_right';
const LEFT_DICE_GIF_SRC = 'images/dice-left.gif';
const RIGHT_DICE_GIF_SRC = 'images/dice-right.gif';
const LEFT_DICE_ALT_TAG = 'left dice';
const RIGHT_DICE_ALT_TAG = 'right dice';

let _DICE_CONTAINER = null;
let _LEFT_DICE = null;
let _RIGHT_DICE = null;

export function renderRollingDice() {
    renderDiceView(LEFT_DICE_GIF_SRC, RIGHT_DICE_GIF_SRC);
}

export function renderDiceResult(left, right) {
    const leftImageSrc = `images/dice${left}.png`;
    const rightImageSrc = `images/dice${right}.png`;
    renderDiceView(leftImageSrc, rightImageSrc);
}

export function hideDice() {
    getDiceContainer().style.display = 'none';
}

export async function preloadDice() {
    new Image().src = LEFT_DICE_GIF_SRC;
    new Image().src = RIGHT_DICE_GIF_SRC;
    for (let i = 1; i <= 6; i++) {
        new Image().src = `images/dice${i}.png`;
    }
}

function renderDiceView(leftDiceImageSrc, rightDiceImageSrc) {
    const diceContainer = getDiceContainer();
    _LEFT_DICE.src = leftDiceImageSrc;
    _RIGHT_DICE.src = rightDiceImageSrc;
    diceContainer.style.display = 'block';
}

function getDiceContainer() {
    if (_DICE_CONTAINER === null) {
        _DICE_CONTAINER = document.createElement('div');
        _DICE_CONTAINER.id = DICE_CONTAINER_ID;
        _DICE_CONTAINER.className = 'dice-container';
        document.getElementById('message-container').appendChild(_DICE_CONTAINER);

        _LEFT_DICE = Utils.createImage(LEFT_DICE_GIF_SRC, LEFT_DICE_ALT_TAG);
        _LEFT_DICE.id = DICE_LEFT_ID;
        _LEFT_DICE.style.float = 'left';
        _RIGHT_DICE = Utils.createImage(RIGHT_DICE_GIF_SRC, RIGHT_DICE_ALT_TAG);
        _RIGHT_DICE.id = DICE_RIGHT_ID;
        _RIGHT_DICE.style.float = 'right';

        _DICE_CONTAINER.append(_LEFT_DICE, _RIGHT_DICE);
    }
    return _DICE_CONTAINER;
}