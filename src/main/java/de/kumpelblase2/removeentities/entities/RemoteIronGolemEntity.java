package de.kumpelblase2.removeentities.entities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityIronGolem;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.World;
import de.kumpelblase2.removeentities.api.RemoteEntity;
import de.kumpelblase2.removeentities.api.RemoteEntityHandle;
import de.kumpelblase2.removeentities.api.events.RemoteEntityTouchEvent;
import de.kumpelblase2.removeentities.api.features.InventoryFeature;
import de.kumpelblase2.removeentities.api.thinking.InteractBehaviour;
import de.kumpelblase2.removeentities.api.thinking.PathfinderGoalSelectorHelper;
import de.kumpelblase2.removeentities.api.thinking.TouchBehaviour;
import de.kumpelblase2.removeentities.utilities.ReflectionUtil;

public class RemoteIronGolemEntity extends EntityIronGolem implements RemoteEntityHandle
{
	private RemoteEntity m_remoteEntity;
	protected final PathfinderGoalSelectorHelper goalSelectorHelper;
	protected final PathfinderGoalSelectorHelper targetSelectorHelper;
	protected int m_maxHealth;
	public static int defaultMaxHealth = 100;
	protected int m_lastBouncedId;
	protected long m_lastBouncedTime;
	
	static
	{
		ReflectionUtil.registerEntityType(RemoteIronGolemEntity.class, "VillagerGolem", 99);
	}
	
	public RemoteIronGolemEntity(World world)
	{
		this(world, null);
	}
	
	public RemoteIronGolemEntity(World world, RemoteEntity inRemoteEntity)
	{
		super(world);
		this.m_remoteEntity = inRemoteEntity;
		this.goalSelectorHelper = new PathfinderGoalSelectorHelper(this.goalSelector);
		this.targetSelectorHelper = new PathfinderGoalSelectorHelper(this.targetSelector);
		this.m_maxHealth = defaultMaxHealth;
	}
	
	@Override
	public Inventory getInventory()
	{
		if(!this.m_remoteEntity.getFeatures().hasFeature("Inventory"))
			return null;
		
		return ((InventoryFeature)this.m_remoteEntity.getFeatures().getFeature("Inventory")).getInventory();
	}

	@Override
	public RemoteEntity getRemoteEntity()
	{
		return this.m_remoteEntity;
	}

	@Override
	public void setupStandardGoals()
	{
	}

	@Override
	public PathfinderGoalSelectorHelper getGoalSelector()
	{
		return this.goalSelectorHelper;
	}

	@Override
	public PathfinderGoalSelectorHelper getTargetSelector()
	{
		return this.targetSelectorHelper;
	}

	@Override
	public void setMaxHealth(int inHealth)
	{
		this.m_maxHealth = inHealth;
	}
	
	@Override
	public int getMaxHealth()
	{
		if(this.m_maxHealth == 0)
			return defaultMaxHealth;
		return this.m_maxHealth;
	}
	
	@Override
	public void h_()
	{
		super.h_();
		this.getRemoteEntity().getMind().tick();
	}
	
	@Override
	public void b_(EntityHuman entity)
	{
		if(entity instanceof EntityPlayer)
		{
			if (this.getRemoteEntity().getMind().canFeel() && (this.m_lastBouncedId != entity.id || System.currentTimeMillis() - this.m_lastBouncedTime > 1000) && this.getRemoteEntity().getMind().hasBehaviour("Touch")) {
				if(entity.getBukkitEntity().getLocation().distanceSquared(getBukkitEntity().getLocation()) <= 1)
				{
					RemoteEntityTouchEvent event = new RemoteEntityTouchEvent(this.m_remoteEntity, entity.getBukkitEntity());
					Bukkit.getPluginManager().callEvent(event);
					if(event.isCancelled())
						return;
					
					((TouchBehaviour)this.getRemoteEntity().getMind().getBehaviour("Touch")).onTouch((Player)entity.getBukkitEntity());
					this.m_lastBouncedTime = System.currentTimeMillis();
					this.m_lastBouncedId = entity.id;
				}
			}
		}
		super.b_(entity);
	}
	
	@Override
	public boolean c(EntityHuman entity)
	{
		if(entity instanceof EntityPlayer && this.getRemoteEntity().getMind().canFeel() && this.getRemoteEntity().getMind().hasBehaviour("Interact"))
		{
			((InteractBehaviour)this.getRemoteEntity().getMind().getBehaviour("Interact")).onInteract((Player)entity.getBukkitEntity());
		}
		
		return super.c(entity);
	}
}
