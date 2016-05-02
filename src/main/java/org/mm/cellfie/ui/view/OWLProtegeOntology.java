package org.mm.cellfie.ui.view;

import java.util.Map;

import org.mm.core.OWLEntityResolver;
import org.mm.core.OWLOntologySourceHook;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.EntityCreationPreferences;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

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
      return new OWLProtegeEntityResolver(editorKit, buildPrefixManager());
   }

   private PrefixManager buildPrefixManager()
   {
      PrefixManager prefixManager = new DefaultPrefixManager();
      OWLDocumentFormat format = modelManager.getOWLOntologyManager().getOntologyFormat(getOWLOntology());
      if (format.isPrefixOWLOntologyFormat()) {
         Map<String, String> prefixMap = format.asPrefixOWLOntologyFormat().getPrefixName2PrefixMap();
         for (String prefixName : prefixMap.keySet()) {
            prefixManager.setPrefix(prefixName, prefixMap.get(prefixName));
         }
      }
      prefixManager.setDefaultPrefix(getDefaultBaseIRI());
      return prefixManager;
   }

   protected String getDefaultBaseIRI()
   {
      IRI baseIRI = null;
      if (useDefaultBaseIRI() || getOWLOntology().getOntologyID().isAnonymous()) {
         baseIRI = EntityCreationPreferences.getDefaultBaseIRI();
      } else {
         baseIRI = getOWLOntology().getOntologyID().getOntologyIRI().get();
      }
      String base = baseIRI.toString().replace(" ", "_");
      if (!base.endsWith("#") && !base.endsWith("/")) {
          base += EntityCreationPreferences.getDefaultSeparator();
      }
      return base;
   }

   private boolean useDefaultBaseIRI()
   {
      return EntityCreationPreferences.useDefaultBaseIRI();
   }
}
