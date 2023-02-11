import * as Utils from './utils.js';

const DICE_CONTAINER_ID = 'dice_container';
const DICE_LEFT_ID = 'dice_left';
const DICE_RIGHT_ID = 'dice_right';
const LEFT_DICE_GIF_SRC = 'images/dice-left.gif';
const RIGHT_DICE_GIF_SRC = 'images/dice-right.gif';
const LEFT_DICE_ALT_TAG = 'left dice';
const RIGHT_DICE_ALT_TAG = 'right dice';

export function renderDiceGifs() {
    renderDiceView(LEFT_DICE_GIF_SRC, RIGHT_DICE_GIF_SRC);
}

export function hideDice() {
    Utils.removeElementsIfPresent(DICE_CONTAINER_ID);
}

export function renderDiceResult(left, right) {
    const leftImage = `images/dice${left}.png`;
    const rightImage = `images/dice${right}.png`;
    if (!document.getElementById(DICE_CONTAINER_ID)) {
        renderDiceView(leftImage, rightImage);
    } else {
        document.getElementById(DICE_LEFT_ID).src = leftImage;
        document.getElementById(DICE_RIGHT_ID).src = rightImage;
    }
}

export async function preloadDice() {
    new Image().src = LEFT_DICE_GIF_SRC;
    new Image().src = RIGHT_DICE_GIF_SRC;
    for (let i = 1; i <= 6; i++) {
        new Image().src = `images/dice${i}.png`;
    }
}

function renderDiceView(leftDiceImage, rightDiceImage) {
    const diceContainer = document.createElement('div');
    diceContainer.id = DICE_CONTAINER_ID;
    diceContainer.className = 'dice-container';
    document.getElementById('message-container').appendChild(diceContainer);

    const diceLeft = Utils.createImage(leftDiceImage, LEFT_DICE_ALT_TAG);
    diceLeft.id = DICE_LEFT_ID;
    diceLeft.style.float = 'left';

    const diceRight = Utils.createImage(rightDiceImage, RIGHT_DICE_ALT_TAG);
    diceRight.id = DICE_RIGHT_ID;
    diceRight.style.float = 'right';

    diceContainer.appendChild(diceLeft);
    diceContainer.appendChild(diceRight);
}