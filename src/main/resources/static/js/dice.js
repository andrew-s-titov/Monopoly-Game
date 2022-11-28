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
    let leftImage = `images/dice${left}.png`;
    let rightImage = `images/dice${right}.png`;
    if (!document.getElementById(DICE_CONTAINER_ID)) {
        renderDiceView(leftImage, rightImage);
    } else {
        document.getElementById(DICE_LEFT_ID).src = leftImage;
        document.getElementById(DICE_RIGHT_ID).src = rightImage;
    }
}

function renderDiceView(leftDiceImage, rightDiceImage) {
    let diceContainer = document.createElement('div');
    diceContainer.id = DICE_CONTAINER_ID;
    diceContainer.style.position = 'fixed';
    diceContainer.style.left = '47%';
    diceContainer.style.top = '45%';
    document.body.appendChild(diceContainer);

    let diceLeft = document.createElement('img');
    diceLeft.id = DICE_LEFT_ID;
    diceLeft.style.float = 'left';

    let diceRight = document.createElement('img');
    diceRight.id = DICE_RIGHT_ID;
    diceRight.style.float = 'left';

    diceContainer.appendChild(diceLeft);
    diceContainer.appendChild(diceRight);

    diceLeft.src = leftDiceImage;
    diceRight.src = rightDiceImage;
}