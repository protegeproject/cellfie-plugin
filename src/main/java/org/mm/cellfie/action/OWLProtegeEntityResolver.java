package org.mm.cellfie.action;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

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
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

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

   /**
    * Resolves the given entity name and returns the OWL entity object with the specified type.
    * The method will scan the entity name in the active ontology and return the found object.
    * If no entity was found, the method will check against a list of built-in entities before
    * throwing a checked exception if still no entity was found.
    *
    * @param entityName
    *          The entity name in short form or as a prefixed name string.
    * @param entityType
    *          The entity type following the OWLAPI class hierarchy. The types an be
    *          one of these: {@link OWLClass}, {@link OWLDataProperty},
    *          {@link OWLObjectProperty}. {@link OWLNamedIndividual} or
    *          {@link OWLDatatype}.
    * @return Returns an OWL entity object according to its type.
    * @throws EntityNotFoundException If the entity name does not exist in the ontology.
    */
   @Override
   public <T extends OWLEntity> T resolve(String entityName, final Class<T> entityType)
         throws EntityNotFoundException {
      T entity = null;
      OWLEntity foundEntity = entityFinder.getOWLEntity(entityName);
      if (foundEntity == null) {
         entity = createNewForBuiltInEntity(entityName, entityType);
      } else {
         entity = entityType.cast(foundEntity);
      }
      if (entity == null) {
         throw new EntityNotFoundException(
               String.format("The expected entity name '%s' does not exist in the ontology",
                     entityName));
      }
      return entity;
   }

   private <T extends OWLEntity> T createNewForBuiltInEntity(String entityName, final Class<T> entityType) {
      if (isPrefixedName(entityName)) {
         IRI entityIri = expand(entityName);
         if (OWLRDFVocabulary.BUILT_IN_VOCABULARY_IRIS.contains(entityIri)) {
            try {
               return createNew(entityName, entityType);
            } catch (OWLEntityCreationException e) {
               throw new IllegalArgumentException(e.getMessage());
            }
         }
      }
      return null;
   }

   private boolean isPrefixedName(String entityName) {
      return entityName.indexOf(":") > 0;
   }

   /**
    * Resolves the given entity name and returns the OWL entity object with the specified type.
    * The method will scan the entity name in the active ontology and return the found object.
    * If no entity was found, the method will check against a list of built-in entities before
    * throwing a runtime exception if still no entity was found.
    *
    * @param entityName
    *          The entity name in short form or as a prefixed name string.
    * @param entityType
    *          The entity type following the OWLAPI class hierarchy. The types an be
    *          one of these: {@link OWLClass}, {@link OWLDataProperty},
    *          {@link OWLObjectProperty}. {@link OWLNamedIndividual} or
    *          {@link OWLDatatype}.
    * @return Returns an OWL entity object according to its type.
    * @throws EntityNotFoundException If the entity name does not exist in the ontology.
    */
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
         try {
            return createNew(entityName, entityType);
         } catch (OWLEntityCreationException e) {
            throw new EntityCreationException(e.getMessage());
         }
      }
      return entityType.cast(entity);
   }

   private <T extends OWLEntity> T createNew(String entityName, final Class<T> entityType)
         throws OWLEntityCreationException {
      String localName = getLocalName(entityName);
      Optional<IRI> prefix = getPrefix(entityName);
      IRI baseIri = prefix.orElseGet(() -> null);
      return entityFactory.createOWLEntity(entityType, localName, baseIri).getOWLEntity();
   }

   @Override
   public <T extends OWLEntity> T createUnchecked(String entityName, final Class<T> entityType) {
      try {
         return create(entityName, entityType);
      } catch (EntityCreationException e) {
         throw new RuntimeException(e.getMessage());
      }
   }

   private IRI expand(String entityName) {
      String localName = getLocalName(entityName);
      Optional<IRI> prefix = getPrefix(entityName);
      if (prefix.isPresent()) {
         String prefixString = prefix.get().toString();
         return IRI.create(prefixString + localName);
      }
      throw new IllegalArgumentException("Missing required prefix");
   }

   private String getLocalName(String prefixedName) {
      int colonIndex = prefixedName.indexOf(':');
      if (colonIndex >= 0) {
          return prefixedName.substring(colonIndex + 1);
      }
      return prefixedName;
   }

   private Optional<IRI> getPrefix(String prefixedName) {
      OWLOntology activeOntology = modelManager.getActiveOntology();
      OWLOntologyManager manager = modelManager.getOWLOntologyManager();
      OWLDocumentFormat format = manager.getOntologyFormat(activeOntology);
      for (Namespaces ns : Namespaces.values()) {
          if (prefixedName.startsWith(ns.name().toLowerCase() + ":")) {
              return Optional.of(IRI.create(ns.toString()));
          }
      }
      int colonIndex = prefixedName.indexOf(':');
      if (colonIndex > 0 && format != null && format.isPrefixOWLOntologyFormat()) {
          PrefixDocumentFormat prefixes = format.asPrefixOWLOntologyFormat();
          String prefixLabel = prefixedName.substring(0, colonIndex + 1);
          String prefix = prefixes.getPrefix(prefixLabel);
          if (prefix != null) {
              return Optional.of(IRI.create(prefix));
          }
      }
      return Optional.empty();
   }
}
