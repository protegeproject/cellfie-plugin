package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.Component;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class PreviewedAxioms extends MList {

   private static final long serialVersionUID = 1L;

   private OWLEditorKit editorKit;

   public PreviewedAxioms(OWLEditorKit editorKit) {
      this.editorKit = editorKit;
      setCellRenderer(new AxiomListItemRenderer());
   }

   public void setAxioms(Set<OWLAxiom> axiomSet) {
      Vector<PreviewedAxioms.Item> items = new Vector<>();
      for (OWLAxiom ax : axiomSet) {
         items.add(new Item(ax));
      }
      Collections.sort(items, new Comparator<PreviewedAxioms.Item>() {
         @Override
         public int compare(PreviewedAxioms.Item o1, PreviewedAxioms.Item o2) {
            return o1.axiom.compareTo(o2.axiom);
         }
      });
      setListData(items);
      setFixedCellHeight(24);
   }

   private class AxiomListItemRenderer implements ListCellRenderer<PreviewedAxioms.Item> {

      private OWLCellRenderer renderer;

      public AxiomListItemRenderer() {
         renderer = new OWLCellRenderer(editorKit);
      }

      @Override
      public Component getListCellRendererComponent(JList<? extends PreviewedAxioms.Item> list,
            PreviewedAxioms.Item item, int index, boolean isSelected, boolean cellHasFocus) {
         renderer.setHighlightKeywords(true);
         renderer.setWrap(false);
         return renderer.getListCellRendererComponent(list, item.axiom, index, isSelected,
               cellHasFocus);
      }
   }

   /**
    * Represents each OWL axiom object in the previewed axioms MList.
    */
   public class Item implements MListItem {

      private final OWLAxiom axiom;

      public Item(@Nonnull OWLAxiom axiom) {
         checkNotNull(axiom);
         this.axiom = axiom;
      }

      public OWLAxiom getObject() {
         return axiom;
      }

      @Override
      public boolean isEditable() {
         return false;
      }

      @Override
      public void handleEdit() {
         // NO-OP
      }

      @Override
      public boolean isDeleteable() {
         return false;
      }

      @Override
      public boolean handleDelete() {
         return false;
      }

      @Override
      public String getTooltip() {
         return "";
      }
   }
}
