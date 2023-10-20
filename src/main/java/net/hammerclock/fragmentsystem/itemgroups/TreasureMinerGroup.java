package net.hammerclock.fragmentsystem.itemgroups;

import net.hammerclock.fragmentsystem.items.RoadPongeglyphFragmentItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class TreasureMinerGroup {
	public static final ItemGroup TREASURE_MINER = new ItemGroup("treasureMinerTab") {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(RoadPongeglyphFragmentItem.ROAD_PONGEGLYPH_FRAGMENT.get());
		}
	};
}
