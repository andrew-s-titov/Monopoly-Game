export const ACTION_BUTTONS_CONTAINER_ID = 'action_container';

export function createActionButton(text, url, able) {
    let actionButton = document.createElement('button');
    actionButton.innerText = text;
    actionButton.style.display = 'block';
    actionButton.style.width = '150px';
    actionButton.style.margin = 'auto';
    if (!able) {
        actionButton.disabled = true;
    }
    actionButton.addEventListener('click', () => {
        let httpRequester = new XMLHttpRequest();
        httpRequester.open('GET', url, true);
        httpRequester.send();
    });
    return actionButton;
}

export function removeOldActionContainer() {
    let oldContainer = document.getElementById(ACTION_BUTTONS_CONTAINER_ID);
    if (oldContainer) {
        oldContainer.remove();
    }
}

export function renderActionContainer(text, button1, button2) {
    let actionContainer = document.createElement('div');
    actionContainer.id = ACTION_BUTTONS_CONTAINER_ID;
    actionContainer.style.width = '20%';
    actionContainer.style.opacity = '0.9'
    actionContainer.style.position = 'fixed';
    actionContainer.style.left = '45%';
    actionContainer.style.top = '35%';
    actionContainer.style.backgroundColor = 'black';

    let actionPhrase = document.createElement('div');
    actionPhrase.innerText = text;
    actionPhrase.style.fontSize = '15px';
    actionPhrase.style.color = 'white';
    actionPhrase.style.textAlign = 'center';
    actionPhrase.style.marginTop = '20px';
    actionPhrase.style.marginBottom = '20px';
    actionContainer.appendChild(actionPhrase);
    if (button1 != null) {
        button1.addEventListener('click', () => actionContainer.remove());
        button1.style.marginBottom = '10px';
        actionContainer.appendChild(button1);
    }
    if (button2 != null) {
        button2.addEventListener('click', () => actionContainer.remove());
        button2.style.marginBottom = '10px';
        actionContainer.appendChild(button2);
    }
    document.getElementById('map').appendChild(actionContainer);
}

