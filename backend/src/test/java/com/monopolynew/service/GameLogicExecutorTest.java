package com.monopolynew.service;

import com.monopolynew.dto.GameFieldState;
import com.monopolynew.event.BankruptcyEvent;
import com.monopolynew.event.FieldStateChangeEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.procedure.CheckToPay;
import com.monopolynew.map.AirportField;
import com.monopolynew.map.GameField;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;
import com.monopolynew.mapper.GameFieldMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.monopolynew.TestData.PLAYER_ID_1;
import static com.monopolynew.TestData.PLAYER_ID_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameLogicExecutorTest {

    @Mock
    GameEventSender gameEventSender;
    @Mock
    GameFieldMapper gameFieldMapper;
    @Captor
    ArgumentCaptor<FieldStateChangeEvent> fieldStateChangeEventCaptor;

    @InjectMocks
    GameLogicExecutor gameLogicExecutor;


    @Nested
    @DisplayName("test 'bankruptPlayer()'")
    class BankruptPlayer {

        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUpFieldConverter() {
            when(gameFieldMapper.toStateList(anyList())).thenAnswer(invocation ->
                    invocation.getArgument(0, List.class).stream()
                            .map(field -> GameFieldState.builder().build())
                            .toList());
        }

        @Test
        @DisplayName("""
                when bankrupt for Bank - expect:
                - debtor with 0 money and bankrupt status;
                - all debtor's fields not owned, not mortgaged and without houses;
                - needed events sent;
                """)
        void bankruptForBank() {
            // given
            var game = mock(Game.class);
            var gameMap = mock(GameMap.class);
            when(game.getGameMap()).thenReturn(gameMap);
            var debtor = new Player(PLAYER_ID_1, "player-1", "av");
            var field1 = new StreetField(1, "field-1", 60, 50, new int[]{2, 10, 30, 90, 160, 250});
            field1.newOwner(debtor);
            field1.buyHouse();
            var field2 = new StreetField(2, "field-2", 70, 50, new int[]{2, 10, 30, 90, 160, 250});
            field2.newOwner(debtor);
            field2.buyHouse();
            field2.buyHouse();
            var field3 = new StreetField(3, "field-3", 70, 50, new int[]{2, 10, 30, 90, 160, 250});
            field3.newOwner(debtor);
            field3.mortgage();
            List<GameField> playerFields = List.of(field1, field2, field3);
            when(gameMap.getFields()).thenReturn(playerFields);

            // when
            gameLogicExecutor.bankruptPlayer(game, debtor);

            // then
            assertCommonBankruptScenarioExecuted(debtor, playerFields);
        }

        @Test
        @DisplayName("""
                when bankrupt for another player and debt > debtor assets price - expect:
                - debtor with 0 money and bankrupt status;
                - beneficiary received money equal to debtor's assets
                - all debtor's fields not owned, not mortgaged and without houses;
                - needed events sent;
                """)
        void bankruptForAnotherPlayerWithNotEnoughProperty() {
            // given
            var game = mock(Game.class);
            var gameMap = mock(GameMap.class);
            when(game.getGameMap()).thenReturn(gameMap);
            var debtor = new Player(PLAYER_ID_1, "player-1", "av");
            var debtorInitialMoney = debtor.getMoney();
            var field1 = new StreetField(1, "field-1", 60, 50, new int[]{2, 10, 30, 90, 160, 250});
            field1.newOwner(debtor);
            field1.buyHouse();
            var field2 = new StreetField(2, "field-2", 70, 50, new int[]{2, 10, 30, 90, 160, 250});
            field2.newOwner(debtor);
            field2.buyHouse();
            field2.buyHouse();
            var field3 = new StreetField(3, "field-3", 70, 50, new int[]{2, 10, 30, 90, 160, 250});
            field3.newOwner(debtor);
            field3.mortgage();
            List<GameField> playerFields = List.of(field1, field2, field3);
            when(gameMap.getFields()).thenReturn(playerFields);

            var debtorAssetsTotal = debtorInitialMoney
                    + field1.getHouses() * field1.getHousePrice()
                    + field2.getHouses() * field2.getHousePrice()
                    + field1.getPrice() / 2
                    + field2.getPrice() / 2;
            var beneficiary = new Player(PLAYER_ID_2, "player-2", "av");
            beneficiary.takeMoney(beneficiary.getMoney());
            var checkToPay = CheckToPay.builder()
                    .beneficiary(beneficiary)
                    .debtor(debtor)
                    .debt(debtorAssetsTotal + 100)
                    .build();
            when(game.getCheckToPay()).thenReturn(checkToPay);

            // when
            gameLogicExecutor.bankruptPlayer(game, debtor);

            // then
            assertCommonBankruptScenarioExecuted(debtor, playerFields);
            assertEquals(debtorAssetsTotal, beneficiary.getMoney());
        }

        @Test
        @DisplayName("""
                when bankrupt for another player and debt < debtor assets price - expect:
                - debtor with 0 money and bankrupt status;
                - beneficiary received money equal to debt
                - all debtor's fields not owned, not mortgaged and without houses;
                - needed events sent;
                """)
        void bankruptForAnotherPlayerWithEnoughProperty() {
            // given
            var game = mock(Game.class);
            var gameMap = mock(GameMap.class);
            when(game.getGameMap()).thenReturn(gameMap);
            var debtor = new Player(PLAYER_ID_1, "player-1", "av");
            var debtorInitialMoney = debtor.getMoney();
            var field1 = new StreetField(1, "field-1", 60, 50, new int[]{2, 10, 30, 90, 160, 250});
            field1.newOwner(debtor);
            field1.buyHouse();
            var field2 = new StreetField(2, "field-2", 70, 50, new int[]{2, 10, 30, 90, 160, 250});
            field2.newOwner(debtor);
            field2.buyHouse();
            field2.buyHouse();
            var field3 = new StreetField(3, "field-3", 70, 50, new int[]{2, 10, 30, 90, 160, 250});
            field3.newOwner(debtor);
            field3.mortgage();
            List<GameField> playerFields = List.of(field1, field2, field3);
            when(gameMap.getFields()).thenReturn(playerFields);

            var debtorAssetsTotal = debtorInitialMoney
                    + field1.getHouses() * field1.getHousePrice()
                    + field2.getHouses() * field2.getHousePrice()
                    + field1.getPrice() / 2
                    + field2.getPrice() / 2;
            var debt = debtorAssetsTotal - 20;
            var beneficiary = new Player(PLAYER_ID_2, "player-2", "av");
            beneficiary.takeMoney(beneficiary.getMoney());
            var checkToPay = CheckToPay.builder()
                    .beneficiary(beneficiary)
                    .debtor(debtor)
                    .debt(debt)
                    .build();
            when(game.getCheckToPay()).thenReturn(checkToPay);

            // when
            gameLogicExecutor.bankruptPlayer(game, debtor);

            // then
            assertCommonBankruptScenarioExecuted(debtor, playerFields);
            assertEquals(debt, beneficiary.getMoney());
        }

        void assertCommonBankruptScenarioExecuted(Player debtor, List<GameField> playerFields) {
            assertTrue(debtor.isBankrupt());
            assertEquals(0, debtor.getMoney());
            verify(gameEventSender).sendToAllPlayers(any(BankruptcyEvent.class));
            verify(gameEventSender).sendToAllPlayers(any(MoneyChangeEvent.class));
            verify(gameEventSender).sendToAllPlayers(fieldStateChangeEventCaptor.capture());
            FieldStateChangeEvent capturedEvent = fieldStateChangeEventCaptor.getValue();
            assertEquals(playerFields.size(), capturedEvent.getChanges().size());
            playerFields.stream()
                    .map(PurchasableField.class::cast)
                    .forEach(field -> {
                        assertFalse(field.isMortgaged());
                        assertNull(field.getOwner());
                        if (field instanceof StreetField streetField) {
                            assertEquals(0, streetField.getHouses());
                        }
                    });
        }
    }

    @Nested
    @DisplayName("test 'processOwnershipChange'")
    class ProcessOwnershipChange {

        @Test
        @DisplayName("for Airports")
        void forAirport() {
            // given
            var player1 = new Player(PLAYER_ID_1, "player-1", "av");
            var player2 = new Player(PLAYER_ID_2, "player-2", "av");
            var game = mock(Game.class);
            var gameMap = mock(GameMap.class);
            when(game.getGameMap()).thenReturn(gameMap);
            var airport1 = new AirportField(5, "airport1", 200, 25);
            var airport2 = new AirportField(15, "airport2", 200, 25);
            var airport3 = new AirportField(25, "airport3", 200, 25);
            var airport4 = new AirportField(35, "airport4", 200, 25);
            List<PurchasableField> airportGroup = List.of(airport1, airport2, airport3, airport4);
            when(gameMap.getField(airport1.getId())).thenReturn(airport1);
            when(gameMap.getField(airport2.getId())).thenReturn(airport2);
            when(gameMap.getField(airport3.getId())).thenReturn(airport3);
            when(gameMap.getField(airport4.getId())).thenReturn(airport4);

            // when
            gameLogicExecutor.processOwnershipChange(game, airport1);
            gameLogicExecutor.processOwnershipChange(game, airportGroup);

            // then
            assertEquals(25, airport1.getCurrentRent());
            assertEquals(25, airport2.getCurrentRent());
            assertEquals(25, airport3.getCurrentRent());
            assertEquals(25, airport4.getCurrentRent());

            // when
            airport1.newOwner(player1);
            airport2.newOwner(player1);
            airport3.newOwner(player2);
            airport4.newOwner(player2);
            gameLogicExecutor.processOwnershipChange(game, airport1);
            gameLogicExecutor.processOwnershipChange(game, airportGroup);

            // then
            assertEquals(50, airport1.getCurrentRent());
            assertEquals(50, airport2.getCurrentRent());
            assertEquals(50, airport3.getCurrentRent());
            assertEquals(50, airport4.getCurrentRent());

            // when
            airport2.newOwner(player2);
            gameLogicExecutor.processOwnershipChange(game, airport1);
            gameLogicExecutor.processOwnershipChange(game, airportGroup);

            // then
            assertEquals(25, airport1.getCurrentRent());
            assertEquals(100, airport2.getCurrentRent());
            assertEquals(100, airport3.getCurrentRent());
            assertEquals(100, airport4.getCurrentRent());

            // when
            airport2.newOwner(null);
            gameLogicExecutor.processOwnershipChange(game, airport1);
            gameLogicExecutor.processOwnershipChange(game, airportGroup);

            // then
            assertEquals(25, airport1.getCurrentRent());
            assertEquals(25, airport2.getCurrentRent());
            assertEquals(50, airport3.getCurrentRent());
            assertEquals(50, airport4.getCurrentRent());
        }
    }
}
