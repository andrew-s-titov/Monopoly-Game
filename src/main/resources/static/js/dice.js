const DICE_CONTAINER_ID = 'dice_container';
const DICE_LEFT_ID = 'dice_left';
const DICE_RIGHT_ID = 'dice_right';

export function renderDiceGifs() {
    let diceContainer = document.createElement('div');
    diceContainer.id = DICE_CONTAINER_ID;
    diceContainer.style.position = 'fixed';
    diceContainer.style.left = '47%';
    diceContainer.style.top = '50%';

    let diceLeft = document.createElement('img');
    diceLeft.id = DICE_LEFT_ID;
    diceLeft.src = 'images/dice-left.gif';
    diceLeft.style.float = 'left';

    let diceRight = document.createElement('img');
    diceRight.id = DICE_RIGHT_ID;
    diceRight.src = 'images/dice-right.gif';
    diceRight.style.float = 'left';

    diceContainer.appendChild(diceLeft);
    diceContainer.appendChild(diceRight);

    document.getElementById('map').appendChild(diceContainer);
}

export function hideDice() {
    document.getElementById(DICE_CONTAINER_ID).remove();
}

export function renderDiceResult(left, right) {
    document.getElementById(DICE_LEFT_ID).src = `images/dice${left}.png`;
    document.getElementById(DICE_RIGHT_ID).src = `images/dice${right}.png`;
}