const DICE_CONTAINER_ID = 'dice_container';
const DICE_LEFT_ID = 'dice_left';
const DICE_RIGHT_ID = 'dice_right';

export function renderDiceGifs() {
    renderDiceView('images/dice-left.gif', 'images/dice-right.gif');
}

export function hideDice() {
    document.getElementById(DICE_CONTAINER_ID).remove();
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

export function preloadDice() {
    const diceLeftGif = document.createElement('img');
    diceLeftGif.src = 'images/dice-left.gif';
    const diceRightGif = document.createElement('img');
    diceRightGif.src = 'images/dice-right.gif';
    for (let i = 1; i <= 6; i++) {
        const dice = document.createElement('img');
        dice.src = `images/dice${i}.png`;
    }
}

function renderDiceView(leftDiceImage, rightDiceImage) {
    const diceContainer = document.createElement('div');
    diceContainer.id = DICE_CONTAINER_ID;
    diceContainer.className = 'dice-container';
    document.getElementById('message-container').appendChild(diceContainer);

    const diceLeft = document.createElement('img');
    diceLeft.id = DICE_LEFT_ID;
    diceLeft.style.float = 'left';

    const diceRight = document.createElement('img');
    diceRight.id = DICE_RIGHT_ID;
    diceRight.style.float = 'right';

    diceContainer.appendChild(diceLeft);
    diceContainer.appendChild(diceRight);

    diceLeft.src = leftDiceImage;
    diceRight.src = rightDiceImage;
}