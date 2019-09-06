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

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleReader {

   @Nonnull
   public static TransformationRuleSet readFromDocument(@Nonnull InputStream inputStream)
         throws FileNotFoundException {
      checkNotNull(inputStream);
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
      return new Gson().fromJson(br, TransformationRuleSet.class);
   }

   @Nonnull
   public static TransformationRuleSet readFromDocument(@Nonnull File file)
         throws FileNotFoundException {
      checkNotNull(file);
      BufferedReader br = new BufferedReader(new FileReader(file));
      return new Gson().fromJson(br, TransformationRuleSet.class);
   }
}
