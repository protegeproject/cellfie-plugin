package org.mm.cellfie.ui.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.swing.JSplitPane;

import org.mm.app.MMApplication;
import org.mm.app.MMApplicationFactory;
import org.mm.app.MMApplicationModel;
import org.mm.core.MappingExpression;
import org.mm.core.MappingExpressionSet;
import org.mm.core.settings.ReferenceSettings;
import org.mm.parser.ASTExpression;
import org.mm.parser.MappingMasterParser;
import org.mm.parser.ParseException;
import org.mm.parser.SimpleNode;
import org.mm.parser.node.ExpressionNode;
import org.mm.renderer.Renderer;
import org.mm.renderer.RendererException;
import org.mm.renderer.text.TextRendererEx;
import org.mm.rendering.Rendering;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ui.DialogManager;
import org.mm.ui.ModelView;
import org.protege.editor.core.ui.split.ViewSplitPane;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * This is the main Mapping Master user interface. It contains a view of a
 * spreadsheet and a control area to edit and execute Mapping Master
 * expressions.
 */
public class ApplicationView extends ViewSplitPane implements ModelView
{
	private static final long serialVersionUID = 1L;

	private OWLEditorKit editorKit;

	private DialogManager applicationDialogManager;
	private DataSourceView dataSourceView;
	private MappingBrowserView mappingExpressionView;

	private MMApplication application;
	private MMApplicationFactory applicationFactory = new MMApplicationFactory();

	private ReferenceSettings referenceSettings = new ReferenceSettings();

	public ApplicationView(OWLOntology ontology, OWLEditorKit editorKit, DialogManager applicationDialogManager)
	{
		super(JSplitPane.VERTICAL_SPLIT);
		
		setUserOntology(ontology);
		
		this.editorKit = editorKit;
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
		prepareLogFileLocation();
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

	private MMApplicationModel getApplicationModel()
	{
		return application.getApplicationModel();
	}

	public void evaluate(MappingExpression mapping, Renderer renderer, Set<Rendering> results) throws ParseException
	{
		String expression = mapping.getExpressionString();
		ExpressionNode expressionNode = parseExpression(expression, referenceSettings);
		results.add(renderer.renderExpression(expressionNode).get());
	}

	public void evaluate(MappingExpression mapping, Renderer renderer, Set<Rendering> results, StringBuffer logMessage) throws ParseException
	{
		String expression = mapping.getExpressionString();
		ExpressionNode expressionNode = parseExpression(expression, referenceSettings);
		results.add(renderer.renderExpression(expressionNode).get());
		log(expressionNode, logMessage);
	}

	private void log(ExpressionNode expressionNode, StringBuffer logMessage)
	{
		try {
			TextRendererEx renderer = getApplicationModel().getLogRenderer();
			String output = renderer.renderExpression(expressionNode).get().getRendering();
			logMessage.append(output);
		}
		catch (RendererException e) {
			e.printStackTrace();
		}
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

	public SpreadSheetDataSource getLoadedSpreadSheet()
	{
		return getApplicationModel().getDataSourceModel().getDataSource();
	}

	public List<MappingExpression> getLoadedMappingExpressions()
	{
		return getApplicationModel().getMappingExpressionsModel().getExpressions();
	}

	public void updateMappingExpressionModel(List<MappingExpression> mappingList)
	{
		MappingExpressionSet mappings = MappingExpressionSet.create(mappingList);
		getApplicationModel().getMappingExpressionsModel().changeMappingExpressionSet(mappings);
	}

	public Renderer getDefaultRenderer()
	{
		return getApplicationModel().getDefaultRenderer();
	}

	public OWLEditorKit getEditorKit()
	{
		return editorKit;
	}

	public DialogManager getApplicationDialogManager()
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
	
	private DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
	
	private String logFileLocation;
	private File logFile;
	
	/* protected */File getLogFile()
	{
		return logFile;
	}

	/* protected */File createLogFile()
	{
		if (logFileLocation == null) {
			logFileLocation = getDefaultLogFileLocation();
		}
		String timestamp = dateFormat.format(new Date());
		String fileName = String.format("%s%s.log", logFileLocation, timestamp);
		logFile = new File(fileName);
		return logFile;
	}

	private void prepareLogFileLocation()
	{
		String mappingFilePath = applicationFactory.getMappingLocation();
		if (mappingFilePath == null) {
			logFileLocation = getDefaultLogFileLocation();
		} else {
			logFileLocation = getLogFileLocation(new File(mappingFilePath));
		}
	}

	private String getLogFileLocation(File mappingFile)
	{
		String mappingPath = mappingFile.getParent();
		String mappingFileName = mappingFile.getName().substring(0, mappingFile.getName().lastIndexOf("."));
		return mappingPath + System.getProperty("file.separator") + mappingFileName + "_mmexec";
	}

	private String getDefaultLogFileLocation()
	{
		String tmpPath = System.getProperty("java.io.tmpdir");
		return tmpPath + System.getProperty("file.separator") + "mmexec";
	}
}
