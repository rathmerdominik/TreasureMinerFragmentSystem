package net.hammerclock.fragmentsystem.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.hammerclock.fragmentsystem.FragmentSystem;
import net.hammerclock.fragmentsystem.types.FragmentStateEnum;
import net.hammerclock.fragmentsystem.utils.TreasureMinerDatabase;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;

public class FragmentCommand {
	private static final Logger LOGGER = LogManager.getLogger(FragmentSystem.PROJECT_ID);

	public FragmentCommand(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("treasure_miner").requires(context -> {
					return context.hasPermission(4);
				})
						.then(Commands.literal("get_fragment_data").executes(context -> {
							return get_fragment_data(context.getSource());
						}))
						.then(Commands.literal("clear_fragment_data").executes(context -> {
							return clear_fragment_data(context.getSource());
						}).then(Commands.argument("fragment_nr", IntegerArgumentType.integer())
								.executes(context -> {
									return clear_fragment_data_by_id(context.getSource(),
											IntegerArgumentType.getInteger(context, "fragment_nr"));
								}))));
	}

	public int clear_fragment_data(CommandSource source) throws CommandSyntaxException {
		TreasureMinerDatabase tmdb = new TreasureMinerDatabase();
		Connection conn = tmdb.getConnection();

		try {
			conn.createStatement().executeUpdate("DELETE FROM fragments");
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		}
		return 0;
	}

	public int clear_fragment_data_by_id(CommandSource source, Integer fragmentNr) {
		TreasureMinerDatabase tmdb = new TreasureMinerDatabase();
		Connection conn = tmdb.getConnection();

		try {
			PreparedStatement stmt = conn.prepareStatement("DELETE FROM fragments WHERE fragment_number = ?");

			stmt.setInt(1, fragmentNr);
			stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		}
		return 0;
	}

	public int get_fragment_data(CommandSource source) throws CommandSyntaxException {
		TreasureMinerDatabase tmdb = new TreasureMinerDatabase();
		Connection conn = tmdb.getConnection();
		StringTextComponent masterComp = new StringTextComponent("==========Fragments==========\n");

		try {

			ResultSet fragments = conn.createStatement().executeQuery("SELECT * FROM fragments");
			while (fragments.next()) {

				masterComp.append("#############################\n");
				masterComp.append("Fragment Number: " + fragments.getInt("fragment_number") + "\n");

				masterComp.append("Owner: " + fragments.getString("player_name") + "\n");

				StringTextComponent fragmentComp = new StringTextComponent("Reason: ");
				StringTextComponent colorText = new StringTextComponent("");
				String reason = fragments.getString("reason");
				FragmentStateEnum state = FragmentStateEnum.valueOf(fragments.getString("state"));

				if (state == FragmentStateEnum.IN_INVENTORY) {
					colorText.append(reason).append("\n")
							.setStyle(Style.EMPTY.withColor(TextFormatting.GREEN));
				} else if (state == FragmentStateEnum.DROPPED && reason.contentEquals("Player has died")) {
					colorText.append(reason).append("\n")
							.setStyle(Style.EMPTY.withColor(TextFormatting.LIGHT_PURPLE));
				} else {
					colorText.append(reason).append("\n")
							.setStyle(Style.EMPTY.withColor(TextFormatting.RED));
				}

				TranslationTextComponent clickableText = new TranslationTextComponent("[TP to location]");
				clickableText.setStyle(
						Style.EMPTY.withClickEvent(
								new ClickEvent(
										ClickEvent.Action.RUN_COMMAND,
										String.format("/tp %d %d %d",
												(int) Math.round(fragments.getDouble("x")),
												(int) Math.round(fragments.getDouble("y")),
												(int) Math.round(fragments.getDouble("z"))))));

				fragmentComp.append(colorText);
				// Combine the two components
				masterComp.append(fragmentComp);
				masterComp.append(clickableText);
				masterComp.append("\n#############################\n");

			}
			masterComp.append("=============================\n");
			conn.close();
		} catch (SQLException e) {
			LOGGER.fatal(e.getMessage());
		}
		source.sendSuccess(masterComp, true);
		return 0;
	}
}