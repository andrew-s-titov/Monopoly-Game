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
    const errorPopUp = document.getElementById('errorMessage');
    errorPopUp.style.display = 'block';
    errorPopUp.textContent = errorMessage;
}