package de.kumpelblase2.remoteentities.api.thinking.goals;

import net.minecraft.server.v1_5_R2.Container;
import net.minecraft.server.v1_5_R2.EntityHuman;
import net.minecraft.server.v1_5_R2.EntityVillager;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.thinking.DesireBase;
import de.kumpelblase2.remoteentities.api.thinking.DesireType;
import de.kumpelblase2.remoteentities.exceptions.NotAVillagerException;

public class DesireTradeWithPlayer extends DesireBase
{
	protected EntityVillager m_villager;
	
	public DesireTradeWithPlayer(RemoteEntity inEntity) throws Exception
	{
		super(inEntity);
		if(!(this.getEntityHandle() instanceof EntityVillager))
			throw new NotAVillagerException();
		
		this.m_villager = (EntityVillager)this.getEntityHandle();
		this.m_type = DesireType.OCCASIONAL_URGE;
	}

	@Override
	public boolean shouldExecute()
	{
		if(this.getEntityHandle() == null)
			return false;
		
		if(!this.getEntityHandle().isAlive())
			return false;
		else if(this.getEntityHandle().G())
			return false;
		else if(!this.getEntityHandle().onGround)
			return false;
		else if(this.getEntityHandle().velocityChanged)
			return false;
		else
		{
			EntityHuman trader = this.m_villager.m_();
			if(trader == null)
				return false;
			
			if(this.m_villager.e(trader) > 16)
				return false;
			
			return trader.activeContainer instanceof Container;
		}
	}
	
	@Override
	public void startExecuting()
	{
		this.m_villager.getNavigation().g();
	}
	
	@Override
	public void stopExecuting()
	{
		this.m_villager.a((EntityHuman)null);
	}
}
