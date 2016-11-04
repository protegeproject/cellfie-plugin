package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.awt.BorderLayout;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.mm.workbook.Sheet;
import org.protege.editor.core.ui.tabbedpane.ViewTabbedPane;
import org.protege.editor.core.ui.util.ComponentFactory;

/**
 * Represents the workbook view used to present the UI for the Excel workbook.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class WorkbookView extends JPanel {

   private static final long serialVersionUID = 1L;

   private ViewTabbedPane tabSheetContainer;

   public WorkbookView(@Nonnull CellfieWorkspace cellfieWorkspace) {
      checkNotNull(cellfieWorkspace);
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      JPanel pnlContainer = new JPanel();
      pnlContainer.setLayout(new BorderLayout());
      pnlContainer.setBorder(ComponentFactory.createTitledBorder(
            format("Workbook (%s)", cellfieWorkspace.getWorkbookFile().getAbsolutePath())));
      add(pnlContainer, BorderLayout.CENTER);

      JPanel pnlWorkbook = new JPanel();
      pnlWorkbook.setLayout(new BorderLayout());
      pnlWorkbook.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      tabSheetContainer = new ViewTabbedPane();
      pnlWorkbook.add(tabSheetContainer, BorderLayout.CENTER);
      pnlContainer.add(pnlWorkbook, BorderLayout.CENTER);

      for (Sheet sheet : cellfieWorkspace.getActiveWorkbook().getSheets()) {
         SheetPanel sheetPanel = new SheetPanel(sheet);
         tabSheetContainer.addTab(sheet.getName(), null, sheetPanel);
      }
      validate();
   }

   public CellRange getSelectedCellRange() {
      SheetPanel selectedSheetPanel = (SheetPanel) tabSheetContainer.getSelectedComponent();
      return selectedSheetPanel.getSelectedCellRange();
   }
}
