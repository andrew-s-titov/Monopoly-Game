import {GROUP_COLORS} from "./colors.js";

import {getPlayerColor} from "./players.js";

export function renderFieldViews(fieldViews) {
    for (let fieldView of fieldViews) {
        let fieldIndex = fieldView.id;
        let htmlField = document.getElementById(`field${fieldIndex}`);
        let nameField = document.getElementById(`field${fieldIndex}-name`);
        if (nameField) {
            nameField.innerHTML = fieldView.name;
        } else {
            htmlField.innerHTML = fieldView.name;
        }
        if (fieldView.hasOwnProperty('owner_id')) {
            htmlField.style.backgroundColor = getPlayerColor(fieldView.owner_id);
        } else {
            htmlField.style.backgroundColor = 'white';
        }
        if (fieldView.hasOwnProperty('price_tag')) {
            let priceTagField = document.getElementById(`field${fieldIndex}-price`);
            let priceTag = fieldView.price_tag;
            priceTagField.innerHTML = priceTag;
            priceTagField.style.backgroundColor = GROUP_COLORS[fieldView.group];
            if (fieldView.mortgage) {
                renderMortgagePlate(fieldIndex, priceTag);
            }
        }
        if (fieldView.hasOwnProperty('houses')) {
            let houses = fieldView.houses;
            renderHouses(fieldIndex, houses);
        }
    }
}

export function renderMortgagePlate(fieldIndex, turns) {
    let propertyField = document.getElementById(`field${fieldIndex}`);
    if (!propertyField) {
        console.error(`no field with id ${fieldIndex} found on map`);
        return;
    }

    let mortgagePlateId = `field${fieldIndex}-mortgage`;
    let oldMortgagePlate = document.getElementById(mortgagePlateId);
    if (oldMortgagePlate) {
        oldMortgagePlate.remove();
    }
    if (turns > 0) {
        let fieldPriceField = document.getElementById(`field${fieldIndex}-price`);
        if (fieldPriceField) {
            fieldPriceField.innerText = turns;
        }
        let newMortgagePlate = document.createElement('div');
        newMortgagePlate.id = mortgagePlateId;
        newMortgagePlate.style.width = '100%';
        newMortgagePlate.style.height = '100%';
        newMortgagePlate.style.position = 'absolute';
        newMortgagePlate.style.background = 'white';
        newMortgagePlate.style.opacity = '0.8';
        newMortgagePlate.innerText = 'MORTGAGE';
        newMortgagePlate.style.color = 'red';
        newMortgagePlate.style.fontSize = '8px';
        newMortgagePlate.style.fontWeight = 'bold';
        newMortgagePlate.style.textAlign = 'center';
        propertyField.appendChild(newMortgagePlate);
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