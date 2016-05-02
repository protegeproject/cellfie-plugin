package org.mm.cellfie.ui.view;

import org.mm.core.OWLEntityResolver;
import org.mm.exceptions.EntityCreationException;
import org.mm.exceptions.EntityNotFoundException;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.PrefixManager;

public class OWLProtegeEntityResolver implements OWLEntityResolver
{
   private PrefixManager prefixManager;

   private OWLEntityFinder entityFinder;
   private OWLDataFactory dataFactory;

   public OWLProtegeEntityResolver(OWLEditorKit editorKit, PrefixManager prefixManager)
   {
      this.prefixManager = prefixManager;
      entityFinder = editorKit.getModelManager().getOWLEntityFinder();
      dataFactory = editorKit.getOWLModelManager().getOWLDataFactory();
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
   public <T extends OWLEntity> T create(String shortName, final Class<T> entityType) throws EntityCreationException
   {
      if (OWLClass.class.isAssignableFrom(entityType)) {
         return entityType.cast(dataFactory.getOWLClass(shortName, prefixManager));
      } else if (OWLObjectProperty.class.isAssignableFrom(entityType)) {
         return entityType.cast(dataFactory.getOWLObjectProperty(shortName, prefixManager));
      } else if (OWLDataProperty.class.isAssignableFrom(entityType)) {
         return entityType.cast(dataFactory.getOWLDataProperty(shortName, prefixManager));
      } else if (OWLNamedIndividual.class.isAssignableFrom(entityType)) {
         return entityType.cast(dataFactory.getOWLNamedIndividual(shortName, prefixManager));
      } else if (OWLAnnotationProperty.class.isAssignableFrom(entityType)) {
         return entityType.cast(dataFactory.getOWLAnnotationProperty(shortName, prefixManager));
      } else if (OWLDatatype.class.isAssignableFrom(entityType)) {
         return entityType.cast(dataFactory.getOWLDatatype(shortName, prefixManager));
      }
      String template = "Failed to create entity from input value '%s'";
      throw new EntityCreationException(String.format(template, shortName));
   }
}
