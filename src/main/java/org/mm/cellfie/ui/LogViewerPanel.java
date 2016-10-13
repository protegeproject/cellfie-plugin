package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledEditorKit;

import org.mm.renderer.text.TextRenderer;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
class LogViewerPanel extends JPanel {

   private static final long serialVersionUID = 1L;

   public LogViewerPanel(@Nonnull String logMessage) {
      checkNotNull(logMessage);
      setPreferredSize(new Dimension(1020, 420));
      setLayout(new BorderLayout());
      // To force the horizontal scrolling
      JTextPane txtLogMessage = createLogTextPanel();
      txtLogMessage.setEditorKit(createHighlightEditorKit());
      txtLogMessage.setText(logMessage);
      txtLogMessage.setEditable(false);
      add(new JScrollPane(txtLogMessage), BorderLayout.CENTER);
   }

   private JTextPane createLogTextPanel() {
      JTextPane txtLogMessage = new JTextPane() {
         private static final long serialVersionUID = 1L;

         @Override
         public boolean getScrollableTracksViewportWidth() {
            // force to show horizontal scrollbar
            return (getSize().width < getParent().getSize().width);
         }

         @Override
         public void setSize(Dimension d) {
            if (d.width < getParent().getSize().width) {
               d.width = getParent().getSize().width;
            }
            super.setSize(d);
         }
      };
      return txtLogMessage;
   }

   private EditorKit createHighlightEditorKit() {
      EditorKit editorKit = new StyledEditorKit() {
         private static final long serialVersionUID = 1L;

         @Override
         public Document createDefaultDocument() {
            return new CommentHighlightDocument();
         }
      };
      return editorKit;
   }

   /*
    * A custom document highlighter to color comments in the log message
    */
   private class CommentHighlightDocument extends DefaultStyledDocument {

      private static final long serialVersionUID = 1L;

      final StyleContext c = StyleContext.getDefaultStyleContext();
      final AttributeSet green = c.addAttribute(c.getEmptySet(), StyleConstants.Foreground,
            new Color(0, 100, 0));

      @Override
      public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
         super.insertString(offset, str, a);
         String content = getText(0, getLength());
         Pattern singleLinecommentsPattern = Pattern.compile(TextRenderer.COMMENT_SYMBOL + ".*");
         Matcher matcher = singleLinecommentsPattern.matcher(content);
         while (matcher.find()) {
            setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), green, false);
         }
      }
   }
}
