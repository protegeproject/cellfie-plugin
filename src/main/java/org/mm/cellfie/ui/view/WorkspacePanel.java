package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import org.mm.app.MMApplication;
import org.mm.app.MMApplicationFactory;
import org.mm.app.MMApplicationModel;
import org.mm.core.OWLOntologySourceHook;
import org.mm.core.TransformationRule;
import org.mm.core.TransformationRuleSet;
import org.mm.core.settings.ReferenceSettings;
import org.mm.core.settings.ValueEncodingSetting;
import org.mm.parser.ASTExpression;
import org.mm.parser.MappingMasterParser;
import org.mm.parser.ParseException;
import org.mm.parser.SimpleNode;
import org.mm.parser.node.ExpressionNode;
import org.mm.parser.node.MMExpressionNode;
import org.mm.renderer.Renderer;
import org.mm.rendering.Rendering;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ui.DialogManager;
import org.protege.editor.core.ui.split.ViewSplitPane;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.base.Optional;

/**
 * This is the main Mapping Master user interface. It contains a view of a
 * spreadsheet and a control area to edit and execute Mapping Master rules.
 */
public class WorkspacePanel extends JPanel
{
   private static final long serialVersionUID = 1L;

   private OWLOntology ontology;
   private OWLEditorKit editorKit;

   private DialogManager dialogHelper;
   private DataSourceView dataSourceView;
   private TransformationRuleBrowserView transformationRuleBrowserView;

   private RenderLogging renderLogging;

   private MMApplication application;
   private MMApplicationFactory applicationFactory = new MMApplicationFactory();

   public WorkspacePanel(OWLOntology ontology, String workbookFilePath, OWLEditorKit editorKit, DialogManager dialogHelper)
   {
      this.ontology = ontology;
      this.editorKit = editorKit;
      this.dialogHelper = dialogHelper;

      setLayout(new BorderLayout());

      JPanel pnlTargetOntology = new JPanel(new FlowLayout(FlowLayout.LEFT));
      pnlTargetOntology.setBorder(new EmptyBorder(5, 5, 0, 5));
      add(pnlTargetOntology, BorderLayout.NORTH);

      JLabel lblTargetOntology = new JLabel("Target Ontology: ");
      lblTargetOntology.setForeground(Color.DARK_GRAY);
      pnlTargetOntology.add(lblTargetOntology);

      JLabel lblOntologyID = new JLabel(getTitle(ontology));
      lblOntologyID.setForeground(Color.DARK_GRAY);
      pnlTargetOntology.add(lblOntologyID);

      ViewSplitPane splitPane = new ViewSplitPane(JSplitPane.VERTICAL_SPLIT);
      splitPane.setResizeWeight(0.4);
      add(splitPane, BorderLayout.CENTER);

      loadWorkbookDocument(workbookFilePath);
//      loadTransformationRuleDocument(ruleFilePath) // XXX In case the UI will allow users to input rule file in advance
      setupApplication();

      /*
       * Workbook sheet GUI presentation
       */
      dataSourceView = new DataSourceView(this);
      splitPane.setTopComponent(dataSourceView);

      /*
       * Transformation rule browser, create, edit, remove panel
       */
      transformationRuleBrowserView = new TransformationRuleBrowserView(this);
      splitPane.setBottomComponent(transformationRuleBrowserView);

      validate();
   }

   private String getTitle(OWLOntology ontology)
   {
      Optional<IRI> ontologyID = ontology.getOntologyID().getOntologyIRI();
      if (ontologyID.isPresent()) {
         IRI id = ontologyID.get();
         return String.format("%s (%s)", id.getRemainder().get(), id);
      }
      return "N/A";
   }

   private void loadWorkbookDocument(String path)
   {
      applicationFactory.setWorkbookFileLocation(path);
   }

   public String getWorkbookFileLocation()
   {
      return applicationFactory.getWorkbookFileLocation();
   }

   public void loadTransformationRuleDocument(String path)
   {
      setRuleFileLocation(path);
      setupApplication();
      transformationRuleBrowserView.update();
   }

   /**
    * Gets the directory location of the transformation rule file. This method returns <code>null</code>
    * if the location hasn't been defined by the users.
    *
    * @return the file path location.
    */
   public String getRuleFileLocation()
   {
      return applicationFactory.getRuleFileLocation();
   }

   public void setRuleFileLocation(String path)
   {
      applicationFactory.setRuleFileLocation(path);
   }

   private void setupApplication()
   {
      try {
         OWLOntologySourceHook ontologySourceHook = new OWLProtegeOntology(getEditorKit());
         application = applicationFactory.createApplication(ontologySourceHook);
      } catch (Exception e) {
         dialogHelper.showErrorMessageDialog(this, "Initialization error: " + e.getMessage());
      }
   }

   private MMApplicationModel getApplicationModel()
   {
      return application.getApplicationModel();
   }

   public void initLogging()
   {
      renderLogging = new RenderLogging();
      renderLogging.init();
   }

   public RenderLogging getRenderLogging()
   {
      return renderLogging;
   }

   public void evaluate(TransformationRule rule, Renderer renderer, Set<Rendering> results) throws ParseException
   {
      final ReferenceSettings referenceSettings = new ReferenceSettings();

      String ruleString = rule.getRuleString();
      MMExpressionNode ruleNode = parseRule(ruleString, referenceSettings).getMMExpressionNode();
      java.util.Optional<? extends Rendering> renderingResult = renderer.render(ruleNode);
      if (renderingResult.isPresent()) {
         results.add(renderingResult.get());
      }
   }

   public void log(TransformationRule rule, Renderer renderer, RenderLogging logging) throws ParseException
   {
      final ReferenceSettings referenceSettings = new ReferenceSettings();
      referenceSettings.setValueEncodingSetting(ValueEncodingSetting.RDFS_LABEL);

      String ruleString = rule.getRuleString();
      MMExpressionNode ruleNode = parseRule(ruleString, referenceSettings).getMMExpressionNode();
      java.util.Optional<? extends Rendering> renderingResult = renderer.render(ruleNode);
      if (renderingResult.isPresent()) {
         logging.append(renderingResult.get().getRendering());
      }
   }

   private ExpressionNode parseRule(String ruleString, ReferenceSettings settings) throws ParseException
   {
      MappingMasterParser parser = new MappingMasterParser(new ByteArrayInputStream(ruleString.getBytes()), settings, -1);
      SimpleNode simpleNode = parser.expression();
      return new ExpressionNode((ASTExpression) simpleNode);
   }

   public OWLOntology getActiveOntology()
   {
      return ontology;
   }

   public SpreadSheetDataSource getActiveWorkbook()
   {
      return getApplicationModel().getDataSourceModel().getDataSource();
   }

   public List<TransformationRule> getActiveTransformationRules()
   {
      return getApplicationModel().getTransformationRuleModel().getRules();
   }

   /* package */ void updateTransformationRuleModel()
   {
      final List<TransformationRule> rules = getTransformationRuleBrowserView().getTransformationRules();
      TransformationRuleSet ruleSet = TransformationRuleSet.create(rules);
      getApplicationModel().getTransformationRuleModel().changeTransformationRuleSet(ruleSet);
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
      return dialogHelper;
   }

   public DataSourceView getDataSourceView()
   {
      return dataSourceView;
   }

   public TransformationRuleBrowserView getTransformationRuleBrowserView()
   {
      return transformationRuleBrowserView;
   }

   public static JDialog createDialog(OWLOntology ontology, String workbookPath, OWLEditorKit editorKit, DialogManager dialogHelper)
   {
      final JDialog dialog = new JDialog(null, "Cellfie", Dialog.ModalityType.MODELESS);
      
      final WorkspacePanel workspacePanel = new WorkspacePanel(ontology, workbookPath, editorKit, dialogHelper);
      workspacePanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CLOSE_DIALOG");
      workspacePanel.getActionMap().put("CLOSE_DIALOG", new AbstractAction() // Closing Cellfie using ESC key
      {
         private static final long serialVersionUID = 1L;
         
         @Override
         public void actionPerformed(ActionEvent e)
         {
            int answer = dialogHelper.showConfirmDialog(null, "Confirm Exit", "Exit Cellfie?");
            switch (answer) {
               case JOptionPane.YES_OPTION:
                  if (workspacePanel.close()) {
                     dialog.setVisible(false);
                  }
            }
         }
      });
      dialog.addWindowListener(new WindowAdapter() // Closing Cellfie using close [x] button
      {
         @Override
         public void windowClosing(WindowEvent e)
         {
            if (workspacePanel.close()) {
               dialog.setVisible(false);
            }
         }
      });
      dialog.setContentPane(workspacePanel);
      dialog.setSize(1200, 900);
      dialog.setResizable(true);
      return dialog;
   }

   protected boolean close()
   {
      return transformationRuleBrowserView.safeGuardChanges();
   }

   class RenderLogging
   {
      private DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");

      private String logFileLocation;
      private StringBuffer logMessage = new StringBuffer();
      private File logFile;

      private final Logger LOG = Logger.getLogger(RenderLogging.class.getName()); 

      public void init()
      {
         clearLog();
         
         String ruleFilePath = applicationFactory.getRuleFileLocation();
         if (ruleFilePath == null) {
            logFileLocation = getDefaultLogFileLocation();
         } else {
            logFileLocation = getLogFileLocation(new File(ruleFilePath));
         }
         String timestamp = dateFormat.format(new Date());
         String fileName = String.format("%s%s.log", logFileLocation, timestamp);
         logFile = new File(fileName);
         
         LOG.info("Cellfie log file: " + fileName);
      }

      public RenderLogging append(String message)
      {
         logMessage.append(message);
         return this;
      }

      public void save() throws FileNotFoundException
      {
         PrintWriter printer = new PrintWriter(logFile);
         printer.print(flushLog());
         printer.close();
         clearLog();
      }

      public InputStreamReader load() throws FileNotFoundException
      {
         return new FileReader(logFile);
      }

      private String getLogFileLocation(File ruleFile)
      {
         String ruleFilePath = ruleFile.getParent();
         String ruleFileName = ruleFile.getName().substring(0, ruleFile.getName().lastIndexOf("."));
         return ruleFilePath + System.getProperty("file.separator") + ruleFileName + "_mmexec";
      }

      private String getDefaultLogFileLocation()
      {
         String tmpPath = System.getProperty("java.io.tmpdir");
         if (tmpPath.endsWith(System.getProperty("file.separator"))) {
            return tmpPath + "mmexec";
         } else {
            return tmpPath + System.getProperty("file.separator") + "mmexec";
         }
      }

      private void clearLog()
      {
         logMessage.setLength(0);
      }

      private String flushLog()
      {
         return logMessage.toString();
      }
   }
}
