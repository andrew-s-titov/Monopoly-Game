import {getPlayerColorById} from "./players.js";

const OWNER_COVER_POSTFIX = '-owner-cover';
const MORTGAGE_TAG_POSTFIX = '-mortgage-cover';

export function renderFieldViews(fieldViews) {
    for (let fieldView of fieldViews) {
        const fieldIndex = fieldView.id;
        const nameField = document.getElementById(`field${fieldIndex}-name`);
        if (nameField) {
            nameField.textContent = fieldView.name;
        }
        if (fieldView.hasOwnProperty('owner_id') && fieldView.owner_id != null) {
            addOwnerCover(fieldIndex, fieldView.owner_id);
        } else {
            removeOwnerCover(fieldIndex);
        }
        if (fieldView.hasOwnProperty('price_tag')) {
            document.getElementById(`field${fieldIndex}-price`).textContent = fieldView.price_tag;
        }
        if (fieldView.hasOwnProperty('mortgage') && fieldView.mortgage) {
            renderMortgageTag(fieldIndex);
        } else {
            removeOldMortgageCover(fieldIndex);
        }
        if (fieldView.hasOwnProperty('houses')) {
            renderHouses(fieldIndex, fieldView.houses);
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
        const fieldPriceField = document.getElementById(`field${fieldIndex}-price`);
        if (fieldPriceField) {
            fieldPriceField.innerText = turns;
        }
        renderMortgageTag(fieldIndex);
    }
}

function renderMortgageTag(fieldIndex) {
    const propertyField = document.getElementById(`field${fieldIndex}`);
    const newMortgageTag = document.createElement('div');
    newMortgageTag.id = `field${fieldIndex}${MORTGAGE_TAG_POSTFIX}`;
    newMortgageTag.className = 'mortgage-tag';
    addTextStickingClassName(fieldIndex, newMortgageTag);
    propertyField.appendChild(newMortgageTag);
}

function removeOldMortgageCover(fieldIndex) {
    const mortgageCover = document.getElementById(`field${fieldIndex}${MORTGAGE_TAG_POSTFIX}`);
    if (mortgageCover) {
        mortgageCover.remove();
    }
}

export function renderHouses(fieldIndex, amount) {
    const field = document.getElementById(`field${fieldIndex}`);
    if (!field) {
        console.error(`no field with id ${fieldIndex} found on map`);
        return;
    }
    const houseContainerId = `field${fieldIndex}-houses`;
    const oldContainer = document.getElementById(houseContainerId);
    if (oldContainer) {
        oldContainer.remove();
    }
    if (amount > 0) {
        const houseContainer = document.createElement('div');
        houseContainer.id = houseContainerId;
        houseContainer.classList.add('house-container');
        if (fieldIndex < 20) {
            houseContainer.classList.add('stick-top', 'stick-right');
        } else {
            houseContainer.classList.add('stick-bottom', 'stick-left');
        }
        if (amount === 5) {
            const hotel = document.createElement('div');
            hotel.className = 'hotel-pic';
            houseContainer.appendChild(hotel);
        } else {
            for (let i = 0; i < amount; i++) {
                const house = document.createElement('div');
                house.className = 'house-pic';
                houseContainer.appendChild(house);
            }
        }
        field.appendChild(houseContainer);
    }
}

function addOwnerCover(fieldIndex, ownerId) {
    removeOwnerCover(fieldIndex);
    const propertyField = document.getElementById(`field${fieldIndex}`);
    const ownerCover = document.createElement('div');
    ownerCover.id = `field${fieldIndex}${OWNER_COVER_POSTFIX}`;
    ownerCover.className = 'owner-cover';
    addTextStickingClassName(fieldIndex, ownerCover);
    setOwnerCoverOrientation(fieldIndex, ownerCover);
    ownerCover.style.backgroundColor = getPlayerColorById(ownerId);
    propertyField.appendChild(ownerCover);
}

function removeOwnerCover(fieldIndex) {
    const ownerCover = document.getElementById(`field${fieldIndex}${OWNER_COVER_POSTFIX}`);
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