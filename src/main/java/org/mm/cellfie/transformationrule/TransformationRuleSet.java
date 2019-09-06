package org.mm.cellfie.transformationrule;

import java.util.Collection;
import java.util.HashSet;
import javax.annotation.Nonnull;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleSet extends HashSet<TransformationRule> {

   private static final long serialVersionUID = -5555840174548058602L;

   public TransformationRuleSet() {
      super();
   }

   @Nonnull
   public static TransformationRuleSet create() {
      return new TransformationRuleSet();
   }

   @Nonnull
   public static TransformationRuleSet create(Collection<TransformationRule> rules) {
      TransformationRuleSet ruleSet = new TransformationRuleSet();
      for (TransformationRule rule : rules) {
         ruleSet.add(rule);
      }
      return ruleSet;
   }
}
