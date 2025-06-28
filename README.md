# Itemex

<img src="https://ipfs.ome.sh/ipfs/QmPhxsvnTvK9MD7HoRBouBQavebdepiQpbXp1yXvq6j3Yn/"/>

Itemex is a free market plugin that lets players trade any item in Minecraft using a real exchange style order book. The plugin integrates with Vault so it can use any supported economy plugin.

[View the full description on SpigotMC](https://www.spigotmc.org/resources/itemex-item-exchange-free-market-plugin-like-a-stock-or-crypto-exchange-with-mc-items.108398/).

## Requirements

* **Java 8** or newer
* **Maven 3** to build the plugin
* A Minecraft server running **Spigot** or **Paper** 1.19+
* The **Vault** plugin with a compatible economy implementation (e.g. EssentialsX)

## Build

The source is located under `Itemex/`. Run the following commands to build the shaded JAR:

```bash
cd Itemex
mvn clean package
```

The compiled plugin will be placed in `target/itemex-<version>-shaded.jar`.

## Install

1. Copy the generated JAR from the `target/` folder into your server's `plugins` directory.
2. Start or reload the server. Itemex will create a `config.yml` on first run.

## Running

After installation a few basic commands are available:

* `/ix help` – list all Itemex commands
* `/i gui` – open the graphical interface

Edit `plugins/Itemex/config.yml` to configure language, database type (SQLite, MariaDB, MySQL or MongoDB) and other options.

```yaml
lang: en
# database_type: sqlite | mariadb | mysql | mongodb
webui: true
web_port: 8080
```

Restart the server after changing the config.

