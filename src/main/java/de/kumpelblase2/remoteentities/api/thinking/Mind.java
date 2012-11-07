package de.kumpelblase2.remoteentities.api.thinking;

import java.util.*;
import de.kumpelblase2.remoteentities.api.RemoteEntity;

public class Mind
{
	private Map<String, Behavior> m_behaviours;
	private RemoteEntity m_entity;
	private boolean m_canFeel = true;
	private Navigation m_targetNavigation;
	private Navigation m_movementNavigation;
	
	public Mind(RemoteEntity inEntity)
	{
		this.m_entity = inEntity;
		this.m_behaviours = new HashMap<String, Behavior>();
		this.m_targetNavigation = new Navigation();
		this.m_movementNavigation = new Navigation();
	}
	
	public void addBehaviour(Behavior inBehaviour)
	{
		this.m_behaviours.put(inBehaviour.getName(), inBehaviour);
	}
	
	public boolean removeBehaviour(String inName)
	{
		return this.m_behaviours.remove(inName) != null;
	}
	
	public boolean hasBehaviour(String inName)
	{
		return this.m_behaviours.containsKey(inName);
	}
	
	public boolean canFeel()
	{
		return this.m_canFeel;
	}
	
	public void blockFeelings(boolean inState)
	{
		this.m_canFeel = inState;
	}
	
	public RemoteEntity getEntity()
	{
		return this.m_entity;
	}
	
	public Behavior getBehaviour(String inName)
	{
		return this.m_behaviours.get(inName);
	}
	
	public Collection<Behavior> getBehaviours()
	{
		return this.m_behaviours.values();
	}

	public void clearBehaviours()
	{
		this.m_behaviours.clear();
	}
	
	public List<Desire> getMovementDesires()
	{
		return this.m_movementNavigation.getDesires();
	}
	
	public int getHighestMovementPriority()
	{
		return this.m_movementNavigation.getHighestPriority();
	}
	
	public int getHighestActionPriority()
	{
		return this.m_targetNavigation.getHighestPriority();
	}
	
	public List<Desire> getActionDesires()
	{
		return this.m_targetNavigation.getDesires();
	}
	
	public<T extends Desire> T getMovementDesire(Class<T> inClass)
	{
		for(Desire desire : this.getMovementDesires())
		{
			if(desire.getClass().equals(inClass) || desire.getClass().getSuperclass().equals(inClass))
				return inClass.cast(desire);
		}
		return null;
	}
	
	public<T extends Desire> T getActionDesire(Class<T> inClass)
	{
		for(Desire desire : this.getActionDesires())
		{
			if(desire.getClass().equals(inClass) || desire.getClass().getSuperclass().equals(inClass))
				return inClass.cast(desire);
		}
		return null;
	}
	
	public void addMovementDesire(Desire inDesire, int inPriority)
	{
		this.m_movementNavigation.addDesire(inDesire, inPriority);
	}
	
	public void addActionDesire(Desire inDesire, int inPriority)
	{
		this.m_targetNavigation.addDesire(inDesire, inPriority);
	}
	
	public void clearMovementDesires()
	{
		this.m_movementNavigation.clearDesires();
	}
	
	public void clearActionDesires()
	{
		this.m_targetNavigation.clearDesires();
	}
	
	public boolean removeMovementDesire(Class<? extends Desire> inToRemove)
	{
		return this.m_targetNavigation.removeDesireByType(inToRemove);
	}
	
	public void removeMovementDesires(Class<? extends Desire> inToRemove)
	{
		while(this.removeMovementDesire(inToRemove)){}
	}
	
	public boolean removeActionDesire(Class<? extends Desire> inToRemove)
	{
		return this.m_movementNavigation.removeDesireByType(inToRemove);
	}
	
	public void removeActionDesires(Class<? extends Desire> inToRemove)
	{
		while(this.removeActionDesire(inToRemove)){}
	}
	
	public void tick()
	{
		if(this.canFeel())
		{
			for(Behavior behaviour : this.m_behaviours.values())
			{
				behaviour.run();
			}
		}
		this.m_movementNavigation.onUpdate();
		this.m_targetNavigation.onUpdate();
	}
}