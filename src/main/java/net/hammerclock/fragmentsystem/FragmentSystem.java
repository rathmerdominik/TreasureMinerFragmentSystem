package net.hammerclock.fragmentsystem;

import net.hammerclock.fragmentsystem.commands.FragmentCommand;
import net.hammerclock.fragmentsystem.items.RoadPongeglyphFragmentItem;
import net.hammerclock.fragmentsystem.utils.TreasureMinerDatabase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
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

@Mod(FragmentSystem.PROJECT_ID)
public class FragmentSystem {
    public static final String PROJECT_ID = "fragment_system";

    public static final Logger LOGGER = LogManager.getLogger(FragmentSystem.PROJECT_ID);

    public FragmentSystem() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        RoadPongeglyphFragmentItem.register(eventBus);

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
