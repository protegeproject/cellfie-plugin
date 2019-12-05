package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import org.mm.cellfie.transformationrule.TransformationRule;

/**
 * Represents the editor form used to create a transformation rule.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleEditor extends JPanel {

   private static final long serialVersionUID = 1L;

   private JComboBox<String> cbbSheetName;

   private JComboBox<String> cmbStartColumn;
   private JComboBox<String> cmbEndColumn;
   private JComboBox<String> cmbStartRow;
   private JComboBox<String> cmbEndRow;
   private JTextArea txtDescription;

   private JTextArea txtRule;

   public TransformationRuleEditor(@Nonnull CellfieWorkspace cellfieWorkspace) {
      checkNotNull(cellfieWorkspace);
      
      setSize(350, 300);
      setLayout(new BorderLayout());

      JPanel pnlMain = new JPanel(new BorderLayout(0, 5));
      pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      add(pnlMain, BorderLayout.CENTER);

      JPanel pnlSheetSelector = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      JLabel lblSheetName = new JLabel("Sheet name:");
      lblSheetName.setPreferredSize(new Dimension(140, 22));
      List<String> sheetNames = cellfieWorkspace.getWorkbook().getSheetNames();
      cbbSheetName = new JComboBox<>(new DefaultComboBoxModel<>(new Vector<>(sheetNames)));
      cbbSheetName.setPreferredSize(new Dimension(300, 22));

      pnlSheetSelector.add(lblSheetName);
      pnlSheetSelector.add(cbbSheetName);

      JLabel lblUseColumnsFrom = new JLabel("Use columns from: ");
      lblUseColumnsFrom.setPreferredSize(new Dimension(140, 22));

      JPanel pnlColumnRange = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      final DefaultComboBoxModel<String> startColumnModel = new DefaultComboBoxModel<String>(
            new String[] {"A", "B", "C", "D", "E", "F",
                  "G", "H", "I", "J", "K", "L", "M",
                  "N", "O", "P", "Q", "R", "S", "T",
                  "U", "V", "W", "X", "Y", "Z"});
      cmbStartColumn = new JComboBox<String>(startColumnModel);
      cmbStartColumn.setSelectedIndex(0);
      cmbStartColumn.setEditable(true);
      cmbStartColumn.setPreferredSize(new Dimension(62, 22));

      JLabel lblColumnTo = new JLabel("to", SwingConstants.CENTER);
      lblColumnTo.setPreferredSize(new Dimension(50, 22));

      final DefaultComboBoxModel<String> endColumnModel = new DefaultComboBoxModel<String>(
            new String[] {"+", "A", "B", "C", "D", "E", "F",
                  "G", "H", "I", "J", "K", "L", "M",
                  "N", "O", "P", "Q", "R", "S", "T",
                  "U", "V", "W", "X", "Y", "Z"});
      cmbEndColumn = new JComboBox<String>(endColumnModel);
      cmbEndColumn.setSelectedIndex(1);
      cmbEndColumn.setEditable(true);
      cmbEndColumn.setPreferredSize(new Dimension(62, 22));

      pnlColumnRange.add(lblUseColumnsFrom);
      pnlColumnRange.add(cmbStartColumn);
      pnlColumnRange.add(lblColumnTo);
      pnlColumnRange.add(cmbEndColumn);

      JLabel lblUseRowsFrom = new JLabel("Use rows from: ");
      lblUseRowsFrom.setPreferredSize(new Dimension(140, 22));

      JPanel pnlRowRange = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      final DefaultComboBoxModel<String> startRowModel = new DefaultComboBoxModel<String>(
            new String[] {"1", "2", "3", "4", "5", "6",
                  "7", "8", "9", "10", "12", "13", "14",
                  "15", "16", "17", "18", "19", "20", "30",
                  "40", "50", "100", "200", "500"});
      cmbStartRow = new JComboBox<String>(startRowModel);
      cmbStartRow.setSelectedIndex(1);
      cmbStartRow.setEditable(true);
      cmbStartRow.setPreferredSize(new Dimension(62, 22));

      JLabel lblRowTo = new JLabel("to", SwingConstants.CENTER);
      lblRowTo.setPreferredSize(new Dimension(50, 22));

      final DefaultComboBoxModel<String> endRowModel = new DefaultComboBoxModel<String>(
            new String[] {"+", "1", "2", "3", "4", "5", "6",
                  "7", "8", "9", "10", "12", "13", "14",
                  "15", "16", "17", "18", "19", "20", "30",
                  "40", "50", "100", "200", "500"});
      cmbEndRow = new JComboBox<String>(endRowModel);
      cmbEndRow.setSelectedIndex(0);
      cmbEndRow.setEditable(true);
      cmbEndRow.setPreferredSize(new Dimension(62, 22));

      pnlRowRange.add(lblUseRowsFrom);
      pnlRowRange.add(cmbStartRow);
      pnlRowRange.add(lblRowTo);
      pnlRowRange.add(cmbEndRow);

      JPanel pnlTop = new JPanel(new BorderLayout(0, 5));
      pnlTop.add(pnlSheetSelector, BorderLayout.NORTH);
      pnlTop.add(pnlColumnRange, BorderLayout.CENTER);
      pnlTop.add(pnlRowRange, BorderLayout.SOUTH);

      JLabel lblRule = new JLabel("Rule:");
      txtRule = new JTextArea("", 18, 42);

      JPanel pnlCenter = new JPanel(new BorderLayout(0, 5));
      pnlCenter.add(lblRule, BorderLayout.NORTH);
      pnlCenter.add(txtRule, BorderLayout.CENTER);

      JLabel lblDescription = new JLabel("Description:");
      txtDescription = new JTextArea("", 5, 42);
      txtDescription.setLineWrap(true);
      txtDescription.setWrapStyleWord(true);
      JScrollPane scrDescription = new JScrollPane(txtDescription);

      JPanel pnlBottom = new JPanel(new BorderLayout(0, 5));
      pnlBottom.add(lblDescription, BorderLayout.NORTH);
      pnlBottom.add(scrDescription, BorderLayout.CENTER);

      pnlMain.add(pnlTop, BorderLayout.NORTH);
      pnlMain.add(pnlCenter, BorderLayout.CENTER);
      pnlMain.add(pnlBottom, BorderLayout.SOUTH);
      
      validate();
   }

   public void setSheetName(@Nonnull String sheetName) {
      checkNotNull(sheetName);
      cbbSheetName.setSelectedItem(sheetName);
   }

   public void setStartColumn(@Nonnull String columnName) {
      checkNotNull(columnName);
      if (columnName.isEmpty()) {
         columnName = TransformationRule.START_COLUMN;
      }
      cmbStartColumn.setSelectedItem(columnName);
   }

   public void setStartRow(@Nonnull String rowNumber) {
      checkNotNull(rowNumber);
      if (rowNumber.isEmpty()) {
         rowNumber = TransformationRule.START_ROW;
      }
      cmbStartRow.setSelectedItem(rowNumber);
   }

   public void setEndColumn(@Nonnull String columnName) {
      checkNotNull(columnName);
      if (columnName.isEmpty()) {
         columnName = TransformationRule.ANY_WILDCARD;
      }
      cmbEndColumn.setSelectedItem(columnName);
   }

   public void setEndRow(@Nonnull String rowNumber) {
      checkNotNull(rowNumber);
      if (rowNumber.isEmpty()) {
         rowNumber = TransformationRule.ANY_WILDCARD;
      }
      cmbEndRow.setSelectedItem(rowNumber);
   }

   public void setDescription(@Nonnull String comment) {
      checkNotNull(comment);
      txtDescription.setText(comment);
   }
   
   public void setRuleExpression(@Nonnull String ruleExpression) {
      checkNotNull(ruleExpression);
      txtRule.setText(ruleExpression);
   }

   public TransformationRule getTransformationRule() {
      return new TransformationRule(cbbSheetName.getSelectedItem().toString(),
            cmbStartColumn.getSelectedItem().toString().trim().toUpperCase(),
            cmbEndColumn.getSelectedItem().toString().trim().toUpperCase(),
            cmbStartRow.getSelectedItem().toString().trim(),
            cmbEndRow.getSelectedItem().toString().trim(),
            txtDescription.getText().trim(),
            txtRule.getText().trim());
   }
}
