let _ERROR_OPACITY_TIMEOUT_ID = -100;
let _ERROR_VISIBILITY_TIMEOUT_ID = -100;
let _ERROR_POP_UP = null;

export function removeElementsIfPresent(...elementIDs) {
    for (let elementId of elementIDs) {
        const element = document.getElementById(elementId);
        if (element) {
            element.remove();
        }
    }
}

export function createImage(srcTag, altTag) {
    const image = document.createElement('img');
    if (srcTag !== undefined && typeof srcTag === 'string') {
        image.src = srcTag;
    }
    if (altTag !== undefined && typeof altTag === 'string') {
        image.alt = altTag;
    }
    return image;
}

export function displayError(errorMessage) {
    if (_ERROR_OPACITY_TIMEOUT_ID !== 0) {
        clearTimeout(_ERROR_OPACITY_TIMEOUT_ID);
        clearTimeout(_ERROR_VISIBILITY_TIMEOUT_ID);
    }
    const errorPopUp = getErrorPopUpElement();
    errorPopUp.style.display = 'block';
    errorPopUp.style.transition = 'none';
    errorPopUp.textContent = errorMessage;
    errorPopUp.style.opacity = '1';
    _ERROR_OPACITY_TIMEOUT_ID = setTimeout(() => {
            errorPopUp.style.transition = 'opacity 1s ease';
            errorPopUp.style.opacity = '0'
            _ERROR_VISIBILITY_TIMEOUT_ID = setTimeout(() => {
                errorPopUp.style.display = 'none';
            },
                1000);
        },
        2000);
}

function getErrorPopUpElement() {
    if (_ERROR_POP_UP === null) {
        _ERROR_POP_UP = document.getElementById('errorMessage');
    }
    return _ERROR_POP_UP;
}