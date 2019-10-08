package pdf;

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;

import javax.print.attribute.PrintRequestAttributeSet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;

/**
 * Utility for printing PDFs.
 * 
 * @author Torsten Oltmanns
 */
public final class PdfPrinting {

  /**
   * Prints the document at its actual size. This is the recommended way to print.
   * 
   * @param document the document
   * @throws PrinterException
   * @throws IOException
   */
  public static void print(final PDDocument document) throws IOException, PrinterException {
    final PrinterJob job = PrinterJob.getPrinterJob();
    job.setPageable(new PDFPageable(document));
    job.print();
  }

  /**
   * Prints using custom PrintRequestAttribute values. E.g. <br/>
   * <code>final PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();</code><br/>
   * <code>attr.add(new PageRanges(1, 1)); // pages 1 to 1</code><br/>
   * 
   * @param document the document
   * @param attributes the print attributes
   * @throws PrinterException
   * @throws IOException
   */
  public static void printWithAttributes(final PDDocument document, final PrintRequestAttributeSet attributes)
      throws IOException, PrinterException {
    final PrinterJob job = PrinterJob.getPrinterJob();
    job.setPageable(new PDFPageable(document));

    job.print(attributes);
  }

  /**
   * Prints with a print preview dialog.
   * 
   * @param document the document
   * @throws PrinterException
   * @throws IOException
   */
  public static void printWithDialog(final PDDocument document) throws IOException, PrinterException {
    final PrinterJob job = PrinterJob.getPrinterJob();
    job.setPageable(new PDFPageable(document));

    if (job.printDialog()) {
      job.print();
    }
  }

  /**
   * Prints with a print preview dialog and custom PrintRequestAttribute values.
   * <code>final PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();</code><br/>
   * <code>attr.add(new PageRanges(1, 1)); // pages 1 to 1</code><br/>
   * 
   * @param document the document
   * @param attributes the print attributes
   * @throws PrinterException
   * @throws IOException
   */
  public static void printWithDialogAndAttributes(final PDDocument document, final PrintRequestAttributeSet attributes)
      throws IOException, PrinterException {
    final PrinterJob job = PrinterJob.getPrinterJob();
    job.setPageable(new PDFPageable(document));

    if (job.printDialog(attributes)) {
      job.print(attributes);
    }
  }

  /**
   * Prints using a custom page size and custom margins.
   *
   * @param document the document
   * @param format the paper format
   * @throws PrinterException
   * @throws IOException
   */
  public static void printWithPaper(final PDDocument document, final PDRectangle format)
      throws IOException, PrinterException {
    printWithPaper(document, format, 0, 0, format.getWidth(), format.getHeight());
  }

  /**
   * Prints using a custom page size and custom margins.
   * 
   * @param document the document
   * @param format the paper format
   * @param marginLeft the left margin
   * @param marginRight the right margin
   * @param marginTop the top margin
   * @param marginBottom the bottom margin
   * @throws IOException
   * @throws PrinterException
   */
  public static void printWithPaper(final PDDocument document, final PDRectangle format, final float marginLeft,
      final float marginRight, final float marginTop, final float marginBottom) throws IOException, PrinterException {
    final PrinterJob job = PrinterJob.getPrinterJob();
    job.setPageable(new PDFPageable(document));

    // define paper format
    final Paper paper = new Paper();
    paper.setSize(format.getWidth(), format.getHeight()); // 1/72 inch

    // set margins
    paper.setImageableArea(marginLeft, marginTop, format.getWidth() - marginLeft - marginRight,
        format.getHeight() - marginTop - marginBottom);

    // custom page format
    final PageFormat pageFormat = new PageFormat();
    pageFormat.setPaper(paper);

    // override the page format
    final Book book = new Book();
    // append all pages
    book.append(new PDFPrintable(document), pageFormat, document.getNumberOfPages());
    job.setPageable(book);

    job.print();
  }
}
