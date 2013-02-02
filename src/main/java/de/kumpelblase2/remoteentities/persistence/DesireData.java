package de.kumpelblase2.remoteentities.persistence;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ClassUtils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.thinking.Desire;
import de.kumpelblase2.remoteentities.api.thinking.DesireItem;

public class DesireData implements ConfigurationSerializable
{
	public String type;
	public ParameterData[] parameters;
	public int priority;
	
	public DesireData()
	{
	}
	
	public DesireData(Desire inDesire, int inPriotity)
	{
		this.type = inDesire.getClass().getName();
		this.parameters = inDesire.getSerializeableData();
		this.priority = inPriotity;
	}
	
	public DesireData(DesireItem item)
	{
		this(item.getDesire(), item.getPriority());
	}
	
	@SuppressWarnings("unchecked")
	public DesireData(Map<String, Object> inData)
	{
		this.type = (String)inData.get("type");
		List<Map<String, Object>> parameterData = (List<Map<String, Object>>)inData.get("parameters");
		this.parameters = new ParameterData[parameterData.size()];
		for(Map<String, Object> param : parameterData)
		{
			ParameterData paramData = new ParameterData(param);
			this.parameters[paramData.pos] = paramData;
		}
		this.priority = (Integer)inData.get("priority");
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("type", this.type);
		List<Map<String, Object>> parameterData = new ArrayList<Map<String, Object>>();
		for(ParameterData param : this.parameters)
		{
			parameterData.add(param.serialize());
		}
		data.put("parameters", parameterData);
		data.put("priority", this.priority);
		return data;
	}
	
	@SuppressWarnings("unchecked")
	public DesireItem create(RemoteEntity inEntity)
	{
		try
		{
			Class<? extends Desire> c = (Class<? extends Desire>)Class.forName(this.type);
			Constructor<? extends Desire> con = c.getConstructor(this.getParameterClasses());
			if(con == null)
				return null;
			
			Object[] values = new Object[this.parameters.length];
			int pos = 0;
			for(ParameterData data : this.parameters)
			{
				if(data.special.equals("entity"))
					values[pos] = (RemoteEntity)inEntity;
				else if(data.special.equals("manager"))
					values[pos] = inEntity.getManager();
				else				
					values[pos] = EntityData.objectParser.deserialize(data);
			}
			Desire d = con.newInstance(values);
			return new DesireItem(d, this.priority);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Class[] getParameterClasses()
	{
		Class[] classes = new Class[this.parameters.length];
		for(int i = 0; i < classes.length; i++)
		{
			try
			{
				Class c = ClassUtils.getClass(this.getClass().getClassLoader(), this.parameters[i].type);
				if(ClassUtils.wrapperToPrimitive(c) != null)
					c = ClassUtils.wrapperToPrimitive(c);
				
				classes[i] = c;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				continue;
			}
		}
		return classes;
	}
}
