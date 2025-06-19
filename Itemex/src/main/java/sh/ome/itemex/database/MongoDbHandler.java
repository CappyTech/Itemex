package sh.ome.itemex.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.HashSet;
import java.util.Set;

import sh.ome.itemex.Itemex;

public class MongoDbHandler {
    private static MongoClient client;

    public static MongoDatabase getDatabase() {
        if (client == null) {
            String uri = "mongodb://" + Itemex.db_username + ":" + Itemex.db_passwd +
                    "@" + Itemex.db_hostname + ":" + Itemex.db_port;
            client = MongoClients.create(uri);
        }
        return client.getDatabase(Itemex.db_name);
    }

    public static void createCollectionsIfNotExist() {
        MongoDatabase db = getDatabase();
        Set<String> collections = new HashSet<>();
        for (String name : db.listCollectionNames()) {
            collections.add(name);
        }
        String[] required = new String[]{"SELLORDERS","BUYORDERS","FULFILLEDORDERS","PAYOUTS","SETTINGS","SELL_NOTIFICATION"};
        for (String coll : required) {
            if (!collections.contains(coll)) {
                db.createCollection(coll);
            }
        }
    }
}
