package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import org.mm.app.MMApplication;
import org.mm.app.MMApplicationFactory;
import org.mm.app.MMApplicationModel;
import org.mm.cellfie.ui.exception.CellfieException;
import org.mm.core.MappingExpression;
import org.mm.core.MappingExpressionSet;
import org.mm.core.settings.ReferenceSettings;
import org.mm.core.settings.ValueEncodingSetting;
import org.mm.parser.ASTExpression;
import org.mm.parser.MappingMasterParser;
import org.mm.parser.ParseException;
import org.mm.parser.SimpleNode;
import org.mm.parser.node.ExpressionNode;
import org.mm.renderer.Renderer;
import org.mm.rendering.Rendering;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ui.DialogManager;
import org.mm.ui.ModelView;
import org.protege.editor.core.ui.split.ViewSplitPane;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * This is the main Mapping Master user interface. It contains a view of a
 * spreadsheet and a control area to edit and execute Mapping Master
 * expressions.
 */
public class ApplicationView extends JPanel implements ModelView
{
	private static final long serialVersionUID = 1L;

	private OWLEditorKit editorKit;

	private DialogManager applicationDialogManager;
	private DataSourceView dataSourceView;
	private MappingBrowserView mappingExpressionView;

	private MMApplication application;
	private MMApplicationFactory applicationFactory = new MMApplicationFactory();

	public ApplicationView(OWLOntology ontology, OWLEditorKit editorKit, DialogManager applicationDialogManager)
	{
		setUserOntology(ontology);
		
		this.editorKit = editorKit;
		this.applicationDialogManager = applicationDialogManager;

		setLayout(new BorderLayout());
		
		JPanel pnlTargetOntology = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlTargetOntology.setBorder(new EmptyBorder(2, 5, 2, 5));
		add(pnlTargetOntology, BorderLayout.NORTH);

		JLabel lblTargetOntology = new JLabel("Target Ontology: ");
		lblTargetOntology.setForeground(Color.DARK_GRAY);
		pnlTargetOntology.add(lblTargetOntology);
		
		JLabel lblOntologyID = new JLabel(createName(ontology));
		lblOntologyID.setForeground(Color.DARK_GRAY);
		pnlTargetOntology.add(lblOntologyID);

		ViewSplitPane splitPane = new ViewSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(500);
		splitPane.setResizeWeight(0.8);
		add(splitPane, BorderLayout.CENTER);

		/*
		 * Workbook sheet GUI presentation
		 */
		dataSourceView = new DataSourceView(this);
		splitPane.setTopComponent(dataSourceView);

		/*
		 * Mapping browser, create, edit, remove panel
		 */
		mappingExpressionView = new MappingBrowserView(this);
		splitPane.setBottomComponent(mappingExpressionView);
		
		validate();
	}

	private String createName(OWLOntology ontology)
	{
		IRI ontologyID = ontology.getOntologyID().getOntologyIRI();
		return String.format("%s (%s)", ontologyID.getFragment(), ontologyID);
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
		fireOntologyDocumentChanged();
	}

	public void loadWorkbookDocument(String path)
	{
		applicationFactory.setWorkbookLocation(path);
		fireWorkbookDocumentChanged();
	}

	public void loadMappingDocument(String path)
	{
		applicationFactory.setMappingLocation(path);
		fireMappingDocumentChanged();
	}

	private void fireOntologyDocumentChanged()
	{
		setupApplication();
	}

	private void fireWorkbookDocumentChanged()
	{
		setupApplication();
		updateDataSourceView();
		updateMappingBrowserView();
	}

	private void fireMappingDocumentChanged()
	{
		setupApplication();
		prepareLogFileLocation();
		updateMappingBrowserView();
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
		final ReferenceSettings referenceSettings = new ReferenceSettings();
		
		String expression = mapping.getExpressionString();
		ExpressionNode expressionNode = parseExpression(expression, referenceSettings);
		results.add(renderer.renderExpression(expressionNode).get());
	}

	public void log(MappingExpression mapping, Renderer renderer, StringBuffer logMessage) throws ParseException
	{
		final ReferenceSettings referenceSettings = new ReferenceSettings();
		referenceSettings.setValueEncodingSetting(ValueEncodingSetting.RDFS_LABEL);
		
		String expression = mapping.getExpressionString();
		ExpressionNode expressionNode = parseExpression(expression, referenceSettings);
		logMessage.append(renderer.renderExpression(expressionNode).get());
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

	public SpreadSheetDataSource getLoadedSpreadSheet() throws CellfieException
	{
		try {
			SpreadSheetDataSource spreadsheet = getApplicationModel().getDataSourceModel().getDataSource();
			spreadsheet.getSheets(); // just to check NullPointerException
			return spreadsheet;
		} catch (NullPointerException ex) {
			throw new CellfieException("Unable to find the spreadsheet. Please load the source spreadsheet first and try again.", ex);
		}
	}

	public List<MappingExpression> getLoadedMappingExpressions() throws CellfieException
	{
		try {
			return getApplicationModel().getMappingExpressionsModel().getExpressions();
		} catch (NullPointerException ex) {
			throw new CellfieException("Unable to find the mapping expressions. Please load the expressions first and try again.", ex);
		}
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

	public Renderer getLogRenderer()
	{
		return getApplicationModel().getLogRenderer();
	}

	public OWLEditorKit getEditorKit()
	{
		return editorKit;
	}

	public DialogManager getApplicationDialogManager()
	{
		return applicationDialogManager;
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
