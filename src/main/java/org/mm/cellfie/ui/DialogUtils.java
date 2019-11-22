package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import java.awt.Component;
import java.io.File;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.core.ui.util.UIUtil;
import com.google.common.collect.Sets;

/**
 * A utility class for creating a uniform dialog UI following Protege look and feel.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class DialogUtils {

   public static int showConfirmDialog(@Nonnull Component parent, @Nonnull String message) {
      checkNotNull(parent);
      checkNotNull(message);
      return showDialog(parent, "Confirm", new JLabel(message),
            JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
   }

   public static int showConfirmWithCancelDialog(@Nonnull Component parent, @Nonnull String message) {
      checkNotNull(parent);
      checkNotNull(message);
      return showDialog(parent, "Confirm", new JLabel(message),
            JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
   }

   public static int showInfoDialog(@Nonnull Component parent, @Nonnull String message) {
      checkNotNull(parent);
      checkNotNull(message);
      return showDialog(parent, "Info", new JLabel(message),
            JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
   }

   public static int showErrorDialog(@Nonnull Component parent, @Nonnull String message) {
      checkNotNull(parent);
      checkNotNull(message);
      return showDialog(parent, "Error", new JLabel(message), JOptionPane.ERROR_MESSAGE,
            JOptionPane.DEFAULT_OPTION);
   }

   public static int showDialog(@Nonnull Component parent, @Nonnull String title,
         @Nonnull JComponent content, int messageType, int optionType) {
      checkNotNull(parent);
      checkNotNull(title);
      checkNotNull(content);
      return JOptionPaneEx.showConfirmDialog(parent, title, content, messageType,
            optionType, null);
   }

   public static File showOpenFileChooser(@Nonnull Component parent, @Nonnull String fileDescription,
         @Nonnull String... extensions) {
      checkNotNull(parent);
      checkNotNull(extensions);
      checkNotNull(fileDescription);
      return UIUtil.openFile(parent, "Open File", fileDescription, Sets.newHashSet(extensions));
   }

   public static File showSaveFileChooser(@Nonnull Component parent, @Nonnull String fileDescription,
         @Nonnull String... extensions) {
      checkNotNull(parent);
      checkNotNull(extensions);
      checkNotNull(fileDescription);
      return UIUtil.saveFile(parent, "Save File", fileDescription, Sets.newHashSet(extensions),
            null);
   }
}
