import {
    PLAYER_COLORS,
    GROUP_COLORS
} from "./colors.js"

const priceSpacePx = 13;
const wideSidePx = 90;
const narrowSidePx = 50;
const stepPx = 50;
const mapLeftMarginPx = 300;
const cornerStepAdjustmentPx = (wideSidePx - narrowSidePx) / 2;
let playerIconWidthPx = 150;

let chips = new Map();

export function defineChipPosition(playerId, fieldNumber) {
    let chip = chips.get(playerId);
    chip.style.top = defineChipTop(fieldNumber);
    chip.style.left = defineChipLeft(fieldNumber);
}

export function renderChip(index, playerId) {
    let chip = document.createElement('div');
    chip.id = 'chip' + index;
    chip.className = 'chip';
    chip.style.background = PLAYER_COLORS[index];
    chip.style.opacity = '0.8'
    chips.set(playerId, chip);
    document.getElementById('map').appendChild(chip);
}

// returning string for 'style.top'
function defineChipTop(fieldNumber) {
    let startTop = priceSpacePx + wideSidePx / 2 + 8; // adding body default margin;
    let postfix = 'px';
    if (fieldNumber >= 0 && fieldNumber <= 10) {
        return startTop + postfix;
    } else if (fieldNumber >= 20 && fieldNumber <= 30) {
        return (startTop
                + stepPx * 10
                + cornerStepAdjustmentPx * 2)
            + postfix;
    } else if (fieldNumber > 10 && fieldNumber < 20) {
        return (startTop
                + cornerStepAdjustmentPx
                + stepPx * (fieldNumber - 10))
            + postfix;
    } else if (fieldNumber > 30 && fieldNumber < 40) {
        return (startTop
                + cornerStepAdjustmentPx
                + stepPx * (40 - fieldNumber))
            + postfix;
    } else {
        console.error('field number exceeds map size');
    }
}

// returning string for 'style.left'
function defineChipLeft(fieldNumber) {
    let startLeft = 8 + mapLeftMarginPx + playerIconWidthPx + priceSpacePx + wideSidePx / 2;
    let postfix = 'px';
    if (fieldNumber === 0 || (fieldNumber >= 30 && fieldNumber < 40)) {
        return startLeft + postfix;
    } else if (fieldNumber >= 10 && fieldNumber <= 20) {
        return (startLeft
                + stepPx * 10
                + cornerStepAdjustmentPx * 2)
            + postfix;
    } else if (fieldNumber > 0 && fieldNumber < 10) {
        return (startLeft
                + stepPx * fieldNumber
                + cornerStepAdjustmentPx)
            + postfix;
    } else if (fieldNumber > 20 && fieldNumber < 30) {
        return (startLeft
                + stepPx * (30 - fieldNumber)
                + cornerStepAdjustmentPx)
            + postfix;
    } else {
        console.error('field number exceeds map size');
    }
}