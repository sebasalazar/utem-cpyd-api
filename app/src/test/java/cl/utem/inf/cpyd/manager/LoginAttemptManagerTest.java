package cl.utem.inf.cpyd.manager;

import cl.utem.inf.cpyd.exceptions.TooManyRequestsException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class LoginAttemptManagerTest {

    @Test
    void blocksAfterMaxFailures() {
        final LoginAttemptManager m = new LoginAttemptManager(3, 15);
        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(() -> m.checkAllowed("a"));
            m.recordFailure("a");
        }
        assertThrows(TooManyRequestsException.class, () -> m.checkAllowed("a"));
    }

    @Test
    void keysAreIndependent() {
        final LoginAttemptManager m = new LoginAttemptManager(1, 15);
        m.recordFailure("a");
        assertThrows(TooManyRequestsException.class, () -> m.checkAllowed("a"));
        assertDoesNotThrow(() -> m.checkAllowed("b"));
    }

    @Test
    void resetClearsFailures() {
        final LoginAttemptManager m = new LoginAttemptManager(1, 15);
        m.recordFailure("a");
        m.reset("a");
        assertDoesNotThrow(() -> m.checkAllowed("a"));
    }

    @Test
    void invalidConfigIsClampedToMinimums() {
        final LoginAttemptManager m = new LoginAttemptManager(0, 0);
        assertDoesNotThrow(() -> m.checkAllowed("a"));
        m.recordFailure("a");
        assertThrows(TooManyRequestsException.class, () -> m.checkAllowed("a"));
    }
}
