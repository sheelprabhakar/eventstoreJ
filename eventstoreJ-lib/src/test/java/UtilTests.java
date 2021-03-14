import org.c4c.eventstorej.Utils;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testable
public class UtilTests {
    @Test
    public void is_valid_uuid_ok(){
        assertTrue(Utils.isValidUUID("009692ee-f930-4a74-bbd0-63b8baa5a927"));
    }
    @Test
    public void is_valid_uuid_false(){
        assertFalse(Utils.isValidUUID(null));
        assertFalse(Utils.isValidUUID(""));
        assertFalse(Utils.isValidUUID("test-ss-ss-ss-s"));
        assertFalse(Utils.isValidUUID("009692ee-f9309-4a74-bbd0-63b8baa5a927"));
        assertFalse(Utils.isValidUUID("1-1-1-1"));
    }
}
