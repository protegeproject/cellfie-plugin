package org.mm.cellfie.transformationrule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.google.gson.annotations.SerializedName;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleSet implements Iterable<TransformationRule> {

   @SerializedName("Collections")
   private Set<TransformationRule> ruleSet;

   public TransformationRuleSet() {
      ruleSet = new HashSet<TransformationRule>();
   }

   public static TransformationRuleSet create(Collection<TransformationRule> rules) {
      TransformationRuleSet ruleSet = new TransformationRuleSet();
      for (TransformationRule rule : rules) {
         ruleSet.add(rule);
      }
      return ruleSet;
   }

   public Collection<TransformationRule> getTransformationRules() {
      return Collections.unmodifiableSet(ruleSet);
   }

   public void add(TransformationRule rule) {
      ruleSet.add(rule);
   }

   public boolean remove(TransformationRule rule) {
      return ruleSet.remove(rule);
   }

   public boolean isEmpty() {
      return ruleSet.isEmpty();
   }

   public boolean contains(TransformationRule rule) {
      return ruleSet.contains(rule);
   }

   public int size() {
      return ruleSet.size();
   }

   @Override
   public Iterator<TransformationRule> iterator() {
      return ruleSet.iterator();
   }
}
