import { GameState, PropertyState, PropertyStaticData } from "../types/interfaces";
import { PROPERTY_INDEXES, UPropertyIndex } from "../types/unions";
import { PropertyGroup } from "../types/enums";
import { GameStages } from "./gameData";

export const PROPERTY_FIELDS_DATA: Record<UPropertyIndex, PropertyStaticData> = {
  1: {
    name: 'Saint Petersburg',
    group: PropertyGroup.yellow,
    price: 60,
    housePrice: 50,
  },
  3: {
    name: 'Moscow',
    group: PropertyGroup.yellow,
    price: 60,
    housePrice: 50,
  },
  5: {
    name: 'Ryanair',
    group: PropertyGroup.airports,
    price: 200,
  },
  6: {
    name: 'Helsinki',
    group: PropertyGroup.sky,
    price: 100,
    housePrice: 50,
  },
  8: {
    name: 'Oslo',
    group: PropertyGroup.sky,
    price: 100,
    housePrice: 50,
  },
  9: {
    name: 'Stockholm',
    group: PropertyGroup.sky,
    price: 120,
    housePrice: 50,
  },
  11: {
    name: 'Budapest',
    group: PropertyGroup.purple,
    price: 140,
    housePrice: 100,
  },
  12: {
    name: 'Electric Company',
    group: PropertyGroup.companies,
    price: 150,
  },
  13: {
    name: 'Prague',
    group: PropertyGroup.purple,
    price: 140,
    housePrice: 100,
  },
  14: {
    name: 'Vienna',
    group: PropertyGroup.purple,
    price: 160,
    housePrice: 100,
  },
  15: {
    name: 'KLM',
    group: PropertyGroup.airports,
    price: 200,
  },
  16: {
    name: 'Venice',
    group: PropertyGroup.brown,
    price: 180,
    housePrice: 100,
  },
  18: {
    name: 'Milan',
    group: PropertyGroup.brown,
    price: 180,
    housePrice: 100,
  },
  19: {
    name: 'Rome',
    group: PropertyGroup.brown,
    price: 200,
    housePrice: 100,
  },
  21: {
    name: 'Lisbon',
    group: PropertyGroup.orange,
    price: 220,
    housePrice: 150,
  },
  23: {
    name: 'Madrid',
    group: PropertyGroup.orange,
    price: 220,
    housePrice: 150,
  },
  24: {
    name: 'Athens',
    group: PropertyGroup.orange,
    price: 240,
    housePrice: 150,
  },
  25: {
    name: 'Lufthansa',
    group: PropertyGroup.airports,
    price: 200,
  },
  26: {
    name: 'Geneva',
    group: PropertyGroup.mustard,
    price: 260,
    housePrice: 150,
  },
  27: {
    name: 'Hamburg',
    group: PropertyGroup.mustard,
    price: 260,
    housePrice: 150,
  },
  28: {
    name: 'Petrol Company',
    group: PropertyGroup.companies,
    price: 150,
  },
  29: {
    name: 'Berlin',
    group: PropertyGroup.mustard,
    price: 280,
    housePrice: 150,
  },
  31: {
    name: 'Luxembourg',
    group: PropertyGroup.green,
    price: 300,
    housePrice: 200,
  },
  32: {
    name: 'Brussels',
    group: PropertyGroup.green,
    price: 300,
    housePrice: 200,
  },
  34: {
    name: 'Amsterdam',
    group: PropertyGroup.green,
    price: 320,
    housePrice: 200,
  },
  35: {
    name: 'British Airways',
    group: PropertyGroup.airports,
    price: 200,
  },
  37: {
    name: 'Paris',
    group: PropertyGroup.blue,
    price: 350,
    housePrice: 200,
  },
  39: {
    name: 'London',
    group: PropertyGroup.blue,
    price: 400,
    housePrice: 200,
  },
}

export const INITIAL_PROPERTY_STATE: Record<UPropertyIndex, PropertyState> = PROPERTY_INDEXES
  .reduce((aggregate, index) => ({
    ...aggregate,
    [index]: {
      isMortgaged: false,
      priceTag: `$${PROPERTY_FIELDS_DATA[index].price}`,
      houses: 0,
    }
  }), {} as Record<UPropertyIndex, PropertyState>);

export const INITIAL_GAME_STATE: GameState = {
  stage: GameStages.TURN_START,
  currentUserId: '',
  propertyStates: { ...INITIAL_PROPERTY_STATE },
  playerStates: {},
  gameStarted: false,
};

export const PROPERTY_GROUPS: Record<PropertyGroup, UPropertyIndex[]> = {
  [PropertyGroup.airports]: [5, 15, 25, 35],
  [PropertyGroup.companies]: [12, 28],
  [PropertyGroup.yellow]: [1, 3],
  [PropertyGroup.sky]: [6, 8, 9],
  [PropertyGroup.purple]: [11, 13, 14],
  [PropertyGroup.brown]: [16, 18, 19],
  [PropertyGroup.orange]: [21, 23, 24],
  [PropertyGroup.mustard]: [26, 27, 29],
  [PropertyGroup.green]: [31, 32, 34],
  [PropertyGroup.blue]: [37, 39],
}
