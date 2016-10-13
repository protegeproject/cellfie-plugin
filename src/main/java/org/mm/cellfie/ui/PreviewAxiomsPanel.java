package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.google.common.base.Strings;

/**
 * Represents the preview panel used to display the generated axioms to the users.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class PreviewAxiomsPanel extends JPanel {

   private static final long serialVersionUID = 1L;

   private final JLabel previewSummaryLabel;
   private final PreviewedAxioms previewedList;

   private String logMessage;

   @SuppressWarnings("unchecked")
   public PreviewAxiomsPanel(@Nonnull CellfieWorkspace container, @Nonnull OWLEditorKit editorKit) {
      checkNotNull(container);
      checkNotNull(editorKit);

      setLayout(new BorderLayout());

      previewSummaryLabel = new JLabel();
      add(previewSummaryLabel, BorderLayout.NORTH);

      previewedList = new PreviewedAxioms(editorKit);
      add(new JScrollPane(previewedList), BorderLayout.CENTER);

      JPanel pnlViewLog = new JPanel();
      pnlViewLog.setLayout(new FlowLayout(FlowLayout.RIGHT));
      add(pnlViewLog, BorderLayout.SOUTH);

      JLabel lblViewLog = new JLabel("View Log");
      lblViewLog.setBorder(new EmptyBorder(0, 7, 0, 0));
      Font font = lblViewLog.getFont();
      @SuppressWarnings("rawtypes")
      Map attributes = font.getAttributes();
      attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
      lblViewLog.setFont(font.deriveFont(attributes));
      lblViewLog.setForeground(Color.BLUE);
      lblViewLog.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblViewLog.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            DialogUtils.showDialog(container, "Log Viewer", new LogViewerPanel(getExecutionLog()),
                  JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
         }
      });
      pnlViewLog.add(lblViewLog);
   }

   public void setContent(@Nonnull Set<OWLAxiom> axioms) {
      setContent(axioms, null);
   }

   public void setContent(@Nonnull Set<OWLAxiom> axioms, String executionLog) {
      checkNotNull(axioms);
      writeSummary(axioms);
      fillList(axioms);
      setExecutionLog(executionLog);
   }

   private void writeSummary(Set<OWLAxiom> axioms) {
      final DecimalFormat decimalFormatter = new DecimalFormat("#,###");
      String formattedSize = decimalFormatter.format(axioms.size());
      previewSummaryLabel.setText(format("Cellfie generates %s axioms.", formattedSize));
   }

   private void fillList(Set<OWLAxiom> axioms) {
      previewedList.addAxioms(axioms);
   }

   private void setExecutionLog(String logMessage) {
      this.logMessage = logMessage;
   }

   private String getExecutionLog() {
      if (Strings.isNullOrEmpty(logMessage)) {
         return "Empty log";
      }
      return logMessage;
   }
}
