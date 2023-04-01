let _WINDOW_SIZE = 0;
let _FIELD_WIDE_SIDE = 0;
let _FIELD_NARROW_SIDE = 0;
let _STEP_PX = 0;
let _CORNER_STEP_ADJUSTMENT = 0;
let _CHIP_WIDTH_ADJUSTMENT = 0;
let _START_POSITION = 0;
const _POSTFIX = 'vh';

let paramsSet = false;

export async function initialiseChipParamsAsync() {
    initialiseChipParams();
}

function initialiseChipParams() {
    const style = getComputedStyle(document.body);
    _WINDOW_SIZE = getNumberProperty(style, '--mount')
    _FIELD_WIDE_SIDE = getNumberProperty(style, '--wide');
    _FIELD_NARROW_SIDE = getNumberProperty(style, '--narrow');
    _STEP_PX = _FIELD_NARROW_SIDE + getNumberProperty(style, '--gap');
    _CORNER_STEP_ADJUSTMENT = (_FIELD_WIDE_SIDE - _FIELD_NARROW_SIDE) / 2;
    _CHIP_WIDTH_ADJUSTMENT = getNumberProperty(style, '--player-chip') / 2;
    _START_POSITION = _FIELD_WIDE_SIDE / 2 - _CHIP_WIDTH_ADJUSTMENT;
    paramsSet = true;
}

export function moveChip(chip, fieldIndex) {
    if (fieldIndex < 0 || fieldIndex > 39) {
        console.error(`no field with id ${fieldIndex} found on map`);
        return;
    }
    if (!paramsSet) {
        initialiseChipParams();
    }
    chip.style.top = defineChipTop(fieldIndex);
    chip.style.left = defineChipLeft(fieldIndex);
}

export function moveToStart(chip) {
    if (!paramsSet) {
        initialiseChipParams();
    }
    chip.style.top = calculateSide(_START_POSITION);
    chip.style.left = calculateSide(_START_POSITION);
}

// returning string for 'style.top'
function defineChipTop(fieldIndex) {
    let top = _START_POSITION;
    if (fieldIndex >= 20 && fieldIndex <= 30) {
        top += _STEP_PX * 10 + _CORNER_STEP_ADJUSTMENT * 2;
    } else if (fieldIndex > 10 && fieldIndex < 20) {
        top += _CORNER_STEP_ADJUSTMENT + _STEP_PX * (fieldIndex - 10);
    } else if (fieldIndex > 30 && fieldIndex < 40) {
        top += _CORNER_STEP_ADJUSTMENT + _STEP_PX * (40 - fieldIndex);
    }
    return calculateSide(top);
}

// returning string for 'style.left'
function defineChipLeft(fieldIndex) {
    let left = _START_POSITION;
    if (fieldIndex >= 10 && fieldIndex <= 20) {
        left += _STEP_PX * 10 + _CORNER_STEP_ADJUSTMENT * 2;
    } else if (fieldIndex > 0 && fieldIndex < 10) {
        left += _STEP_PX * fieldIndex + _CORNER_STEP_ADJUSTMENT;
    } else if (fieldIndex > 20 && fieldIndex < 30) {
        left += _STEP_PX * (30 - fieldIndex) + _CORNER_STEP_ADJUSTMENT;
    }
    return calculateSide(left);
}

function getNumberProperty(style, propertyName) {
    return Number.parseInt(style.getPropertyValue(propertyName).trim());
}

function calculateSide(value) {
    return `calc(100${_POSTFIX} * ${value} / ${_WINDOW_SIZE})`;
}
