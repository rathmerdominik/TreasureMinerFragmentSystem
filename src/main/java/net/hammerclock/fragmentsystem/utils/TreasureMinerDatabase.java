package net.hammerclock.fragmentsystem.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.hammerclock.fragmentsystem.FragmentSystem;
import net.minecraftforge.fml.loading.FMLPaths;

public class TreasureMinerDatabase {
	private String dbUrl = String.format("jdbc:sqlite:%s/treasure_miner.db", FMLPaths.GAMEDIR.get().toString());
	private Connection connection;
	private static final Logger LOGGER = LogManager.getLogger(FragmentSystem.PROJECT_ID);

	public TreasureMinerDatabase() {
		try {
			Class.forName("org.sqlite.JDBC");
			this.connection = DriverManager.getConnection(dbUrl);
		} catch (SQLException e) {
			LOGGER.warn(e.getSQLState());
			LOGGER.warn(e.getStackTrace().toString());
			LOGGER.warn(e.getMessage());
		} catch (ClassNotFoundException e) {
			LOGGER.fatal("JDBC IS BROKEN! PLEASE CONTACT DerHammerclock!");
			return;
		}
	}

	public Connection getConnection() {
		return this.connection;
	}

	public void setConnection(String dbUrl) {
		try {
			this.connection = DriverManager.getConnection(dbUrl);
		} catch (SQLException e) {
			LOGGER.warn(e.getSQLState());
			LOGGER.warn(e.getStackTrace().toString());
			LOGGER.warn(e.getMessage());
		}
	}

}
