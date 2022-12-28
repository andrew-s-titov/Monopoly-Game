import {getPlayerColor} from "./players.js";

const OWNER_COVER_POSTFIX = '-owner-cover';
const MORTGAGE_TAG_POSTFIX = '-mortgage-cover';

export function renderFieldViews(fieldViews) {
    for (let fieldView of fieldViews) {
        let fieldIndex = fieldView.id;
        let nameField = document.getElementById(`field${fieldIndex}-name`);
        if (nameField) {
            nameField.innerHTML = fieldView.name;
        }
        if (fieldView.hasOwnProperty('owner_id') && fieldView.owner_id != null) {
            addOwnerCover(fieldIndex, fieldView.owner_id);
        } else {
            removeOwnerCover(fieldIndex);
        }
        if (fieldView.hasOwnProperty('price_tag')) {
            let priceTagField = document.getElementById(`field${fieldIndex}-price`);
            priceTagField.innerHTML = fieldView.price_tag;
        }
        if (fieldView.hasOwnProperty('mortgage') && fieldView.mortgage) {
            renderMortgageTag(fieldIndex);
        } else {
            removeOldMortgageCover(fieldIndex);
        }
        if (fieldView.hasOwnProperty('houses')) {
            let houses = fieldView.houses;
            renderHouses(fieldIndex, houses);
        }
    }
}

export function renderMortgageState(fieldIndex, turns) {
    if (fieldIndex < 0 || fieldIndex > 39) {
        console.error(`no field with id ${fieldIndex} found on map`);
        return;
    }
    removeOldMortgageCover(fieldIndex);
    if (turns > 0) {
        let fieldPriceField = document.getElementById(`field${fieldIndex}-price`);
        if (fieldPriceField) {
            fieldPriceField.innerText = turns;
        }
        renderMortgageTag(fieldIndex);
    }
}

function renderMortgageTag(fieldIndex) {
    let propertyField = document.getElementById(`field${fieldIndex}`);
    let newMortgageTag = document.createElement('div');
    newMortgageTag.id = `field${fieldIndex}${MORTGAGE_TAG_POSTFIX}`;
    newMortgageTag.className = 'mortgage-tag';
    addTextStickingClassName(fieldIndex, newMortgageTag);
    propertyField.appendChild(newMortgageTag);
}

function removeOldMortgageCover(fieldIndex) {
    let mortgageCover = document.getElementById(`field${fieldIndex}${MORTGAGE_TAG_POSTFIX}`);
    if (mortgageCover) {
        mortgageCover.remove();
    }
}

export function renderHouses(fieldIndex, amount) {
    let field = document.getElementById(`field${fieldIndex}`);
    if (!field) {
        console.error(`no field with id ${fieldIndex} found on map`);
        return;
    }
    let houseContainerId = `field${fieldIndex}-houses`;
    let oldContainer = document.getElementById(houseContainerId);
    if (oldContainer) {
        oldContainer.remove();
    }
    if (amount > 0) {
        let houseContainer = document.createElement('div');
        houseContainer.id = houseContainerId;
        houseContainer.style.position = 'absolute';
        if (fieldIndex < 20) {
            houseContainer.style.up = '0px';
            houseContainer.style.right = '0px';
        } else {
            houseContainer.style.bottom = '0px';
            houseContainer.style.left = '0px';
        }
        if (amount === 5) {
            let hotel = document.createElement('img');
            hotel.setAttribute('src', 'images/hotel.png')
            hotel.style.width = '20px';
            hotel.style.height = '20px';
            houseContainer.appendChild(hotel);
        } else {
            for (let i = 0; i < amount; i++) {
                let house = document.createElement('img');
                house.setAttribute('src', 'images/house.png')
                house.style.width = '10px';
                house.style.height = '10px';
                houseContainer.appendChild(house);
            }
        }
        field.appendChild(houseContainer);
    }
}

function addOwnerCover(fieldIndex, ownerId) {
    removeOwnerCover(fieldIndex);
    let propertyField = document.getElementById(`field${fieldIndex}`);
    let ownerCover = document.createElement('div');
    ownerCover.id = `field${fieldIndex}${OWNER_COVER_POSTFIX}`;
    ownerCover.className = 'owner-cover';
    addTextStickingClassName(fieldIndex, ownerCover);
    setOwnerCoverOrientation(fieldIndex, ownerCover);
    ownerCover.style.backgroundColor = getPlayerColor(ownerId);
    propertyField.appendChild(ownerCover);
}

function removeOwnerCover(fieldIndex) {
    let ownerCover = document.getElementById(`field${fieldIndex}${OWNER_COVER_POSTFIX}`);
    if (ownerCover) {
        ownerCover.remove();
    }
}

function addTextStickingClassName(fieldIndex, htmlElement) {
    let className;
    if (fieldIndex < 10) {
        className = 'stick-top';
    } else if (fieldIndex < 20) {
        className = 'stick-right';
    } else if (fieldIndex < 30) {
        className = 'stick-bottom';
    } else {
        className = 'stick-left';
    }
    htmlElement.classList.add(className);
}

function setOwnerCoverOrientation(fieldIndex, ownerCoverHtmlElement) {
    let className;
    if (fieldIndex < 10 || (fieldIndex > 20 && fieldIndex < 30)) {
        className = 'vertical-cover';
    } else {
        className = 'horizontal-cover';
    }
    ownerCoverHtmlElement.classList.add(className);
}