let _ERROR_TIMEOUT_ID = 0;
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
    if (_ERROR_TIMEOUT_ID !== 0) {
        clearTimeout(_ERROR_TIMEOUT_ID)
    }
    const errorPopUp = getErrorPopUpElement();
    errorPopUp.style.transition = 'none';
    errorPopUp.textContent = errorMessage;
    errorPopUp.style.opacity = '1';
    _ERROR_TIMEOUT_ID = setTimeout(() => {
            errorPopUp.style.transition = 'opacity 1s ease';
            errorPopUp.style.opacity = '0'
        },
        2000);
}

export async function loadHtmlPage(url, containerClassName) {
    if (url === undefined || url === null || typeof url !== 'string') {
        console.error("cannot load html content with empty URL");
        return;
    }
    const element = document.createElement('div');
    element.className = containerClassName;
    element.innerHTML = await fetch(url)
        .then(response => {
            if (response.status !== 200) {
                console.error('');
                return null;
            }
            return response;
        })
        .then(response => response.text())
        .catch(error => {
            displayError('Something went wrong. Please, reload the page');
            console.log(error)
        });
    return element;
}

function getErrorPopUpElement() {
    if (_ERROR_POP_UP === null) {
        _ERROR_POP_UP = document.getElementById('errorMessage');
    }
    return _ERROR_POP_UP;
}