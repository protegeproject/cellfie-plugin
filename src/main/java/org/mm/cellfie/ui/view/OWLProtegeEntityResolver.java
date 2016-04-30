package org.mm.cellfie.ui.view;

import org.mm.core.OWLEntityResolver;
import org.mm.exceptions.EntityCreationException;
import org.mm.exceptions.EntityNotFoundException;
import org.protege.editor.owl.model.entity.EntityCreationPreferences;
import org.protege.editor.owl.model.entity.OWLEntityCreationException;
import org.protege.editor.owl.model.entity.OWLEntityFactory;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.model.OWLEntity;

public class OWLProtegeEntityResolver implements OWLEntityResolver
{
   private OWLEntityFinder entityFinder;
   private OWLEntityFactory entityFactory;

   public OWLProtegeEntityResolver(OWLEntityFinder entityFinder, OWLEntityFactory entityFactory)
   {
      this.entityFinder = entityFinder;
      this.entityFactory = entityFactory;
   }

   @Override
   public String getDefaultPrefix()
   {
      return EntityCreationPreferences.getDefaultBaseIRI().toString();
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
      try {
         return entityFactory.createOWLEntity(entityType, shortName, null).getOWLEntity();
      } catch (OWLEntityCreationException e) {
         String template = "Failed to create entity from input value '%s'";
         throw new EntityCreationException(String.format(template, shortName), e);
      }
   }
}
