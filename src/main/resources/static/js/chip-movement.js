const priceSpacePx = 13;
const wideSidePx = 90;
const narrowSidePx = 50;
const stepPx = 50;
const mapLeftMarginPx = 300;
const cornerStepAdjustmentPx = (wideSidePx - narrowSidePx) / 2;
let playerIconWidthPx = 150;

export function moveChip(chip, fieldIndex) {
    chip.style.top = defineChipTop(fieldIndex);
    chip.style.left = defineChipLeft(fieldIndex);
}

// returning string for 'style.top'
function defineChipTop(fieldIndex) {
    let startTop = priceSpacePx + wideSidePx / 2 + 8; // adding body default margin;
    let postfix = 'px';
    if (fieldIndex >= 0 && fieldIndex <= 10) {
        return startTop + postfix;
    } else if (fieldIndex >= 20 && fieldIndex <= 30) {
        return (startTop
                + stepPx * 10
                + cornerStepAdjustmentPx * 2)
            + postfix;
    } else if (fieldIndex > 10 && fieldIndex < 20) {
        return (startTop
                + cornerStepAdjustmentPx
                + stepPx * (fieldIndex - 10))
            + postfix;
    } else if (fieldIndex > 30 && fieldIndex < 40) {
        return (startTop
                + cornerStepAdjustmentPx
                + stepPx * (40 - fieldIndex))
            + postfix;
    } else {
        console.error(`no field with id ${fieldIndex} found on map`);
    }
}

// returning string for 'style.left'
function defineChipLeft(fieldIndex) {
    let startLeft = 8 + mapLeftMarginPx + playerIconWidthPx + priceSpacePx + wideSidePx / 2;
    let postfix = 'px';
    if (fieldIndex === 0 || (fieldIndex >= 30 && fieldIndex < 40)) {
        return startLeft + postfix;
    } else if (fieldIndex >= 10 && fieldIndex <= 20) {
        return (startLeft
                + stepPx * 10
                + cornerStepAdjustmentPx * 2)
            + postfix;
    } else if (fieldIndex > 0 && fieldIndex < 10) {
        return (startLeft
                + stepPx * fieldIndex
                + cornerStepAdjustmentPx)
            + postfix;
    } else if (fieldIndex > 20 && fieldIndex < 30) {
        return (startLeft
                + stepPx * (30 - fieldIndex)
                + cornerStepAdjustmentPx)
            + postfix;
    } else {
        console.error(`no field with id ${fieldIndex} found on map`);
    }
}