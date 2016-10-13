package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.Set;
import java.util.Vector;

import javax.annotation.Nonnull;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Represents the list of generated axioms that will be presented in the preview panel.
 * 
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class PreviewedAxioms extends MList {

   private static final long serialVersionUID = 1L;

   public PreviewedAxioms(@Nonnull OWLEditorKit editorKit) {
      checkNotNull(editorKit);
      setCellRenderer(new PreviewAxiomsRenderer(editorKit));
      setFixedCellHeight(24);
   }

   @SuppressWarnings("unchecked")
   public void addAxioms(@Nonnull Set<OWLAxiom> axioms) {
      checkNotNull(axioms);
      final Vector<PreviewedAxioms.Item> items = copyAndSort(axioms);
      setListData(items);
   }

   private Vector<PreviewedAxioms.Item> copyAndSort(Set<OWLAxiom> axioms) {
      Vector<PreviewedAxioms.Item> listItems = new Vector<>();
      for (OWLAxiom axiom : axioms) {
         listItems.add(new PreviewedAxioms.Item(axiom));
      }
      Collections.sort(listItems, (o1, o2) -> {
         return o1.getObject().compareTo(o2.getObject());
      });
      return listItems;
   }

   /**
    * Represents each OWL axiom object in the preview list.
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
