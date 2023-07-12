package elucent.rootsclassic.ritual.rituals;

import com.google.gson.JsonObject;
import elucent.rootsclassic.Const;
import elucent.rootsclassic.ritual.RitualEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeSpawnEggItem;

import java.util.List;

public class RitualSummoning extends RitualEffect<RitualSummoning.RitualSummoningConfig> {

	@Override
	public void doEffect(Level levelAccessor, BlockPos pos, Container inventory, List<ItemStack> incenses, RitualSummoningConfig config) {
		if (!levelAccessor.isClientSide) {
			Entity toSpawn = config.entityType.create(levelAccessor);
			if (toSpawn != null) {
				if(toSpawn instanceof Mob mob) {
					mob.finalizeSpawn((ServerLevel) levelAccessor, levelAccessor.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, (SpawnGroupData) null, (CompoundTag) null);
				}
				toSpawn.setPos(pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5);
				inventory.clearContent();
				levelAccessor.addFreshEntity(toSpawn);
				BlockEntity tile = levelAccessor.getBlockEntity(pos);
				if (tile != null) {
					tile.setChanged();
				}
			}
		}
	}

	@Override
	public MutableComponent getInfoText(RitualSummoningConfig config) {
		var egg = ForgeSpawnEggItem.fromEntityType(config.entityType);
		if (egg == null) return Component.empty();
		return Component.translatable(Const.MODID + ".jei.tooltip.summoning", config.entityType.getDescription());
	}

	@Override
	public ItemStack getResult(RitualSummoningConfig config) {
		var egg = ForgeSpawnEggItem.fromEntityType(config.entityType);
		if (egg == null) return super.getResult(config);
		var display = getInfoText(config);
		return new ItemStack(egg).setHoverName(display.withStyle(Style.EMPTY.withItalic(false)));
	}

	@Override
	public RitualSummoningConfig fromJSON(JsonObject object) {
		var id = GsonHelper.getAsString(object, "entity");
		var type = Registry.ENTITY_TYPE.get(new ResourceLocation(id));
		return new RitualSummoningConfig(type);
	}

	@Override
	public void toNetwork(RitualSummoningConfig config, FriendlyByteBuf buffer) {
		buffer.writeResourceLocation(Registry.ENTITY_TYPE.getKey(config.entityType));
	}

	@Override
	public RitualSummoningConfig fromNetwork(FriendlyByteBuf buffer) {
		var id = buffer.readResourceLocation();
		var type = Registry.ENTITY_TYPE.get(id);
		return new RitualSummoningConfig(type);
	}

	public record RitualSummoningConfig(EntityType<?> entityType) {
	}
}
