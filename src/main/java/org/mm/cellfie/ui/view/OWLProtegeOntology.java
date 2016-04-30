package org.mm.cellfie.ui.view;

import org.mm.core.OWLEntityResolver;
import org.mm.core.OWLOntologySourceHook;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.OWLEntityFactory;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.model.OWLOntology;

public class OWLProtegeOntology implements OWLOntologySourceHook
{
   public OWLModelManager modelManager;

   public OWLProtegeOntology(OWLEditorKit editorKit)
   {
      this.modelManager = editorKit.getOWLModelManager();
   }

   @Override
   public OWLOntology getOWLOntology()
   {
      return modelManager.getActiveOntology();
   }

   @Override
   public OWLEntityResolver getEntityResolver()
   {
      OWLEntityFinder entityFinder = modelManager.getOWLEntityFinder();
      OWLEntityFactory entityFactory = modelManager.getOWLEntityFactory();
      return new OWLProtegeEntityResolver(entityFinder, entityFactory);
   }
}
