package sh.ome.itemex;

import org.junit.Test;
import sh.ome.itemex.RAM.TopOrders;

import static org.junit.Assert.*;

public class TopOrdersTest {
    @Test
    public void testFindOrderMatch() {
        TopOrders top = new TopOrders();
        double[] buyPrices = {10,0,0,0,0};
        double[] sellPrices = {8,0,0,0,0};
        int[] buyAmounts = {1,0,0,0,0};
        int[] sellAmounts = {1,0,0,0,0};
        double[] lastPrices = {0,0,0,0,0};
        int[] timestamps = {0,0,0,0,0};
        top.update_topOrders("DIAMOND", buyPrices, sellPrices, buyAmounts, sellAmounts, lastPrices, timestamps);
        assertTrue(top.find_order_match());
    }
}
