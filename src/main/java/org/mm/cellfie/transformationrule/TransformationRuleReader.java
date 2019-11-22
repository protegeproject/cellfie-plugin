package org.mm.cellfie.transformationrule;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.annotation.Nonnull;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleReader {

   @Nonnull
   public static TransformationRuleList readFromDocument(@Nonnull InputStream inputStream)
         throws FileNotFoundException {
      checkNotNull(inputStream);
      try {
         BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
         return new Gson().fromJson(br, TransformationRuleList.class);
      } catch (JsonSyntaxException e) {
         // Handle previous version of transformation rules serialization
         BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
         TransformationRuleSet ruleSet = new Gson().fromJson(br, TransformationRuleSet.class);
         return new TransformationRuleList(ruleSet.getTransformationRules());
      }
   }

   @Nonnull
   public static TransformationRuleList readFromDocument(@Nonnull File file)
         throws FileNotFoundException {
      checkNotNull(file);
      try {
         BufferedReader br = new BufferedReader(new FileReader(file));
         return new Gson().fromJson(br, TransformationRuleList.class);
      } catch (JsonSyntaxException e) {
         // Handle previous version of transformation rules serialization
         BufferedReader br = new BufferedReader(new FileReader(file));
         TransformationRuleSet ruleSet = new Gson().fromJson(br, TransformationRuleSet.class);
         return new TransformationRuleList(ruleSet.getTransformationRules());
      }
   }
}
