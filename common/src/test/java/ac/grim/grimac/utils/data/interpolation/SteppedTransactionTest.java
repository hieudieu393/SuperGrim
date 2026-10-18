package ac.grim.grimac.utils.data.interpolation;

import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.interpolation.stepped.SteppedEntityInterpolation;
import com.github.retrooper.packetevents.util.Vector3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SteppedTransactionTest {
    private SteppedEntityInterpolation interpolation() {
        return new SteppedEntityInterpolation(2, new SimpleCollisionBox(0, 0, 0, 0, 0, 0, false));
    }

    private void begin(SteppedEntityInterpolation interpolation, int transaction) {
        interpolation.begin(null, false, true, 0, 0, 0, null, null, false, transaction, Vector3d.zero());
    }

    @Test
    void remainsUnconfirmedUntilTheMatchingTransactionArrives() {
        SteppedEntityInterpolation interpolation = interpolation();
        assertFalse(interpolation.hasUnconfirmedMovement());
        begin(interpolation, 10);
        assertTrue(interpolation.hasUnconfirmedMovement());
        interpolation.confirm(9);
        assertTrue(interpolation.hasUnconfirmedMovement());
        interpolation.confirm(10);
        assertFalse(interpolation.hasUnconfirmedMovement());
        begin(interpolation, 11);
        interpolation.confirm(10);
        assertTrue(interpolation.hasUnconfirmedMovement());
        interpolation.confirm(11);
        assertFalse(interpolation.hasUnconfirmedMovement());
    }

    @Test
    void earlyConfirmationDoesNotAcknowledgeANewMovement() {
        SteppedEntityInterpolation interpolation = interpolation();
        interpolation.confirm(10);
        begin(interpolation, 10);
        assertTrue(interpolation.hasUnconfirmedMovement());
        interpolation.confirm(10);
        assertFalse(interpolation.hasUnconfirmedMovement());
    }

    @Test
    void resetRemovesPendingMovement() {
        SteppedEntityInterpolation interpolation = interpolation();
        begin(interpolation, 10);
        interpolation.reset(new SimpleCollisionBox(0, 0, 0, 0, 0, 0, false));
        assertFalse(interpolation.hasUnconfirmedMovement());
    }
}
