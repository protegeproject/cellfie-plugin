package org.mm.cellfie.ui.view;

import java.io.ByteArrayInputStream;
import java.util.Set;

import javax.swing.JSplitPane;

import org.mm.cellfie.ui.MMApplication;
import org.mm.cellfie.ui.MMApplicationFactory;
import org.mm.cellfie.ui.dialog.MMDialogManager;
import org.mm.cellfie.ui.model.ApplicationModel;
import org.mm.core.MappingExpression;
import org.mm.core.MappingExpressionSet;
import org.mm.core.settings.ReferenceSettings;
import org.mm.parser.ASTExpression;
import org.mm.parser.MappingMasterParser;
import org.mm.parser.ParseException;
import org.mm.parser.SimpleNode;
import org.mm.parser.node.ExpressionNode;
import org.mm.renderer.Renderer;
import org.mm.rendering.Rendering;
import org.protege.editor.core.ui.split.ViewSplitPane;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * This is the main Mapping Master user interface. It contains a view of a
 * spreadsheet and a control area to edit and execute Mapping Master
 * expressions.
 */
public class ApplicationView extends ViewSplitPane implements MMView
{
	private static final long serialVersionUID = 1L;

	private MMDialogManager applicationDialogManager;
	private DataSourceView dataSourceView;
	private MappingBrowserView mappingExpressionView;

	private MMApplication application;
	private MMApplicationFactory applicationFactory = new MMApplicationFactory();

	private ReferenceSettings referenceSettings = new ReferenceSettings();

	public ApplicationView(OWLOntology ontology, OWLEditorKit editorKit, MMDialogManager applicationDialogManager)
	{
		super(JSplitPane.VERTICAL_SPLIT);
		
		setUserOntology(ontology);
		this.applicationDialogManager = applicationDialogManager;

		setDividerLocation(500);
		setResizeWeight(0.8);

		/*
		 * Workbook sheet GUI presentation
		 */
		dataSourceView = new DataSourceView(this);
		setTopComponent(dataSourceView);

		/*
		 * Mapping browser, create, edit, remove panel
		 */
		mappingExpressionView = new MappingBrowserView(this);
		setBottomComponent(mappingExpressionView);
		
		validate();
	}

	protected void setUserOntology(OWLOntology ontology)
	{
		if (ontology != null) {
			applicationFactory.setUserOntology(ontology);
		}
	}

	public void updateOntologyDocument(String path)
	{
		applicationFactory.setOntologyLocation(path);
		fireApplicationResourceChanged();
	}

	public void loadWorkbookDocument(String path)
	{
		applicationFactory.setWorkbookLocation(path);
		fireApplicationResourceChanged();

		updateDataSourceView();
		updateMappingBrowserView();
	}

	public void loadMappingDocument(String path)
	{
		applicationFactory.setMappingLocation(path);
		fireApplicationResourceChanged();
		
		updateMappingBrowserView();
	}

	private void fireApplicationResourceChanged()
	{
		setupApplication();
	}

	private void updateDataSourceView()
	{
		dataSourceView.update();
	}

	private void updateMappingBrowserView()
	{
		mappingExpressionView.update();
	}

	private void setupApplication()
	{
		try {
			application = applicationFactory.createApplication();
		} catch (Exception e) {
			applicationDialogManager.showErrorMessageDialog(this, "Initialization error: " + e.getMessage());
		}
	}

	public ApplicationModel getApplicationModel()
	{
		return application.getApplicationModel();
	}

	public void evaluate(MappingExpression mapping, Renderer renderer, Set<Rendering> results) throws ParseException
	{
		String expression = mapping.getExpressionString();
		ExpressionNode expressionNode = parseExpression(expression, referenceSettings);
		results.add(renderer.renderExpression(expressionNode).get());
	}

	private ExpressionNode parseExpression(String expression, ReferenceSettings settings) throws ParseException
	{
		MappingMasterParser parser = new MappingMasterParser(new ByteArrayInputStream(expression.getBytes()), settings, -1);
		SimpleNode simpleNode = parser.expression();
		return new ExpressionNode((ASTExpression) simpleNode);
	}

	@Override
	public void update()
	{
		// NO-OP
	}

	public void updateMappingExpressionModel(MappingExpressionSet mappings)
	{
		getApplicationModel().getMappingExpressionsModel().changeMappingExpressionSet(mappings);
	}

	public Renderer getDefaultRenderer()
	{
		return getApplicationModel().getDefaultRenderer();
	}

	public MMDialogManager getApplicationDialogManager()
	{
		return applicationDialogManager;
	}

	public ReferenceSettings getReferenceSettings()
	{
		return referenceSettings;
	}

	public DataSourceView getDataSourceView()
	{
		return dataSourceView;
	}

	public MappingBrowserView getMappingBrowserView()
	{
		return mappingExpressionView;
	}
}
