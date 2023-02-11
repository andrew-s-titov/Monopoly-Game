let errorTimeoutId = 0;

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
    if (errorTimeoutId !== 0) {
        clearTimeout(errorTimeoutId)
    }
    const errorPopUp = document.getElementById('errorMessage');
    errorPopUp.style.transition = 'none';
    errorPopUp.textContent = errorMessage;
    errorPopUp.style.opacity = '1';
    errorTimeoutId = setTimeout(() => {
            errorPopUp.style.transition = 'opacity 1s ease';
            errorPopUp.style.opacity = '0'
        },
        2000);
}