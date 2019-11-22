package org.mm.cellfie.transformationrule;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleList extends ArrayList<TransformationRule> {

   private static final long serialVersionUID = 1L;

   public TransformationRuleList() {
      super();
   }

   public TransformationRuleList(Collection<TransformationRule> rules) {
      for (TransformationRule rule : rules) {
         add(rule);
      }
   }

   public void addRule(TransformationRule rule) {
      add(rule);
   }

   public boolean removeRule(TransformationRule rule) {
      return remove(rule);
   }
}
