package org.mm.cellfie.ui.model;

import org.mm.core.MappingExpressionSet;
import org.mm.renderer.Renderer;
import org.mm.renderer.owlapi.OWLAPIRenderer;
import org.mm.renderer.text.TextRenderer;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ui.MMModel;
import org.semanticweb.owlapi.model.OWLOntology;

public class ApplicationModel implements MMModel
{
	private OWLOntology ontology;
	private SpreadSheetDataSource dataSource;

	private DataSourceModel dataSourceModel;
	private MappingExpressionModel expressionMappingsModel;

	public ApplicationModel(OWLOntology ontology, SpreadSheetDataSource dataSource, MappingExpressionSet mappings)
	{
		this.ontology = ontology;
		this.dataSource = dataSource;
		dataSourceModel = new DataSourceModel(dataSource);
		expressionMappingsModel = new MappingExpressionModel(mappings);
	}

	public OWLOntology getOntology()
	{
		return ontology;
	}

	public DataSourceModel getDataSourceModel()
	{
		return dataSourceModel;
	}

	public MappingExpressionModel getMappingExpressionsModel()
	{
		return expressionMappingsModel;
	}

	public Renderer getDefaultRenderer()
	{
		return getOWLAPIRenderer();
	}

	public OWLAPIRenderer getOWLAPIRenderer()
	{
		return new OWLAPIRenderer(ontology, dataSource);
	}

	public TextRenderer getTextRenderer()
	{
		return new TextRenderer(dataSource);
	}
}
