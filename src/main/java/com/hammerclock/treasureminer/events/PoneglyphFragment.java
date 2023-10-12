package com.hammerclock.treasureminer.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hammerclock.treasureminer.TreasureMiner;
import com.hammerclock.treasureminer.types.FragmentState;
import com.hammerclock.treasureminer.types.FragmentStateEnum;
import com.hammerclock.treasureminer.utils.TreasureMinerDatabase;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TreasureMiner.PROJECT_ID)
public class PoneglyphFragment {
	private static final Logger LOGGER = LogManager.getLogger(TreasureMiner.PROJECT_ID);
	private static final ResourceLocation fragmentResource = new ResourceLocation("onepiecemod", "roadponeglyphfragment");

	@SubscribeEvent
	public static void onItemToss(ItemTossEvent event) {

		if (event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			ItemEntity itemEntity = event.getEntityItem();
			ItemStack itemStack = itemEntity.getItem();

			if (!itemStack.isEmpty() && itemStack.getItem().getRegistryName()
					.equals(fragmentResource)) {

				insertOrUpdateFragmentState(player, itemEntity, FragmentStateEnum.DROPPED, "Player threw item away");
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (event.getEntityLiving() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
			World world = event.getEntityLiving().level;

			double x = event.getEntityLiving().getX();
			double y = event.getEntityLiving().getY();
			double z = event.getEntityLiving().getZ();

			for (ItemStack itemStack : player.inventory.items) {
				if (!itemStack.isEmpty() && itemStack.getItem().getRegistryName()
				.equals(fragmentResource)) {
					ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack);
					insertOrUpdateFragmentState(player, itemEntity, FragmentStateEnum.DROPPED, "Player has died");
				}
			}
		}
	}

	@SubscribeEvent
	public static void onItemDespawn(ItemExpireEvent event) {
		ItemStack itemStack = event.getEntityItem().getItem();
		if (!itemStack.isEmpty()) {
			Item item = itemStack.getItem();

			ResourceLocation registryName = item.getRegistryName();
			if (registryName != null) {
				if (itemStack.getItem().getRegistryName()
				.equals(fragmentResource)) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onExplosion(ExplosionEvent.Detonate event) {
		List<Entity> affectedEntities = event.getAffectedEntities();

		List<ItemEntity> itemsToKeep = new ArrayList<>();

		for (Entity entity : affectedEntities) {
			if (entity instanceof ItemEntity) {
				ItemEntity itemEntity = (ItemEntity) entity;

				ItemStack destroyedItem = itemEntity.getItem();

				if (!destroyedItem.isEmpty()) {
					ResourceLocation itemRegistryName = destroyedItem.getItem().getRegistryName();

					if (itemRegistryName.equals(fragmentResource)) {
						itemsToKeep.add(itemEntity);
					}
				}
			}
		}

		affectedEntities.removeAll(itemsToKeep);
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof ItemEntity) {
			ItemEntity itemEntity = (ItemEntity) entity;
			if (itemEntity.getItem().getItem().getRegistryName().equals(fragmentResource)) {

				entity.setInvulnerable(true);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
		ItemStack itemStack = event.getItemStack();
		Entity target = event.getTarget();

		if (!itemStack.isEmpty() &&
				itemStack.getItem().getRegistryName().equals(fragmentResource) &&
				target.getDisplayName().getString().equals("Item Frame")) {
			event.getPlayer().displayClientMessage(
					new StringTextComponent("The Fragment rejected your attempt to display it"), true);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onContainerEvent(PlayerContainerEvent.Open event) {
		Container container = event.getContainer();
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();


		if (event.getPlayer().hasPermissions(4)) {
			return;
		}
		if (container instanceof PlayerContainer) {
			return;
		}

		int containerSlots = event.getContainer().slots.size() - (event.getPlayer().inventory.items.size());

		for (int i = 0; i < containerSlots; i++) {
			Slot slot = event.getContainer().slots.get(i);
			ItemStack itemStack = slot.getItem();

			ResourceLocation itemRegistryName = itemStack.getItem().getRegistryName();
			if (slot.hasItem() && itemRegistryName.equals(fragmentResource)) {
				event.getPlayer().displayClientMessage(
						new StringTextComponent("You have found a fragment! The Fragment has ejected itself from the container!"), true);

				ItemEntity itemEntity = new ItemEntity(event.getPlayer().level, event.getPlayer().getX(),
						event.getPlayer().getY(), event.getPlayer().getZ(), itemStack);
				container.setItem(i, ItemStack.EMPTY);
				player.drop(itemStack, false);
				event.getPlayer().level.playSound(event.getPlayer(), event.getPlayer().getX(), event.getPlayer().getY(), event.getPlayer().getZ(), SoundEvents.BEACON_POWER_SELECT, SoundCategory.BLOCKS, 5.0F, 1.0F);
				event.getPlayer().playNotifySound( SoundEvents.BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1.0F, 1.0F);
				onItemToss(new ItemTossEvent(itemEntity, event.getPlayer()));
			}
		}
	}

	@SubscribeEvent
	public static void onContainerClose(PlayerContainerEvent.Close event) {
		Container container = event.getContainer();
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();


		if (event.getPlayer().hasPermissions(4)) {
			return;
		}
		if (container instanceof PlayerContainer) {
			return;
		}

		int containerSlots = event.getContainer().slots.size() - (event.getPlayer().inventory.items.size());

		for (int i = 0; i < containerSlots; i++) {
			Slot slot = event.getContainer().slots.get(i);
			ItemStack itemStack = slot.getItem();

			ResourceLocation itemRegistryName = itemStack.getItem().getRegistryName();
			if (slot.hasItem() && itemRegistryName.equals(fragmentResource)) {
				event.getPlayer().displayClientMessage(
						new StringTextComponent("The Fragment rejected your attempt to stash it"), true);

				ItemEntity itemEntity = new ItemEntity(event.getPlayer().level, event.getPlayer().getX(),
						event.getPlayer().getY(), event.getPlayer().getZ(), itemStack);
				container.setItem(i, ItemStack.EMPTY);
				player.drop(itemStack, false);

				onItemToss(new ItemTossEvent(itemEntity, event.getPlayer()));
			}
		}
	}

	@SubscribeEvent
	public static void onItemPickup(EntityItemPickupEvent event) {
		ItemStack itemStack = event.getItem().getItem();

		if (!itemStack.isEmpty() && itemStack.getItem().getRegistryName()
				.equals(fragmentResource)) {
			CompoundNBT tag = itemStack.getOrCreateTag();

			if (!tag.contains("fragmentuuid")) {
				tag.putString("fragmentuuid", UUID.randomUUID().toString());
			}

			insertOrUpdateFragmentState((ServerPlayerEntity) event.getPlayer(), event.getItem(),
					FragmentStateEnum.IN_INVENTORY,
					"In inventory");
		}
	}

	public static void insertOrUpdateFragmentState(ServerPlayerEntity player, ItemEntity fragment,
			FragmentStateEnum state,
			String reason) {

			CompoundNBT tag = fragment.getItem().getOrCreateTag();

			if (!tag.contains("fragmentuuid"))
				return;
			
			TreasureMinerDatabase tmDb = new TreasureMinerDatabase();
			Connection dbConn = tmDb.getConnection();

			FragmentState fragmentData = new FragmentState();

			fragmentData.setUuid(tag.getString("fragmentuuid"));
			fragmentData.setName(player.getDisplayName().getString());
			fragmentData.setX(fragment.getX());
			fragmentData.setY(fragment.getY());
			fragmentData.setZ(fragment.getZ());
			fragmentData.setState(state);
			fragmentData.setReason(reason);

			try {
				ResultSet res = dbConn.createStatement().executeQuery("SELECT COUNT(uuid) AS total FROM fragments");

				PreparedStatement checkIsInDatabase = dbConn.prepareStatement("SELECT fragment_number, COUNT(uuid) AS total FROM fragments WHERE uuid = ?");
				checkIsInDatabase.setString(1, tag.getString("fragmentuuid"));
				ResultSet databaseMatches = checkIsInDatabase.executeQuery();

				Integer matches = 0;
				Integer fragmentNr = -1;
				while (databaseMatches.next()) {
					matches = databaseMatches.getInt("total");
					fragmentNr = databaseMatches.getInt("fragment_number");
				}

				while (res.next()) {
					if (matches == 0) {
						fragmentData.setFragmentNr(res.getInt("total") + 1);
					} else {
						fragmentData.setFragmentNr(fragmentNr);
					}
				}
				
			} catch (SQLException e) {
				LOGGER.warn(e.getSQLState());
				LOGGER.warn(e.getStackTrace().toString());
				LOGGER.error(e.getMessage());
			}
			
			replaceIntoTable(fragmentData);
	}

	public static void replaceIntoTable(FragmentState fragmentData) {
		TreasureMinerDatabase tmDb = new TreasureMinerDatabase();
		Connection dbConn = tmDb.getConnection();

		try {
			String query = new StringBuilder()
				.append("REPLACE INTO Fragments ")
				.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
				.toString();
			PreparedStatement stmt = dbConn.prepareStatement(query);

			stmt.setString(1, fragmentData.getUUID());
			stmt.setString(2, fragmentData.getName());
			stmt.setDouble(3, fragmentData.getX());
			stmt.setDouble(4, fragmentData.getY());
			stmt.setDouble(5, fragmentData.getZ());
			stmt.setString(6, fragmentData.getState().toString());
			stmt.setString(7, fragmentData.getReason());
			stmt.setInt(8, fragmentData.getFragmentNumber());
			
			stmt.executeUpdate();

			dbConn.close();
		} catch (SQLException e) {
			LOGGER.warn(e.getSQLState());
			LOGGER.warn(e.getStackTrace().toString());
			LOGGER.error(e.getMessage());
		} catch (NullPointerException e) {
			LOGGER.fatal(e.getMessage());
		}
	}
}
