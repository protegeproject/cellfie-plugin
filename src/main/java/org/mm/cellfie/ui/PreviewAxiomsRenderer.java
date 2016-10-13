package org.mm.cellfie.ui;

import java.awt.Component;

import javax.annotation.Nonnull;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;

public class PreviewAxiomsRenderer implements ListCellRenderer<PreviewAxiomList.Item> {

   private OWLCellRenderer renderer;

   public PreviewAxiomsRenderer(@Nonnull OWLEditorKit editorKit) {
      renderer = new OWLCellRenderer(editorKit);
   }

   @Override
   public Component getListCellRendererComponent(JList<? extends PreviewAxiomList.Item> list,
         PreviewAxiomList.Item item, int index, boolean isSelected, boolean cellHasFocus) {
      renderer.setHighlightKeywords(true);
      renderer.setWrap(false);
      return renderer.getListCellRendererComponent(list, item.getObject(), index, isSelected,
            cellHasFocus);
   }
}