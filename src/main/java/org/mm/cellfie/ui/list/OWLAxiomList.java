package org.mm.cellfie.ui.list;

import java.awt.Component;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.OWLAxiom;

public class OWLAxiomList extends MList
{
   private static final long serialVersionUID = 1L;

   private OWLEditorKit editorKit;

   public OWLAxiomList(OWLEditorKit editorKit)
   {
      this.editorKit = editorKit;
      setCellRenderer(new AxiomListItemRenderer());
   }

   public void setAxioms(Set<OWLAxiom> axiomSet)
   {
      Vector<AxiomListItem> items = new Vector<AxiomListItem>();
      for (OWLAxiom ax : axiomSet) {
         items.add(new AxiomListItem(ax));
      }
      Collections.sort(items, new Comparator<AxiomListItem>()
      {
         @Override
         public int compare(AxiomListItem o1, AxiomListItem o2)
         {
            return o1.axiom.compareTo(o2.axiom);
         }
      });
      setListData(items);
      setFixedCellHeight(24);
   }

   private class AxiomListItemRenderer implements ListCellRenderer<AxiomListItem>
   {
      private OWLCellRenderer renderer;

      public AxiomListItemRenderer()
      {
         renderer = new OWLCellRenderer(editorKit);
      }

      public Component getListCellRendererComponent(JList<? extends AxiomListItem> list, AxiomListItem item, int index,
            boolean isSelected, boolean cellHasFocus)
      {
         renderer.setHighlightKeywords(true);
         renderer.setWrap(false);
         return renderer.getListCellRendererComponent(list, item.axiom, index, isSelected, cellHasFocus);
      }
   }

   private class AxiomListItem implements MListItem
   {
      private OWLAxiom axiom;

      public AxiomListItem(OWLAxiom axiom)
      {
         this.axiom = axiom;
      }

      public boolean isEditable()
      {
         return false;
      }

      public void handleEdit()
      {
         // NO-OP
      }

      public boolean isDeleteable()
      {
         return false;
      }

      public boolean handleDelete()
      {
         return false;
      }

      public String getTooltip()
      {
         return "";
      }
   }
}
