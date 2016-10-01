package org.mm.cellfie.action;

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
   public <T extends OWLEntity> T resolve(String renderingName, final Class<T> entityType) throws EntityNotFoundException
   {
      OWLEntity entity = entityFinder.getOWLEntity(renderingName);
      if (entity != null) {
         try {
            T toReturn = entityType.cast(entity);
            return toReturn;
         }
         catch (ClassCastException e) {
            String template = "The expected entity '%s' does not have type: %s";
            throw new EntityNotFoundException(String.format(template, renderingName, entityType.getSimpleName()));
         }
      }
      String template = "The expected entity '%s' does not exist in the ontology";
      throw new EntityNotFoundException(String.format(template, renderingName));
   }

   @Override
   public <T extends OWLEntity> T create(String entityName, final Class<T> entityType) throws EntityCreationException
   {
      OWLEntity entity = entityFinder.getOWLEntity(entityName);
      if (entity == null) {
         try {
            String localName = getLocalName(entityName);
            String prefix = getPrefix(entityName);
            return entityFactory.createOWLEntity(entityType, localName, IRI.create(prefix)).getOWLEntity();
         } catch (OWLEntityCreationException e) {
            throw new EntityCreationException(e.getMessage());
         }
      }
      return entityType.cast(entity);
   }

   private String getLocalName(String prefixedName)
   {
      int colonIndex = prefixedName.indexOf(':');
      if (colonIndex >= 0) {
          return prefixedName.substring(colonIndex + 1);
      }
      return prefixedName;
   }

   private String getPrefix(String prefixedName) throws OWLEntityCreationException
   {
      OWLOntology activeOntology = editorKit.getModelManager().getActiveOntology();
      OWLOntologyManager manager = editorKit.getModelManager().getOWLOntologyManager();
      OWLDocumentFormat format = manager.getOntologyFormat(activeOntology);
      for (Namespaces ns : Namespaces.values()) {
          if (prefixedName.startsWith(ns.name().toLowerCase() + ":")) {
              return ns.toString();
          }
      }
      int colonIndex = prefixedName.indexOf(':');
      if (colonIndex > 0 && format != null && format.isPrefixOWLOntologyFormat()) {
          PrefixDocumentFormat prefixes = format.asPrefixOWLOntologyFormat();
          String prefixLabel = prefixedName.substring(0, colonIndex + 1);
          String prefix = prefixes.getPrefix(prefixLabel);
          if (prefix != null) {
              return prefix;
          }
      }
      String message = String.format("Unable to get the prefix from '%s'" + prefixedName);
      throw new OWLEntityCreationException(message);
   }
}
