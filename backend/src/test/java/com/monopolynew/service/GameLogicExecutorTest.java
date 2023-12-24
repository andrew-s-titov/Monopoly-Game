package com.monopolynew.service;

import com.monopolynew.dto.GameFieldState;
import com.monopolynew.event.BankruptcyEvent;
import com.monopolynew.event.FieldStateChangeEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.procedure.CheckToPay;
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
            for (GameField field : playerFields) {
                if (field instanceof PurchasableField purchasableField) {
                    assertNull(purchasableField.getOwner());
                    assertFalse(purchasableField.isMortgaged());
                }
                if (field instanceof StreetField streetField) {
                    assertEquals(0, streetField.getHouses());
                }
            }
        }

        @Test
        @DisplayName("""
                when bankrupt for another player and debt > debtor assets price - expect:
                - debtor with 0 money and bankrupt status;
                - beneficiary all debtor's money received and all debtor's fields
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
                    + field3.getHouses() * field3.getHousePrice()
                    + field1.getPrice()
                    + field2.getPrice()
                    + field3.getPrice() / 2;
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
            assertEquals(beneficiary, field1.getOwner());
            assertEquals(0, field1.getHouses());
            assertEquals(beneficiary, field2.getOwner());
            assertEquals(0, field2.getHouses());
            assertEquals(beneficiary, field3.getOwner());
            assertEquals(0, field3.getHouses());
            assertTrue(field3.isMortgaged(),
                    "field should have stayed mortgaged if transferred to another player");
            assertEquals(debtorInitialMoney, beneficiary.getMoney());
        }

        @Test
        @DisplayName("""
                when bankrupt for another player and debt < debtor assets price - expect:
                - debtor with 0 money and bankrupt status;
                - beneficiary received money and debtor fields equal to debt
                - spare fields are not owned and not mortgaged and have no houses;
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
                    + field3.getHouses() * field3.getHousePrice()
                    + field1.getPrice()
                    + field2.getPrice()
                    + field3.getPrice() / 2;
            var debtDifference = 20;
            var debt = debtorAssetsTotal - debtDifference;
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
            assertEquals(beneficiary, field1.getOwner());
            assertEquals(0, field1.getHouses());
            assertEquals(beneficiary, field2.getOwner());
            assertEquals(0, field2.getHouses());
            assertNull(field3.getOwner(), "field wasn't sold to Bank");
            assertEquals(0, field3.getHouses());
            assertFalse(field3.isMortgaged(), "field sold to Bank stayed mortgaged");
            assertEquals(debtorInitialMoney + field3.getPrice() / 2 - debtDifference,
                    beneficiary.getMoney());
        }

        void assertCommonBankruptScenarioExecuted(Player debtor, List<GameField> playerFields) {
            assertTrue(debtor.isBankrupt());
            assertEquals(0, debtor.getMoney());
            verify(gameEventSender).sendToAllPlayers(any(BankruptcyEvent.class));
            verify(gameEventSender).sendToAllPlayers(any(MoneyChangeEvent.class));
            verify(gameEventSender).sendToAllPlayers(fieldStateChangeEventCaptor.capture());
            FieldStateChangeEvent capturedEvent = fieldStateChangeEventCaptor.getValue();
            assertEquals(playerFields.size(), capturedEvent.getChanges().size());
        }
    }

}
