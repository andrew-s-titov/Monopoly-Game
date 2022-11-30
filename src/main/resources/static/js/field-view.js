import {getPlayerColor} from "./players.js";

const OWNER_COVER_POSTFIX = '-owner-cover';
const MORTGAGE_COVER_POSTFIX = '-mortgage-cover';

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
            if (fieldView.hasOwnProperty('mortgage') && fieldView.mortgage) {
                renderMortgageCover(fieldIndex);
            }
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
        renderMortgageCover(fieldIndex);
    }
}

function renderMortgageCover(fieldIndex) {
    let propertyField = document.getElementById(`field${fieldIndex}`);
    let newMortgageCover = document.createElement('div');
    newMortgageCover.id = `field${fieldIndex}${MORTGAGE_COVER_POSTFIX}`;
    newMortgageCover.className = defineCoverClassName(fieldIndex, 'mortgage-cover');
    newMortgageCover.innerText = 'MORTGAGE';
    if (fieldIndex < 10 || (fieldIndex > 20 && fieldIndex < 30)) {
        newMortgageCover.style.writingMode = 'vertical-rl';
    }
    propertyField.appendChild(newMortgageCover);
}

function removeOldMortgageCover(fieldIndex) {
    let mortgageCover = document.getElementById(`field${fieldIndex}${MORTGAGE_COVER_POSTFIX}`);
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
    ownerCover.className = defineCoverClassName(fieldIndex, 'owner-cover');
    ownerCover.style.backgroundColor = getPlayerColor(ownerId);
    propertyField.appendChild(ownerCover);
}

function removeOwnerCover(fieldIndex) {
    let ownerCover = document.getElementById(`field${fieldIndex}${OWNER_COVER_POSTFIX}`);
    if (ownerCover) {
        ownerCover.remove();
    }
}

function defineCoverClassName(fieldIndex, baseCoverClassName) {
    let className;
    if (fieldIndex < 10) {
        className = `${baseCoverClassName} vertical-cover stick-top`;
    } else if (fieldIndex < 20) {
        className = `${baseCoverClassName} horizontal-cover stick-right`;
    } else if (fieldIndex < 30) {
        className = `${baseCoverClassName} vertical-cover stick-bottom`;
    } else {
        className = `${baseCoverClassName} horizontal-cover stick-left`;
    }
    return className;
}