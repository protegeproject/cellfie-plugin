package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.mm.cellfie.transformationrule.TransformationRule;
import org.mm.renderer.Sheet;

/**
 * Represents the editor form used to create a transformation rule.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleEditor extends JPanel {

   private static final long serialVersionUID = 1L;

   private JComboBox<Sheet> cbbSheetName;

   private JTextField txtStartColumn;
   private JTextField txtEndColumn;
   private JTextField txtStartRow;
   private JTextField txtEndRow;
   private JTextField txtComment;

   private JTextArea txtRule;

   public TransformationRuleEditor(@Nonnull CellfieWorkspace cellfieWorkspace) {
      checkNotNull(cellfieWorkspace);
      
      setLayout(new BorderLayout());

      JPanel pnlMain = new JPanel(new BorderLayout());
      pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      add(pnlMain, BorderLayout.CENTER);

      JLabel lblSheetName = new JLabel("Sheet name:");
      List<Sheet> sheets = cellfieWorkspace.getWorkbook().getSheets();
      cbbSheetName = new JComboBox<>(new DefaultComboBoxModel<>(new Vector<>(sheets)));

      JLabel lblStartColumn = new JLabel("Start column:");
      txtStartColumn = new JTextField("");
      txtStartColumn.addFocusListener(new FocusAdapter() {
         @Override
         public void focusGained(FocusEvent evt) {
            SwingUtilities.invokeLater(() -> {
               txtStartColumn.selectAll();
            });
         }
      });

      JLabel lblEndColumn = new JLabel("End column:");
      txtEndColumn = new JTextField("");
      txtEndColumn.addFocusListener(new FocusAdapter() {
         @Override
         public void focusGained(FocusEvent evt) {
            SwingUtilities.invokeLater(() -> {
               txtEndColumn.selectAll();
            });
         }
      });

      JLabel lblStartRow = new JLabel("Start row:");
      txtStartRow = new JTextField("");
      txtStartRow.addFocusListener(new FocusAdapter() {
         @Override
         public void focusGained(FocusEvent evt) {
            SwingUtilities.invokeLater(() -> {
               txtStartRow.selectAll();
            });
         }
      });

      JLabel lblEndRow = new JLabel("End row:");
      txtEndRow = new JTextField("");
      txtEndRow.addFocusListener(new FocusAdapter() {
         @Override
         public void focusGained(FocusEvent evt) {
            SwingUtilities.invokeLater(() -> {
               txtEndRow.selectAll();
            });
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

   public void setSheetName(@Nonnull String sheetName) {
      checkNotNull(sheetName);
      cbbSheetName.setSelectedItem(sheetName);
   }

   public void setStartColumn(@Nonnull String columnName) {
      checkNotNull(columnName);
      if (columnName.isEmpty()) {
         columnName = TransformationRule.START_COLUMN;
      }
      txtStartColumn.setText(columnName);
   }

   public void setStartRow(@Nonnull String rowNumber) {
      checkNotNull(rowNumber);
      if (rowNumber.isEmpty()) {
         rowNumber = TransformationRule.START_ROW;
      }
      txtStartRow.setText(rowNumber);
   }

   public void setEndColumn(@Nonnull String columnName) {
      checkNotNull(columnName);
      if (columnName.isEmpty()) {
         columnName = TransformationRule.ANY_WILDCARD;
      }
      txtEndColumn.setText(columnName);
   }

   public void setEndRow(@Nonnull String rowNumber) {
      checkNotNull(rowNumber);
      if (rowNumber.isEmpty()) {
         rowNumber = TransformationRule.ANY_WILDCARD;
      }
      txtEndRow.setText(rowNumber);
   }

   public void setComment(@Nonnull String comment) {
      checkNotNull(comment);
      txtComment.setText(comment);
   }
   
   public void setRuleExpression(@Nonnull String ruleExpression) {
      checkNotNull(ruleExpression);
      txtRule.setText(ruleExpression);
   }

   public TransformationRule getTransformationRule() {
      return new TransformationRule((Sheet) cbbSheetName.getSelectedItem(),
            txtStartColumn.getText().trim().toUpperCase(),
            txtEndColumn.getText().trim().toUpperCase(),
            txtStartRow.getText().trim(),
            txtEndRow.getText().trim(),
            txtComment.getText().trim(),
            txtRule.getText().trim());
   }
}
