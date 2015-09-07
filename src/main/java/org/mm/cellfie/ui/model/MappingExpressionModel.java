package org.mm.cellfie.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mm.core.MappingExpression;
import org.mm.core.MappingExpressionSet;
import org.mm.ui.MMModel;

public class MappingExpressionModel implements MMModel
{
	private MappingExpressionSet mappings;

	private List<MappingExpression> cache = new ArrayList<MappingExpression>();

	public MappingExpressionModel()
	{
		this(new MappingExpressionSet());
	}

	public MappingExpressionModel(MappingExpressionSet mappings)
	{
		changeMappingExpressionSet(mappings);
	}

	public void changeMappingExpressionSet(MappingExpressionSet mappings)
	{
		this.mappings = mappings;
		fireModelChanged();
	}

	private void fireModelChanged()
	{
		cache.clear(); // reset the cache
		for (MappingExpression mapping : mappings) {
			cache.add(mapping);
		}
	}

	public List<MappingExpression> getExpressions()
	{
		return Collections.unmodifiableList(cache);
	}

	public MappingExpression getExpression(int index)
	{
		return cache.get(index);
	}

	public boolean isEmpty()
	{
		return cache.isEmpty();
	}

	public boolean contains(MappingExpression mapping)
	{
		return cache.contains(mapping);
	}

	public void addMappingExpression(MappingExpression mapping)
	{
		cache.add(mapping);
	}

	public void removeMappingExpression(MappingExpression mapping)
	{
		cache.remove(mapping);
	}
}
