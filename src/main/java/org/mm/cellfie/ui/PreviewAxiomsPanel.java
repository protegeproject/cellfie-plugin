package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Represents the preview panel used to display the generated axioms to the users.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class PreviewAxiomsPanel extends JPanel {

   private static final long serialVersionUID = 1L;

   private final JLabel previewSummaryLabel;
   private final PreviewAxiomList previewedList;

   public PreviewAxiomsPanel(@Nonnull OWLEditorKit editorKit) {
      checkNotNull(editorKit);

      setLayout(new BorderLayout());

      previewSummaryLabel = new JLabel();
      add(previewSummaryLabel, BorderLayout.NORTH);

      previewedList = new PreviewAxiomList(editorKit);
      add(new JScrollPane(previewedList), BorderLayout.CENTER);

      JPanel pnlViewLog = new JPanel();
      pnlViewLog.setLayout(new FlowLayout(FlowLayout.RIGHT));
      add(pnlViewLog, BorderLayout.SOUTH);
   }

   public void setContent(@Nonnull Collection<OWLAxiom> axioms) {
      setContent(axioms, null);
   }

   public void setContent(@Nonnull Collection<OWLAxiom> axioms, String executionLog) {
      checkNotNull(axioms);
      writeSummary(axioms);
      fillList(axioms);
   }

   private void writeSummary(Collection<OWLAxiom> axioms) {
      final DecimalFormat decimalFormatter = new DecimalFormat("#,###");
      String formattedSize = decimalFormatter.format(axioms.size());
      previewSummaryLabel.setText(format("Cellfie generates %s axioms.", formattedSize));
   }

   private void fillList(Collection<OWLAxiom> axioms) {
      previewedList.addAxioms(axioms);
   }
}
