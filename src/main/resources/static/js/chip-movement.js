const priceNarrowSidePx = 14;
const fieldWideSidePx = 81;
const fieldNarrowSidePx = 52;
const stepPx = fieldNarrowSidePx;
const cornerStepAdjustmentPx = (fieldWideSidePx - fieldNarrowSidePx) / 2;
const chipWidth = 24;
const chipWidthAdjustment = chipWidth / 2;
const PX_POSTFIX = 'px';

export function moveChip(chip, fieldIndex) {
    if (fieldIndex < 0 || fieldIndex > 39) {
        console.error(`no field with id ${fieldIndex} found on map`);
        return;
    }
    chip.style.top = defineChipTop(fieldIndex);
    chip.style.left = defineChipLeft(fieldIndex);
}

// returning string for 'style.top'
function defineChipTop(fieldIndex) {
    let top = priceNarrowSidePx + fieldWideSidePx / 2 - chipWidthAdjustment; // adding body default margin;
    if (fieldIndex >= 20 && fieldIndex <= 30) {
        top += stepPx * 10 + cornerStepAdjustmentPx * 2;
    } else if (fieldIndex > 10 && fieldIndex < 20) {
        top += cornerStepAdjustmentPx + stepPx * (fieldIndex - 10);
    } else if (fieldIndex > 30 && fieldIndex < 40) {
        top += cornerStepAdjustmentPx + stepPx * (40 - fieldIndex);
    }
    return top + PX_POSTFIX;
}

// returning string for 'style.left'
function defineChipLeft(fieldIndex) {
    let left = priceNarrowSidePx + fieldWideSidePx / 2 - chipWidthAdjustment;
    if (fieldIndex >= 10 && fieldIndex <= 20) {
        left += stepPx * 10 + cornerStepAdjustmentPx * 2;
    } else if (fieldIndex > 0 && fieldIndex < 10) {
        left += stepPx * fieldIndex + cornerStepAdjustmentPx;
    } else if (fieldIndex > 20 && fieldIndex < 30) {
        left += stepPx * (30 - fieldIndex) + cornerStepAdjustmentPx;
    }
    return left + PX_POSTFIX;
}