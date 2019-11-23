package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.mm.cellfie.transformationrule.TransformationRule;
import org.mm.cellfie.transformationrule.TransformationRuleList;
import org.mm.cellfie.transformationrule.TransformationRuleReader;
import org.mm.cellfie.transformationrule.TransformationRuleWriter;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleBrowserView extends JPanel implements TableModelListener {

   private static final Logger logger = LoggerFactory.getLogger(TransformationRuleBrowserView.class);

   private static final long serialVersionUID = 1L;

   private final CellfieWorkspace cellfieWorkspace;

   private JPanel pnlContainer;

   private JButton cmdAdd;
   private JButton cmdEdit;
   private JButton cmdDelete;
   private JButton cmdSave;
   private JButton cmdSaveAs;

   private TransformationRuleTableModel transformationRuleModel;
   private TransformationRuleTable tblTransformationRules;

   private boolean hasUnsavedChanges = false;

   private File ruleFile;

   public TransformationRuleBrowserView(@Nonnull CellfieWorkspace cellfieWorkspace) {
      checkNotNull(cellfieWorkspace);
      this.cellfieWorkspace = cellfieWorkspace;

      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      pnlContainer = new JPanel();
      pnlContainer.setLayout(new BorderLayout());
      pnlContainer.setBorder(ComponentFactory.createTitledBorder("Transformation Rules"));

      add(pnlContainer, BorderLayout.CENTER);

      transformationRuleModel = new TransformationRuleTableModel();
      transformationRuleModel.addTableModelListener(this);

      tblTransformationRules = new TransformationRuleTable(transformationRuleModel);
      tblTransformationRules.addMouseListener(new RuleEditMouseListener());
      tblTransformationRules.addMouseListener(new RuleSelectionMouseListener());

      tblTransformationRules.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "ADD_RULE");
      tblTransformationRules.getActionMap().put("ADD_RULE", new AddRuleKeyAction());

      tblTransformationRules.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "EDIT_RULE");
      tblTransformationRules.getActionMap().put("EDIT_RULE", new EditRuleKeyAction());

      tblTransformationRules.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_RULE");
      tblTransformationRules.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE_RULE");
      tblTransformationRules.getActionMap().put("DELETE_RULE", new DeleteRuleKeyAction());

      JScrollPane scrMappingExpression = new JScrollPane(tblTransformationRules);

      JPanel pnlAddEditDelete = new JPanel(new BorderLayout());
      pnlAddEditDelete.setBorder(new EmptyBorder(2, 5, 7, 5));
      pnlContainer.add(pnlAddEditDelete, BorderLayout.NORTH);

      JPanel pnlCommandButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pnlAddEditDelete.add(pnlCommandButton, BorderLayout.WEST);

      cmdAdd = new JButton("Add");
      cmdAdd.setPreferredSize(new Dimension(72, 22));
      cmdAdd.addActionListener(new AddRuleButtonAction());
      pnlCommandButton.add(cmdAdd);

      cmdEdit = new JButton("Edit");
      cmdEdit.setPreferredSize(new Dimension(72, 22));
      cmdEdit.setEnabled(false);
      cmdEdit.addActionListener(new EditRuleButtonAction());
      pnlCommandButton.add(cmdEdit);

      cmdDelete = new JButton("Delete");
      cmdDelete.setPreferredSize(new Dimension(72, 22));
      cmdDelete.setEnabled(false);
      cmdDelete.addActionListener(new DeleteRuleButtonAction());
      pnlCommandButton.add(cmdDelete);

      JPanel pnlExpressionBrowser = new JPanel(new BorderLayout());
      pnlContainer.add(pnlExpressionBrowser, BorderLayout.CENTER);

      pnlExpressionBrowser.add(scrMappingExpression, BorderLayout.CENTER);

      JPanel pnlMappingOpenSave = new JPanel(new FlowLayout(FlowLayout.CENTER));
      pnlContainer.add(pnlMappingOpenSave, BorderLayout.SOUTH);

      JButton cmdLoad = new JButton("Load Rules");
      cmdLoad.setPreferredSize(new Dimension(122, 22));
      cmdLoad.addActionListener(new LoadRulesAction());
      pnlMappingOpenSave.add(cmdLoad);

      cmdSave = new JButton("Save Rules");
      cmdSave.setPreferredSize(new Dimension(122, 22));
      cmdSave.addActionListener(new SaveRulesAction());
      cmdSave.setEnabled(true);
      pnlMappingOpenSave.add(cmdSave);

      cmdSaveAs = new JButton("Save As...");
      cmdSaveAs.setPreferredSize(new Dimension(122, 22));
      cmdSaveAs.addActionListener(new SaveAsAction());
      cmdSaveAs.setEnabled(true);
      pnlMappingOpenSave.add(cmdSaveAs);

      validate();
   }

   private boolean isFilePresent() {
      return ruleFile != null;
   }

   public boolean hasTransformationRules() {
      return tblTransformationRules.getRowCount() > 0;
   }

   public TransformationRuleList getAllRules() {
      return tblTransformationRules.getAllRules();
   }

   public TransformationRuleList getPickedRules() {
      return tblTransformationRules.getPickedRules();
   }

   /*
    * Mouse listener to detect editing actions using mouse clicks
    */
   private class RuleEditMouseListener extends MouseAdapter {
      @Override
      public void mouseClicked(MouseEvent e) {
         if (isSingleClicked(e)) {
            enableEditAndDeleteButtons();
         } else if (isDoubleClicked(e)) {
            editTransformationRule();
         }
      }

      private void enableEditAndDeleteButtons() {
         cmdEdit.setEnabled(true);
         cmdDelete.setEnabled(true);
      }

      private boolean isSingleClicked(MouseEvent e) {
         return e.getClickCount() == 1;
      }

      private boolean isDoubleClicked(MouseEvent e) {
         return e.getClickCount() == 2;
      }
   }

   /*
    * Mouse listener to detect rule selection using the rule select column
    */
   private class RuleSelectionMouseListener extends MouseAdapter {
      @Override
      public void mouseClicked(MouseEvent e) {
         if (isRuleSelectionEvent(e)) {
            int selectedRow = tblTransformationRules.getSelectedRow();
            if (isUserDeselecting(selectedRow)) {
               tblTransformationRules.uncheckSelectAllRules();
            } else {
               // In the case of the user is selecting one transformation rule,
               // check if this action leads to all rules selected. If so, we
               // would like to have the check box header gets selected as well.
               if (isAllTransformationRulesSelected()) {
                  tblTransformationRules.checkSelectAllRules();
               }
            }
            repaint();
         }
      }

      private boolean isRuleSelectionEvent(MouseEvent event) {
         Point clickPoint = event.getPoint();
         int selectedColumn = tblTransformationRules.columnAtPoint(clickPoint);
         return selectedColumn == TransformationRuleTableModel.RULE_PICK_COLUMN;
      }

      private boolean isUserDeselecting(int rowIndex) {
         final TableModel tableModel = tblTransformationRules.getModel();
         boolean selectionValue = (Boolean) tableModel.getValueAt(rowIndex,
               TransformationRuleTableModel.RULE_PICK_COLUMN);
         return selectionValue == false;
      }

      private boolean isAllTransformationRulesSelected() {
         final TableModel tableModel = tblTransformationRules.getModel();
         for (int rowIndex = 0; rowIndex < tableModel.getRowCount(); rowIndex++) {
            if (isUserDeselecting(rowIndex)) {
               return false;
            }
         }
         return true;
      }
   }

   /*
    * Action listeners for adding a new rule.
    */

   @SuppressWarnings("serial")
   private class AddRuleKeyAction extends AbstractAction {
      @Override
      public void actionPerformed(ActionEvent e) {
         addTransformationRule();
      }
   }

   private class AddRuleButtonAction implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         addTransformationRule();
      }
   }

   private void addTransformationRule() {
      CellRange cellRange = cellfieWorkspace.getWorkbookView().getSelectedCellRange();
      TransformationRuleEditor ruleEditor = createRuleEditor(cellRange);
      int answer = showTransformationRuleEditorDialog(ruleEditor);
      if (answer == JOptionPane.OK_OPTION) {
         TransformationRule newRule = ruleEditor.getTransformationRule();
         tblTransformationRules.addRule(newRule);
         tblTransformationRules.requestFocusInWindow();
      }
   }

   private TransformationRuleEditor createRuleEditor(CellRange cellRange) {
      TransformationRuleEditor editorPanel = new TransformationRuleEditor(cellfieWorkspace);
      editorPanel.setSheetName(cellRange.getSheetName());
      editorPanel.setStartColumn(cellRange.getStartColumnName());
      editorPanel.setStartRow(cellRange.getStartRowNumber());
      editorPanel.setEndColumn(cellRange.getEndColumnName());
      editorPanel.setEndRow(cellRange.getEndRowNumber());
      return editorPanel;
   }

   /*
    * Action listeners for editing an existing rule.
    */

   @SuppressWarnings("serial")
   private class EditRuleKeyAction extends AbstractAction {
      @Override
      public void actionPerformed(ActionEvent e) {
         editTransformationRule();
      }
   }

   private class EditRuleButtonAction implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         editTransformationRule();
      }
   }

   private void editTransformationRule() {
      TransformationRule selectedRule = tblTransformationRules.getRuleAtSelection();
      TransformationRuleEditor ruleEditor = createRuleEditor(selectedRule);
      int answer = showTransformationRuleEditorDialog(ruleEditor);
      if (answer == JOptionPane.OK_OPTION) {
         TransformationRule modifiedRule = ruleEditor.getTransformationRule();
         tblTransformationRules.modifyRuleAtSelection(modifiedRule);
      }
   }

   private TransformationRuleEditor createRuleEditor(TransformationRule rule) {
      TransformationRuleEditor editorPanel = new TransformationRuleEditor(cellfieWorkspace);
      editorPanel.setSheetName(rule.getSheetName());
      editorPanel.setStartColumn(rule.getStartColumn());
      editorPanel.setStartRow(rule.getStartRow());
      editorPanel.setEndColumn(rule.getEndColumn());
      editorPanel.setEndRow(rule.getEndRow());
      editorPanel.setDescription(rule.getComment());
      editorPanel.setRuleExpression(rule.getRuleExpression());
      return editorPanel;
   }

   private int showTransformationRuleEditorDialog(TransformationRuleEditor editorPanel) {
      return DialogUtils.showDialog(cellfieWorkspace,
            "Transformation Rule Editor",
            editorPanel,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION);
   }

   /*
    * Action listeners for deleting a rule.
    */

   @SuppressWarnings("serial")
   private class DeleteRuleKeyAction extends AbstractAction {
      @Override
      public void actionPerformed(ActionEvent e) {
         removeTransformationRule();
      }
   }

   private class DeleteRuleButtonAction implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         removeTransformationRule();
      }
   }

   private void removeTransformationRule() {
      int answer = DialogUtils.showConfirmDialog(cellfieWorkspace,
            "Do you really want to delete the selected transformation rule?");
      if (answer == JOptionPane.YES_OPTION) {
         tblTransformationRules.removeRuleAtSelection();
      }
   }

   /*
    * Action listener used to perform a file load action.
    */
   private class LoadRulesAction implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent event) {
         boolean canProceed = safeGuardChanges();
         if (canProceed) {
            ruleFile = DialogUtils.showOpenFileChooser(cellfieWorkspace,
                  "Mapping Master Transformation Rules (.json)", "json");
            if (ruleFile != null) {
               try {
                  TransformationRuleList ruleList = TransformationRuleReader.readFromDocument(ruleFile);
                  tblTransformationRules.load(ruleList);
                  hasUnsavedChanges = false;
               } catch (Exception e) {
                  ErrorLogPanel.showErrorDialog(e);
                  logger.error(e.getMessage(), e);
               }
            }
         }
      }
   }

   @Override
   public void tableChanged(TableModelEvent e) {
      hasUnsavedChanges = true; // flag the rule model is dirty
      updateCommandButtonsUi();
   }

   /*
    * Action listener used to perform a save file action
    */
   private class SaveRulesAction implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         saveTable();
      }
   }

   /*
    * Action listener used to perform a save-as file action
    */
   private class SaveAsAction implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         saveAsFile();
      }
   }

   private void saveAsFile() {
      File chosenFile = DialogUtils.showSaveFileChooser(cellfieWorkspace,
            "Mapping Master Transformation Rule (.json)",
            "json");
      if (chosenFile != null) {
         String filePath = chosenFile.getAbsolutePath();
         ruleFile = (!filePath.endsWith(".json")) ? new File(filePath + ".json") : new File(filePath);
         saveFile(ruleFile);
      }
   }

   private void saveFile(File targetFile) {
      try {
         TransformationRuleWriter.writeToDocument(targetFile, getAllRules());
         DialogUtils.showInfoDialog(cellfieWorkspace, "Saving was successful");
         hasUnsavedChanges = false;
      } catch (IOException e) {
         ErrorLogPanel.showErrorDialog(e);
         logger.error(e.getMessage(), e);
      }
   }

   public boolean safeGuardChanges() {
      boolean canProceed = true;
      if (hasUnsavedChanges) {
         int answer = DialogUtils.showConfirmWithCancelDialog(cellfieWorkspace,
               "There are unsaved changes in your transformation rules. Do you want to save them?");
         if (answer == JOptionPane.YES_OPTION) {
            saveTable();
         } else if (answer == JOptionPane.NO_OPTION) {
            clearTable();
         } else if (answer == JOptionPane.CANCEL_OPTION) {
            canProceed = false;
         }
      }
      return canProceed;
   }

   private void saveTable() {
      if (!isFilePresent()) {
         saveAsFile();
      } else {
         saveFile(ruleFile);
      }
   }

   private void clearTable() {
      transformationRuleModel.removeAllRules();
   }

   private void updateCommandButtonsUi() {
      if (hasTransformationRules()) {
         cmdEdit.setEnabled(true);
         cmdDelete.setEnabled(true);
      } else {
         cmdEdit.setEnabled(false);
         cmdDelete.setEnabled(false);
      }
   }
}
