package de.kumpelblase2.removeentities;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Map.Entry;
import net.minecraft.server.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import de.kumpelblase2.removeentities.api.*;
import de.kumpelblase2.removeentities.exceptions.NoNameException;

public class EntityManager
{
	private static EntityManager m_instance;
	private Map<Integer, RemoteEntity> m_entities;
	private final Plugin m_plugin;
	
	public EntityManager(final Plugin inPlugin)
	{
		m_instance = this;
		this.m_plugin = inPlugin;
		this.m_entities = new HashMap<Integer, RemoteEntity>();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(inPlugin, new Runnable()
		{
			@Override
			public void run()
			{
				Iterator<Entry<Integer, RemoteEntity>> it = m_entities.entrySet().iterator();
				while(it.hasNext())
				{
					Entry<Integer, RemoteEntity> entry = it.next();
					entry.getValue().getHandle().z();
					if(entry.getValue().getHandle().dead)
					{
						entry.getValue().despawn();
						it.remove();
					}
				}
			}
		}, 1L, 1L);
		
		Bukkit.getPluginManager().registerEvents(new Listener()
		{
			@EventHandler
			public void disabled(PluginDisableEvent event)
			{
				if(event.getPlugin() == inPlugin)
					despawnAll();
			}
		}, inPlugin);
	}
	
	public static EntityManager getInstance()
	{
		return m_instance;
	}
	
	public Plugin getPlugin()
	{
		return this.m_plugin;
	}
	
	private Integer getNextFreeID()
	{
		Set<Integer> ids = this.m_entities.keySet();
		Integer current = 0;
		while(ids.contains(current))
		{
			current++;
		}
		return current;
	}
	
	public RemoteEntity createEntity(RemoteEntityType inType, Location inLocation) throws NoNameException
	{
		if(inType.isNamed())
			throw new NoNameException("Tried to spawn a named entity without name");

		Integer id = this.getNextFreeID();
		try
		{
			Constructor<? extends RemoteEntity> constructor = inType.getRemoteClass().getConstructor(int.class);
			RemoteEntity entity = constructor.newInstance(id);
			entity.spawn(inLocation);
			((RemoteEntityHandle)entity.getHandle()).setupStandardGoals();
			this.m_entities.put(id, entity);
			return entity;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public RemoteEntity createNamedEntity(RemoteEntityType inType, Location inLocation, String inName)
	{
		Integer id = this.getNextFreeID();
		try
		{
			Constructor<? extends RemoteEntity> constructor = inType.getRemoteClass().getConstructor(int.class, String.class);
			RemoteEntity entity = constructor.newInstance(id, inName);
			entity.spawn(inLocation);
			((RemoteEntityHandle)entity.getHandle()).setupStandardGoals();
			this.m_entities.put(id, entity);
			return entity;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void removeEntity(int inID)
	{
		if(this.m_entities.containsKey((Integer)inID))
			this.m_entities.get((Integer)inID).despawn();
		
		this.m_entities.remove((Integer)inID);
	}
	
	public boolean isRemoteEntity(LivingEntity inEntity)
	{
		EntityLiving handle = ((CraftLivingEntity)inEntity).getHandle();
		return handle instanceof RemoteEntityHandle;
	}
	
	public RemoteEntity getRemoteEntityFromEntity(LivingEntity inEntity)
	{
		if(!this.isRemoteEntity(inEntity))
			return null;
		
		EntityLiving entityHandle = ((CraftLivingEntity)inEntity).getHandle();
		return ((RemoteEntityHandle)entityHandle).getRemoteEntity();
	}
	
	public RemoteEntity getRemoteEntityByID(int inID)
	{
		return this.m_entities.get((Integer)inID);
	}
	
	public void addRemoteEntity(int inID, RemoteEntity inEntity)
	{
		this.m_entities.put(inID, inEntity);
	}
	
	public RemoteEntity createRemoteEntityFromExisting(LivingEntity inEntity) //TODO copy more shit
	{
		RemoteEntityType type = RemoteEntityType.getByEntityClass(((CraftLivingEntity)inEntity).getHandle().getClass());
		Location originalSpot = inEntity.getLocation();
		String name = (inEntity instanceof HumanEntity) ? ((HumanEntity)inEntity).getName() : null;
		inEntity.remove();
		try
		{
			if(name == null)
				return this.createEntity(type, originalSpot);
			else
				return this.createNamedEntity(type, originalSpot, name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}		
	}
	
	public void despawnAll()
	{
		for(RemoteEntity entity : this.m_entities.values())
		{
			entity.despawn();
		}
		this.m_entities.clear();
	}
}
