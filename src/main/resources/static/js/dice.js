const LEFT_DICE_GIF_SRC = '/images/dice-left.gif';
const RIGHT_DICE_GIF_SRC = '/images/dice-right.gif';

let _DICE_CONTAINER = null;
let _LEFT_DICE = null;
let _RIGHT_DICE = null;

export function showRollingDice() {
    showDiceContainer();
    changeDiceView(LEFT_DICE_GIF_SRC, RIGHT_DICE_GIF_SRC);
}

export function showDiceResult(left, right) {
    showDiceContainer();
    const leftImageSrc = `images/dice${left}.png`;
    const rightImageSrc = `images/dice${right}.png`;
    changeDiceView(leftImageSrc, rightImageSrc);
}

export function hideDice() {
    getDiceContainer().style.display = 'none';
}

export async function preloadDiceImages() {
    new Image().src = LEFT_DICE_GIF_SRC;
    new Image().src = RIGHT_DICE_GIF_SRC;
    for (let i = 1; i <= 6; i++) {
        new Image().src = `images/dice${i}.png`;
    }
}

export async function initialiseDiceAsync() {
    if (_DICE_CONTAINER === null) {
        initialiseDice();
    }
}

function showDiceContainer() {
    getDiceContainer().style.display = 'flex';
}

function changeDiceView(leftDiceImageSrc, rightDiceImageSrc) {
    getLeftDice().src = leftDiceImageSrc;
    getRightDice().src = rightDiceImageSrc;
}

function getDiceContainer() {
    if (_DICE_CONTAINER === null) {
        initialiseDice();
    }
    return _DICE_CONTAINER;
}

function getLeftDice() {
    if (_LEFT_DICE === null) {
        initialiseDice();
    }
    return _LEFT_DICE;
}

function getRightDice() {
    if (_RIGHT_DICE === null) {
        initialiseDice();
    }
    return _RIGHT_DICE;
}

function initialiseDice() {
   _DICE_CONTAINER = document.getElementById('dice-container');
   _LEFT_DICE = _DICE_CONTAINER.firstElementChild;
   _RIGHT_DICE = _DICE_CONTAINER.lastElementChild;
}