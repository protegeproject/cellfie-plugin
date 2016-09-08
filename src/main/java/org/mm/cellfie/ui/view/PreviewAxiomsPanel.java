package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.mm.cellfie.ui.list.OWLAxiomList;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.semanticweb.owlapi.model.OWLAxiom;

public class PreviewAxiomsPanel extends JPanel
{
   private static final long serialVersionUID = 1L;

   private final WorkspacePanel container;
   private final String logMessage;

   public PreviewAxiomsPanel(WorkspacePanel container, Set<OWLAxiom> axioms, String logMessage)
   {
      this.container = container;
      this.logMessage = logMessage;

      setLayout(new BorderLayout());

      JLabel lblPreview = new JLabel();
      lblPreview.setText("Cellfie generates " + axioms.size() + " axioms:");
      add(lblPreview, BorderLayout.NORTH);

      OWLAxiomList previewList = new OWLAxiomList(container.getEditorKit());
      previewList.setAxioms(axioms);
      add(new JScrollPane(previewList), BorderLayout.CENTER);

      JPanel pnlViewLog = new JPanel();
      pnlViewLog.setLayout(new FlowLayout(FlowLayout.RIGHT));
      add(pnlViewLog, BorderLayout.SOUTH);

      JLabel lblViewLog = new JLabel("View Log");
      lblViewLog.setBorder(new EmptyBorder(0, 7, 0, 0));
      Font font = lblViewLog.getFont();
      Map attributes = font.getAttributes();
      attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
      lblViewLog.setFont(font.deriveFont(attributes));
      lblViewLog.setForeground(Color.BLUE);
      lblViewLog.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblViewLog.addMouseListener(new ViewLogAction());
      pnlViewLog.add(lblViewLog);
   }

   class ViewLogAction extends MouseAdapter
   {
      @Override
      public void mouseClicked(MouseEvent e)
      {
         JOptionPaneEx.showConfirmDialog(container, "Log Viewer", new LogViewerPanel(logMessage), JOptionPane.PLAIN_MESSAGE,
               JOptionPane.DEFAULT_OPTION, null);
      }
   }
}
