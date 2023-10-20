package net.hammerclock.fragmentsystem.items;

import net.hammerclock.fragmentsystem.FragmentSystem;
import net.hammerclock.fragmentsystem.itemgroups.TreasureMinerGroup;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RoadPongeglyphFragmentItem {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			FragmentSystem.PROJECT_ID);

	public static final RegistryObject<Item> ROAD_PONGEGLYPH_FRAGMENT = ITEMS.register(
			"road_poneglyph_fragment",
			() -> new Item(new Item.Properties().tab(TreasureMinerGroup.TREASURE_MINER)));

	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
}