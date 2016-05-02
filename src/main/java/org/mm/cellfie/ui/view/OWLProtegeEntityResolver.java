package org.mm.cellfie.ui.view;

import org.mm.core.OWLEntityResolver;
import org.mm.exceptions.EntityCreationException;
import org.mm.exceptions.EntityNotFoundException;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.entity.OWLEntityCreationException;
import org.protege.editor.owl.model.entity.OWLEntityFactory;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.Namespaces;

public class OWLProtegeEntityResolver implements OWLEntityResolver
{
   private OWLEditorKit editorKit;
   private OWLEntityFinder entityFinder;
   private OWLEntityFactory entityFactory;

   public OWLProtegeEntityResolver(OWLEditorKit editorKit)
   {
      this.editorKit = editorKit;
      entityFinder = editorKit.getModelManager().getOWLEntityFinder();
      entityFactory = editorKit.getModelManager().getOWLEntityFactory();
   }

   @Override
   public <T extends OWLEntity> T resolve(String shortName, final Class<T> entityType) throws EntityNotFoundException
   {
      OWLEntity entity = entityFinder.getOWLEntity(shortName);
      if (entity != null) {
         try {
            T toReturn = entityType.cast(entity);
            return toReturn;
         }
         catch (ClassCastException e) {
            String template = "The expected entity '%s' does not have type: %s";
            throw new EntityNotFoundException(String.format(template, shortName, entityType.getSimpleName()));
         }
      }
      String template = "The expected entity '%s' does not exist in the ontology";
      throw new EntityNotFoundException(String.format(template, shortName));
   }

   @Override
   public <T extends OWLEntity> T create(String entityName, final Class<T> entityType) throws EntityCreationException
   {
      OWLEntity entity = entityFinder.getOWLEntity(entityName);
      if (entity == null) {
         try {
            return entityFactory.createOWLEntity(entityType, getLocalName(entityName), getPrefix(entityName)).getOWLEntity();
         } catch (OWLEntityCreationException e) {
            throw new IllegalStateException("Programmer error - report this (with stack trace) to the Protege mailing list");
         }
      }
      return entityType.cast(entity);
   }

   private String getLocalName(String entityName)
   {
      int colonIndex = entityName.indexOf(':');
      if (colonIndex >= 0) {
          return entityName.substring(colonIndex + 1);
      }
      return entityName;
   }

   private IRI getPrefix(String entityName)
   {
      OWLOntology activeOntology = editorKit.getModelManager().getActiveOntology();
      OWLOntologyManager manager = editorKit.getModelManager().getOWLOntologyManager();
      OWLDocumentFormat format = manager.getOntologyFormat(activeOntology);
      for (Namespaces ns : Namespaces.values()) {
          if (entityName.startsWith(ns.name().toLowerCase() + ":")) {
              return IRI.create(ns.toString());
          }
      }
      int colonIndex = entityName.indexOf(':');
      if (colonIndex > 0 && format != null && format.isPrefixOWLOntologyFormat()) {
          PrefixDocumentFormat prefixes = format.asPrefixOWLOntologyFormat();
          String prefixName = entityName.substring(0, colonIndex + 1);
          String prefix = prefixes.getPrefix(prefixName);
          if (prefix != null) {
              return IRI.create(prefix);
          }
      }
      return null;
   }
}
