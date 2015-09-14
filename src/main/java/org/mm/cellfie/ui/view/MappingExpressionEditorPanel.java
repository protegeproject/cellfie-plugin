package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.mm.core.MappingExpression;

public class MappingExpressionEditorPanel extends JPanel
{
   private static final long serialVersionUID = 1L;

   private JComboBox<String> cbbSheetName;

   private JTextField txtStartColumn;
   private JTextField txtEndColumn;
   private JTextField txtStartRow;
   private JTextField txtEndRow;
   private JTextField txtComment;

   private JTextArea txtExpression;

   public MappingExpressionEditorPanel()
   {
      setLayout(new BorderLayout());

      JPanel pnlMain = new JPanel(new BorderLayout());
      pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      add(pnlMain, BorderLayout.CENTER);

      JLabel lblSheetName = new JLabel("Sheet name:");
      cbbSheetName = new JComboBox<>();
      cbbSheetName.setModel(new DefaultComboBoxModel<>());

      JLabel lblStartColumn = new JLabel("Start column:");
      txtStartColumn = new JTextField("");

      JLabel lblEndColumn = new JLabel("End column:");
      txtEndColumn = new JTextField("");

      JLabel lblStartRow = new JLabel("Start row:");
      txtStartRow = new JTextField("");

      JLabel lblEndRow = new JLabel("End row:");
      txtEndRow = new JTextField("");

      JLabel lblComment = new JLabel("Comment:");
      txtComment = new JTextField("");

      JLabel lblExpression = new JLabel("DSL mapping expression:");

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
      pnlFields.add(lblExpression);

      pnlMain.add(pnlFields, BorderLayout.NORTH);

      txtExpression = new JTextArea("", 20, 48);
      pnlMain.add(txtExpression, BorderLayout.CENTER);
   }

   public void setSheetNames(List<String> sheetNames)
   {
      cbbSheetName.setModel(new DefaultComboBoxModel<>(new Vector<>(sheetNames)));
   }

   public void fillFormFields(String sheetName, String startColumn, String endColumn, String startRow, String endRow,
         String expression, String comment)
   {
      cbbSheetName.setSelectedItem(sheetName);

      txtStartColumn.setText(startColumn);
      txtEndColumn.setText(endColumn);
      txtStartRow.setText(startRow);
      txtEndRow.setText(endRow);

      txtComment.setText(comment);
      txtExpression.setText(expression);
   }

   public MappingExpression getUserInput()
   {
      return new MappingExpression((String) cbbSheetName.getSelectedItem(),
            txtStartColumn.getText().trim().toUpperCase(), txtEndColumn.getText().trim().toUpperCase(),
            txtStartRow.getText().trim(), txtEndRow.getText().trim(), txtComment.getText().trim(),
            txtExpression.getText().trim());
   }
}
