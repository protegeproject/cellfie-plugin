package org.mm.cellfie.action;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import org.mm.core.OWLEntityResolver;
import org.mm.exceptions.EntityCreationException;
import org.mm.exceptions.EntityNotFoundException;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.OWLEntityCreationException;
import org.protege.editor.owl.model.entity.OWLEntityFactory;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.Namespaces;

public class OWLProtegeEntityResolver implements OWLEntityResolver {

   private final OWLModelManager modelManager;
   private final OWLEntityFinder entityFinder;
   private final OWLEntityFactory entityFactory;

   public OWLProtegeEntityResolver(@Nonnull OWLEditorKit editorKit) {
      checkNotNull(editorKit);
      modelManager = editorKit.getModelManager();
      entityFinder = modelManager.getOWLEntityFinder();
      entityFactory = modelManager.getOWLEntityFactory();
   }

   @Override
   public <T extends OWLEntity> T resolve(String entityName, final Class<T> entityType)
         throws EntityNotFoundException {
      OWLEntity entity = entityFinder.getOWLEntity(entityName);
      if (entity != null) {
         try {
            T toReturn = entityType.cast(entity);
            return toReturn;
         } catch (ClassCastException e) {
            throw new EntityNotFoundException(
                  String.format("The expected entity name '%s' does not have type %s",
                  entityName, entityType.getSimpleName()));
         }
      }
      throw new EntityNotFoundException(
            String.format("The expected entity name '%s' does not exist in the ontology",
                  entityName));
   }

   @Override
   public <T extends OWLEntity> T resolveUnchecked(String entityName, final Class<T> entityType) {
      try {
         return resolve(entityName, entityType);
      } catch (EntityNotFoundException e) {
         throw new RuntimeException(e.getMessage());
      }
   }

   /**
    * Creates an OWL entity following its given name and type. The method will first look
    * the entity name in the active ontology and reuse the same object. If no entity was
    * found, this method will create a new object
    *
    * @param entityName
    *          The entity name in short form or as a prefixed name string.
    * @param entityType
    *          The entity type following the OWLAPI class hierarchy. The types an be
    *          one of these: {@link OWLClass}, {@link OWLDataProperty},
    *          {@link OWLObjectProperty}. {@link OWLNamedIndividual} or
    *          {@link OWLDatatype}.
    * @return Returns an OWL entity object following its given name and type.
    * @throws EntityCreationException If the entity creation was failed
    */
   @Override
   public <T extends OWLEntity> T create(String entityName, final Class<T> entityType)
         throws EntityCreationException {
      OWLEntity entity = entityFinder.getOWLEntity(entityName);
      if (entity == null) {
         return createNew(entityName, entityType);
      }
      return entityType.cast(entity);
   }

   private <T extends OWLEntity> T createNew(String entityName, final Class<T> entityType) throws EntityCreationException {
      try {
         String localName = getLocalName(entityName);
         String prefix = getPrefix(entityName);
         IRI entityIri = IRI.create(prefix);
         return entityFactory.createOWLEntity(entityType, localName, entityIri).getOWLEntity();
      } catch (OWLEntityCreationException e) {
         throw new EntityCreationException(e.getMessage());
      }
   }

   @Override
   public <T extends OWLEntity> T createUnchecked(String entityName, final Class<T> entityType) {
      try {
         return create(entityName, entityType);
      } catch (EntityCreationException e) {
         throw new RuntimeException(e.getMessage());
      }
   }

   private String getLocalName(String prefixedName) {
      int colonIndex = prefixedName.indexOf(':');
      if (colonIndex >= 0) {
          return prefixedName.substring(colonIndex + 1);
      }
      return prefixedName;
   }

   private String getPrefix(String prefixedName) {
      OWLOntology activeOntology = modelManager.getActiveOntology();
      OWLOntologyManager manager = modelManager.getOWLOntologyManager();
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
      throw new IllegalArgumentException(
            String.format("Unable to get the prefix from '%s'", prefixedName));
   }
}
