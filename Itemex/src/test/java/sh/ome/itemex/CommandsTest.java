package sh.ome.itemex;

import org.junit.Test;
import sh.ome.itemex.commands.commands;

import static org.junit.Assert.*;

public class CommandsTest {
    @Test
    public void testGetItemId() {
        String json = "[{\"itemid\":\"DIAMOND_SWORD\"}]";
        assertEquals("DIAMOND_SWORD", commands.get_itemid(json));
    }

    @Test
    public void testGetJsonFromMeta() {
        String meta = "DIAMOND_SWORD";
        String expected = "[{\"itemid\":\"DIAMOND_SWORD\"}]";
        assertEquals(expected, commands.get_json_from_meta(meta));
    }
}
