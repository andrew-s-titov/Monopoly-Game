let _PRICE_NARROW_SIDE = 14;
let _FIELD_WIDE_SIDE = 81;
let _FIELD_NARROW_SIDE = 52;
let _STEP_PX = _FIELD_NARROW_SIDE;
let _CORNER_STEP_ADJUSTMENT = 14.5;
let _CHIP_WIDTH = 24;
let _CHIP_WIDTH_ADJUSTMENT = 12;
const PX_POSTFIX = 'px';

let paramsSet = false;

export async function initialiseChipParamsAsync() {
    initialiseChipParams();
}

function initialiseChipParams() {
    const style = getComputedStyle(document.body);
    _PRICE_NARROW_SIDE = getStylePropertyNumber(style, '--price-narrow-side');
    _FIELD_WIDE_SIDE = getStylePropertyNumber(style, '--field-wide-side');
    _FIELD_NARROW_SIDE = getStylePropertyNumber(style, '--field-narrow-side');
    _STEP_PX = _FIELD_NARROW_SIDE;
    _CORNER_STEP_ADJUSTMENT = (_FIELD_WIDE_SIDE - _FIELD_NARROW_SIDE) / 2;
    _CHIP_WIDTH = getStylePropertyNumber(style, '--chip-width');
    _CHIP_WIDTH_ADJUSTMENT = _CHIP_WIDTH / 2;
    paramsSet = true;
}

export function moveChip(chip, fieldIndex) {
    if (fieldIndex < 0 || fieldIndex > 39) {
        console.error(`no field with id ${fieldIndex} found on map`);
        return;
    }
    chip.style.top = defineChipTop(fieldIndex);
    chip.style.left = defineChipLeft(fieldIndex);
}

export function moveToStart(chip) {
    chip.style.top = getStartTop() + PX_POSTFIX;
    chip.style.left = getStartLeft() + PX_POSTFIX;
}

// returning string for 'style.top'
function defineChipTop(fieldIndex) {
    let top = getStartTop();
    if (fieldIndex >= 20 && fieldIndex <= 30) {
        top += _STEP_PX * 10 + _CORNER_STEP_ADJUSTMENT * 2;
    } else if (fieldIndex > 10 && fieldIndex < 20) {
        top += _CORNER_STEP_ADJUSTMENT + _STEP_PX * (fieldIndex - 10);
    } else if (fieldIndex > 30 && fieldIndex < 40) {
        top += _CORNER_STEP_ADJUSTMENT + _STEP_PX * (40 - fieldIndex);
    }
    return top + PX_POSTFIX;
}

// returning string for 'style.left'
function defineChipLeft(fieldIndex) {
    let left = getStartLeft();
    if (fieldIndex >= 10 && fieldIndex <= 20) {
        left += _STEP_PX * 10 + _CORNER_STEP_ADJUSTMENT * 2;
    } else if (fieldIndex > 0 && fieldIndex < 10) {
        left += _STEP_PX * fieldIndex + _CORNER_STEP_ADJUSTMENT;
    } else if (fieldIndex > 20 && fieldIndex < 30) {
        left += _STEP_PX * (30 - fieldIndex) + _CORNER_STEP_ADJUSTMENT;
    }
    return left + PX_POSTFIX;
}

function getStylePropertyNumber(computedStyle, propertyName) {
    return Number.parseFloat(computedStyle.getPropertyValue(propertyName).trim().replace(PX_POSTFIX, ''));
}

function getStartTop() {
    return _PRICE_NARROW_SIDE + _FIELD_WIDE_SIDE / 2 - _CHIP_WIDTH_ADJUSTMENT; // adding body default margin;
}

function getStartLeft() {
    return _PRICE_NARROW_SIDE + _FIELD_WIDE_SIDE / 2 - _CHIP_WIDTH_ADJUSTMENT;
}