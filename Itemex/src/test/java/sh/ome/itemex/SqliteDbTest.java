package sh.ome.itemex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sh.ome.itemex.functions.sqliteDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.Assert.*;

public class SqliteDbTest {
    private Connection connection;

    @Before
    public void setup() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE PAYOUTS (id INTEGER PRIMARY KEY AUTOINCREMENT, player_uuid TEXT, itemid TEXT, amount INT, timestamp TEXT)");
        stmt.close();
        sh.ome.itemex.Itemex.c = connection;
    }

    @After
    public void teardown() throws Exception {
        connection.close();
    }

    @Test
    public void testInsertAndGetPayout() {
        assertTrue(sqliteDb.insertPayout("player", "DIAMOND", 5));
        sqliteDb.Payout[] payouts = sqliteDb.getPayout("player");
        assertEquals(1, payouts.length);
        assertEquals("DIAMOND", payouts[0].itemid);
        assertEquals(5, payouts[0].amount);
    }
}
