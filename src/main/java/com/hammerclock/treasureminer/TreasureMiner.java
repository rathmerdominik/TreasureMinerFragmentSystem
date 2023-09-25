package com.hammerclock.treasureminer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hammerclock.treasureminer.commands.FragmentCommand;
import com.hammerclock.treasureminer.utils.TreasureMinerDatabase;

@Mod(TreasureMiner.PROJECT_ID)
public class TreasureMiner {
    public static final String PROJECT_ID = "fragment_system";
    public static final Logger LOGGER = LogManager.getLogger(TreasureMiner.PROJECT_ID);

    public TreasureMiner() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
        TreasureMinerDatabase tmDb = new TreasureMinerDatabase();
        Connection dbConn = tmDb.getConnection();

        createFragmentTable(dbConn);
        LOGGER.info("Fragment System loaded!");
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        new FragmentCommand(event.getServer().getCommands().getDispatcher());
    }
    public void createFragmentTable(Connection dbConn) {
        StringBuilder sql = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS Fragments (")
                .append("uuid TEXT PRIMARY KEY,")
                .append("player_name TEXT NOT NULL,")
                .append("x REAL NOT NULL,")
                .append("y REAL NOT NULL,")
                .append("z REAL NOT NULL,")
                .append("state TEXT NOT NULL,")
                .append("reason TEXT NOT NULL,")
                .append("fragment_number INTEGER NOT NULL")
                .append(")");
        try {
            Statement stmt = dbConn.createStatement();
            stmt.executeUpdate(sql.toString());
        } catch (SQLException e) {
            LOGGER.fatal(e.getMessage());
        }
    }
}
