package de.kumpelblase2.remoteentities.api.thinking.selectors;

import net.minecraft.server.v1_5_R3.Entity;
import net.minecraft.server.v1_5_R3.IEntitySelector;
import net.minecraft.server.v1_5_R3.IMonster;

public class EntitySelectorMonster implements IEntitySelector
{
	@Override
	public boolean a(Entity inEntity)
	{
		return inEntity instanceof IMonster;
	}
}
