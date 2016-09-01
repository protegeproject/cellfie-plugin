package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.mm.core.TransformationRule;

public class TransformationRuleEditorPanel extends JPanel
{
   private static final long serialVersionUID = 1L;

   private JComboBox<String> cbbSheetName;

   private JTextField txtStartColumn;
   private JTextField txtEndColumn;
   private JTextField txtStartRow;
   private JTextField txtEndRow;
   private JTextField txtComment;

   private JTextArea txtRule;

   public TransformationRuleEditorPanel(List<String> sheetNames)
   {
      setLayout(new BorderLayout());

      JPanel pnlMain = new JPanel(new BorderLayout());
      pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      add(pnlMain, BorderLayout.CENTER);

      JLabel lblSheetName = new JLabel("Sheet name:");
      cbbSheetName = new JComboBox<>(new DefaultComboBoxModel<>(new Vector<>(sheetNames)));

      JLabel lblStartColumn = new JLabel("Start column:");
      txtStartColumn = new JTextField("");
      txtStartColumn.addFocusListener(new FocusAdapter() {
         @Override
         public void focusGained(FocusEvent evt) {
            SwingUtilities.invokeLater(() -> { txtStartColumn.selectAll(); });
         }
      });

      JLabel lblEndColumn = new JLabel("End column:");
      txtEndColumn = new JTextField("");
      txtEndColumn.addFocusListener(new FocusAdapter() {
         @Override
         public void focusGained(FocusEvent evt) {
            SwingUtilities.invokeLater(() -> { txtEndColumn.selectAll(); });
         }
      });

      JLabel lblStartRow = new JLabel("Start row:");
      txtStartRow = new JTextField("");
      txtStartRow.addFocusListener(new FocusAdapter() {
         @Override
         public void focusGained(FocusEvent evt) {
            SwingUtilities.invokeLater(() -> { txtStartRow.selectAll(); });
         }
      });

      JLabel lblEndRow = new JLabel("End row:");
      txtEndRow = new JTextField("");
      txtEndRow.addFocusListener(new FocusAdapter() {
         @Override
         public void focusGained(FocusEvent evt) {
            SwingUtilities.invokeLater(() -> { txtEndRow.selectAll(); });
         }
      });

      JLabel lblComment = new JLabel("Comment:");
      txtComment = new JTextField("");
      txtComment.addFocusListener(new FocusAdapter() {
         @Override
         public void focusGained(FocusEvent evt) {
            SwingUtilities.invokeLater(() -> {
               txtComment.requestFocus();
               txtComment.selectAll();
            });
         }
      });

      JLabel lblRule = new JLabel("Rule:");

      JPanel pnlFields = new JPanel(new GridLayout(7, 2));
      pnlFields.add(lblSheetName);
      pnlFields.add(cbbSheetName);
      pnlFields.add(lblStartColumn);
      pnlFields.add(txtStartColumn);
      pnlFields.add(lblEndColumn);
      pnlFields.add(txtEndColumn);
      pnlFields.add(lblStartRow);
      pnlFields.add(txtStartRow);
      pnlFields.add(lblEndRow);
      pnlFields.add(txtEndRow);
      pnlFields.add(lblComment);
      pnlFields.add(txtComment);
      pnlFields.add(lblRule);

      pnlMain.add(pnlFields, BorderLayout.NORTH);

      txtRule = new JTextArea("", 20, 48);
      pnlMain.add(txtRule, BorderLayout.CENTER);
   }

   public void setSheetName(String sheetName) {
      cbbSheetName.setSelectedItem(sheetName);
   }

   public void setStartColumn(String columnName) {
      if (columnName.isEmpty()) {
         columnName = "A";
      }
      txtStartColumn.setText(columnName);
   }

   public void setStartRow(String rowNumber) {
      if (rowNumber.isEmpty()) {
         rowNumber = "1";
      }
      txtStartRow.setText(rowNumber);
   }

   public void setEndColumn(String columnName) {
      if (columnName.isEmpty()) {
         columnName = "+";
      }
      txtEndColumn.setText(columnName);
   }

   public void setEndRow(String rowNumber) {
      if (rowNumber.isEmpty()) {
         rowNumber = "+";
      }
      txtEndRow.setText(rowNumber);
   }

   public void fillFormFields(String sheetName, String startColumn, String endColumn, String startRow, String endRow,
         String rule, String comment)
   {
      cbbSheetName.setSelectedItem(sheetName);

      txtStartColumn.setText(startColumn);
      txtEndColumn.setText(endColumn);
      txtStartRow.setText(startRow);
      txtEndRow.setText(endRow);

      txtComment.setText(comment);
      txtRule.setText(rule);
   }

   public TransformationRule getUserInput()
   {
      return new TransformationRule((String) cbbSheetName.getSelectedItem(),
            txtStartColumn.getText().trim().toUpperCase(), txtEndColumn.getText().trim().toUpperCase(),
            txtStartRow.getText().trim(), txtEndRow.getText().trim(), txtComment.getText().trim(),
            txtRule.getText().trim());
   }
}
