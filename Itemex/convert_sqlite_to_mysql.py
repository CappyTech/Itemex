# install requirements:
# pip install pymysql

# edit your database settings at the bottom

import sqlite3
import pymysql

def create_tables_in_mariadb(maria_cursor):
    # Create table statements for MariaDB
    tables = {
        "SELLORDERS": """
            CREATE TABLE IF NOT EXISTS SELLORDERS (
                id INT AUTO_INCREMENT PRIMARY KEY,
                player_uuid TEXT,
                itemid TEXT,
                ordertype TEXT,
                amount INT,
                price REAL,
                timestamp TEXT
            )""",
        "BUYORDERS": """
            CREATE TABLE IF NOT EXISTS BUYORDERS (
                id INT AUTO_INCREMENT PRIMARY KEY,
                player_uuid TEXT,
                itemid TEXT,
                ordertype TEXT,
                amount INT,
                price REAL,
                timestamp TEXT
            )""",
        "FULFILLEDORDERS": """
            CREATE TABLE IF NOT EXISTS FULFILLEDORDERS (
                id INT AUTO_INCREMENT PRIMARY KEY,
                seller_uuid TEXT,
                buyer_uuid TEXT,
                itemid TEXT,
                amount INT,
                price REAL,
                timestamp TEXT
            )""",
        "PAYOUTS": """
            CREATE TABLE IF NOT EXISTS PAYOUTS (
                id INT AUTO_INCREMENT PRIMARY KEY,
                player_uuid TEXT,
                itemid TEXT,
                amount INT,
                timestamp TEXT
            )""",
        "SETTINGS": """
            CREATE TABLE IF NOT EXISTS SETTINGS (
                id INT AUTO_INCREMENT PRIMARY KEY,
                player_uuid TEXT UNIQUE,
                set1 TEXT,
                set2 TEXT,
                set3 TEXT,
                set4 TEXT,
                set5 TEXT,
                set6 TEXT,
                withdraw_threshold INT
            )""",
        "SELL_NOTIFICATION": """
            CREATE TABLE IF NOT EXISTS SELL_NOTIFICATION (
                id INT AUTO_INCREMENT PRIMARY KEY,
                player_uuid TEXT,
                itemid TEXT,
                amount INT,
                price REAL,
                timestamp TEXT
            )"""
    }

    for statement in tables.values():
        maria_cursor.execute(statement)

def transfer_data(sqlite_db_file, maria_db_config):
    # Connect to SQLite
    sqlite_conn = sqlite3.connect(sqlite_db_file)
    sqlite_cursor = sqlite_conn.cursor()

    # Connect to MariaDB
    maria_conn = pymysql.connect(**maria_db_config)
    maria_cursor = maria_conn.cursor()

    # Create tables in MariaDB if they do not exist
    create_tables_in_mariadb(maria_cursor)

    # List of tables
    table_names = [
        "SELLORDERS", "BUYORDERS", "FULFILLEDORDERS", "PAYOUTS",
        "SETTINGS", "SELL_NOTIFICATION"
    ]

    for table in table_names:
        # Fetch data from SQLite
        sqlite_cursor.execute(f"SELECT * FROM {table}")
        rows = sqlite_cursor.fetchall()

        if rows:
            placeholders = ", ".join(["%s"] * len(rows[0]))
            insert_stmt = f"INSERT INTO {table} VALUES ({placeholders})"
            
            # Loop over each row and insert one by one
            for row in rows:
                try:
                    maria_cursor.execute(insert_stmt, row)
                    maria_conn.commit()
                except pymysql.err.IntegrityError as e:
                    if e.args[0] == 1062:  # Code for duplicate entry
                        print(f"Duplicate entry found for table {table}, data: {row}. Skipping.")
                    else:
                        # If it's another integrity error, raise it to see what it is
                        raise
                except Exception as e:
                    # This is a general catch for all other exceptions. Decide if you want this.
                    print(f"Error inserting data into table {table}, data: {row}. Error: {e}")


    # Close connections
    sqlite_cursor.close()
    sqlite_conn.close()
    maria_cursor.close()
    maria_conn.close()

if __name__ == "__main__":
    SQLITE_DB_FILE = 'itemex.db'                    # if this program isn't in the same folder, use the full path
    MARIA_DB_CONFIG = {
        'host': 'localhost',                        # IP address or domain of your MariaDB or MySQL server
        'user': 'root',         
        'password': 'password',
        'db': 'itemex_db'                           # name of the database (you have to create it manually)
    }
    transfer_data(SQLITE_DB_FILE, MARIA_DB_CONFIG)
