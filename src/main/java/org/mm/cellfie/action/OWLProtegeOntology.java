package org.mm.cellfie.action;

import org.mm.core.OWLEntityResolver;
import org.mm.core.OWLOntologySourceHook;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLOntology;

public class OWLProtegeOntology implements OWLOntologySourceHook
{
   private OWLEditorKit editorKit;
   private OWLModelManager modelManager;

   public OWLProtegeOntology(OWLEditorKit editorKit)
   {
      this.editorKit = editorKit;
      modelManager = editorKit.getOWLModelManager();
   }

   @Override
   public OWLOntology getOWLOntology()
   {
      return modelManager.getActiveOntology();
   }

   @Override
   public OWLEntityResolver getEntityResolver()
   {
      return new OWLProtegeEntityResolver(editorKit);
   }
}
