import {getPlayerColorById} from './players.js';

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
            showMortgageTag(fieldIndex);
        } else {
            hideMortgageCover(fieldIndex);
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
    hideMortgageCover(fieldIndex);
    if (turns > 0) {
        const fieldPriceField = document.getElementById(`field${fieldIndex}-price`);
        if (fieldPriceField) {
            fieldPriceField.innerText = turns;
        }
        showMortgageTag(fieldIndex);
    }
}

function showMortgageTag(fieldIndex) {
    const mortgageTag = document.getElementById(`field${fieldIndex}-mortgage`);
    if (mortgageTag) {
        mortgageTag.style.display = 'block';
    }
}

function hideMortgageCover(fieldIndex) {
    const mortgageTag = document.getElementById(`field${fieldIndex}-mortgage`);
    if (mortgageTag) {
        mortgageTag.style.display = 'none';
    }
}

export function renderHouses(fieldIndex, amount) {
    const houseContainer = document.getElementById(`field${fieldIndex}-houses`);
    if (!houseContainer) {
        console.error(`cannot display houses on field with id ${fieldIndex}`);
        return;
    }
    const property = houseContainer.children;
    const propertyAmount = property.length;
    if (amount < 0 || amount > 5) {
        return;
    }
    if (amount === propertyAmount) {
        if (amount !== 1) {
            return;
        } else {
            if (property[0].className === 'house-pic') {
                return;
            }
        }
    }
    houseContainer.innerHTML = '';
    if (amount === 5) {
        addProperty(houseContainer, false);
    } else {
        for (let i = 0; i < amount; i++) {
            addProperty(houseContainer, true);
        }
    }
}

function addProperty(houseContainer, house) {
    const property = document.createElement('div');
    property.className = house === true ? 'house-pic' : 'hotel-pic';
    houseContainer.appendChild(property);
}

function addOwnerCover(fieldIndex, ownerId) {
    const fieldCover = document.getElementById(`field${fieldIndex}-cover`);
    if (fieldCover) {
        fieldCover.style.backgroundColor = getPlayerColorById(ownerId);
    }
}

function removeOwnerCover(fieldIndex) {
    const fieldCover = document.getElementById(`field${fieldIndex}-cover`);
    if (fieldCover) {
        fieldCover.style.backgroundColor = 'transparent';
    }
}