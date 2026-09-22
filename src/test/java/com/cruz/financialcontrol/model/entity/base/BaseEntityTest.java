package com.cruz.financialcontrol.model.entity.base;

import com.cruz.financialcontrol.model.entity.Account;
import com.cruz.financialcontrol.model.entity.Transaction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the identity-based equals/hashCode contract defined in {@link BaseEntity},
 * which every JPA entity in this application inherits instead of generating its own
 * via Lombok's @EqualsAndHashCode.
 */
class BaseEntityTest {

    @Test
    void equals_shouldReturnTrue_whenSameInstance() {
        Account account = new Account();
        account.setId(1L);

        assertThat(account).isEqualTo(account);
    }

    @Test
    void equals_shouldReturnTrue_whenSameClassAndSameId_evenIfOtherFieldsDiffer() {
        Account account1 = new Account();
        account1.setId(1L);
        account1.setName("Savings");

        Account account2 = new Account();
        account2.setId(1L);
        account2.setName("Different Name");

        assertThat(account1).isEqualTo(account2);
    }

    @Test
    void equals_shouldReturnFalse_whenIdsDiffer() {
        Account account1 = new Account();
        account1.setId(1L);

        Account account2 = new Account();
        account2.setId(2L);

        assertThat(account1).isNotEqualTo(account2);
    }

    @Test
    void equals_shouldReturnFalse_whenIdIsNull() {
        Account account1 = new Account();
        Account account2 = new Account();

        // Transient (unsaved) entities should never be considered equal to one another
        assertThat(account1).isNotEqualTo(account2);
    }

    @Test
    void equals_shouldReturnFalse_whenComparedToDifferentEntityType_evenWithSameId() {
        Account account = new Account();
        account.setId(1L);

        Transaction transaction = new Transaction();
        transaction.setId(1L);

        assertThat(account).isNotEqualTo(transaction);
    }

    @Test
    void equals_shouldReturnFalse_whenComparedToNull() {
        Account account = new Account();
        account.setId(1L);

        assertThat(account).isNotEqualTo(null);
    }

    @Test
    void hashCode_shouldBeStable_regardlessOfMutableFieldChanges() {
        Account account = new Account();
        account.setId(1L);
        account.setName("Savings");

        int hashBefore = account.hashCode();
        account.setName("Renamed");
        int hashAfter = account.hashCode();

        assertThat(hashBefore).isEqualTo(hashAfter);
    }

    @Test
    void hashCode_shouldBeSame_forAllInstancesOfSameClass() {
        Account account1 = new Account();
        Account account2 = new Account();

        assertThat(account1.hashCode()).isEqualTo(account2.hashCode());
    }

    @Test
    void hashCode_shouldDiffer_betweenDifferentEntityTypes() {
        Account account = new Account();
        Transaction transaction = new Transaction();

        assertThat(account.hashCode()).isNotEqualTo(transaction.hashCode());
    }
}
