package pdf;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDComboBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDListBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;

/**
 * Utility to generate PDFs.
 * 
 * @author Torsten Oltmanns
 *
 */
public class PdfGenerator {

    /**
     * Create a new document.
     * 
     * @return the document
     */
    public static PDDocument createDocument() {
        return new PDDocument();
    }


    /**
     * Add a new page to the specified document.
     * 
     * @param doc the document
     * @return the new page content stream
     * @throws IOException
     */
    public static PDPage addPage(final PDDocument document) throws IOException {
        final PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        return page;
    }


    public static PDPageContentStream getPageContentStream(final PDDocument document, final PDPage page)
            throws IOException {
        return new PDPageContentStream(document, page);
    }


    /**
     * Adds a text to the page content with the specified font at the current position.
     * 
     * @param contentStream the content stream
     * @param font the font
     * @param fontSize the font size
     * @param text the text
     * @throws IOException
     */
    public static void addText(final PDPageContentStream contentStream, final PDFont font, final int fontSize,
            final String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.showText(text);
        contentStream.endText();
    }


    /**
     * Adds a text to the page content with the specified font and position offsets.
     * 
     * @param contentStream the content stream
     * @param font the font
     * @param fontSize the font size
     * @param text the text
     * @param offsetX the X offset
     * @param offsetY the Y offset
     * @throws IOException
     */
    public static void addText(final PDPageContentStream contentStream, final PDFont font, final int fontSize,
            final String text, final float offsetX, final float offsetY) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(offsetX, offsetY);
        contentStream.showText(text);
        contentStream.endText();
    }


    /**
     * Adds an image from file at the specified offsets.
     * 
     * @param document the document
     * @param contentStream the content stream
     * @param imageFile the image file
     * @param offsetX the X offset
     * @param offsetY the Y offset
     * @throws IOException
     */
    public static void addImage(final PDDocument document, final PDPageContentStream contentStream, final File imageFile,
            final float offsetX, final float offsetY) throws IOException {
        final PDImageXObject pdImage = PDImageXObject.createFromFileByExtension(imageFile, document);
        addImage(contentStream, pdImage, offsetX, offsetY);
    }


    /**
     * Adds the image at the specified offsets.
     * 
     * @param document the document
     * @param contentStream the content stream
     * @param image the image
     * @param offsetX the X offset
     * @param offsetY the Y offset
     * @throws IOException
     */
    public static void addImage(final PDDocument document, final PDPageContentStream contentStream,
            final BufferedImage image, final float offsetX, final float offsetY) throws IOException {
        final PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);
        addImage(contentStream, pdImage, offsetX, offsetY);
    }


    /**
     * Adds the image at the specified offsets.
     * 
     * @param contentStream the content stream
     * @param image the image
     * @param offsetX the X offset
     * @param offsetY the Y offset
     * @throws IOException
     */
    public static void addImage(final PDPageContentStream contentStream, final PDImageXObject image, final float offsetX,
            final float offsetY) throws IOException {
        final float scale = 1f;
        contentStream.drawImage(image, offsetX, offsetY, image.getWidth() * scale, image.getHeight() * scale);
    }


    /**
     * Adds the specified AWT {@link Component} as an image at the specified offsets.
     * 
     * @param document the document
     * @param contentStream the content stream
     * @param component the component
     * @param offsetX the X offset
     * @param offsetY the Y offset
     * @throws IOException
     */
    public static void addAWTComponentAsImage(final PDDocument document, final PDPageContentStream contentStream,
            final Component component, final float offsetX, final float offsetY) throws IOException {
        final BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();
        component.paintAll(g);
        addImage(document, contentStream, image, offsetX, offsetY);
    }


    /**
     * Closes the specified content stream.
     * 
     * @param contentStream the content stream
     * @throws IOException
     */
    public static void closeContent(final PDPageContentStream contentStream) throws IOException {
        contentStream.close();
    }


    /**
     * Saves the document under the specified filename.
     * 
     * @param document the document
     * @param filename the filename
     * @throws IOException
     */
    public static void saveDocument(final PDDocument document, final String filename) throws IOException {
        try {
            document.save(filename);
        } finally {
            document.close();
        }
    }


    /**
     * Add a form to the specified document using the specified the default font.
     * 
     * @param document the document
     * @param defaultFont the default font to use
     * @return the form
     */
    public static PDAcroForm addForm(final PDDocument document, final PDFont defaultFont) {
        // set the default font for the form
        final PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Form-DefaultFont"), defaultFont);

        // create a new form and add it to the document
        final PDAcroForm acroForm = new PDAcroForm(document);
        document.getDocumentCatalog().setAcroForm(acroForm);

        // set the form resources
        acroForm.setDefaultResources(resources);

        // set form default appearance
        final String defaultAppearanceString = "/Form-DefaultFont 0 Tf 0 g";
        acroForm.setDefaultAppearance(defaultAppearanceString);

        return acroForm;
    }


    /**
     * Adds a field to the form at the specified position and bounding rectangle.
     * 
     * @param page the page to add the field to at the specified position
     * @param acroForm the form to add the field
     * @param field the field
     * @param font the font for the field
     * @param fontSize the font size
     * @param x the X offset on the page
     * @param y the Y offset on the page
     * @param width the width of the field
     * @param height the height of the field
     * @throws IOException
     */
    public static void addField(final PDPage page, final PDAcroForm acroForm, final PDField field, final PDFont font,
            final int fontSize, final float x, final float y, final float width, final float height) throws IOException {

        final String fieldName = field.getPartialName();
        final PDResources resources = acroForm.getDefaultResources();
        resources.put(COSName.getPDFName("Font-" + fieldName), font);

        if (field instanceof PDVariableText) {
            final String defaultAppearanceString = "/Font-" + fieldName + " " + fontSize + " Tf 0 g";
            ((PDVariableText) field).setDefaultAppearance(defaultAppearanceString);
        }

        // add the field to the form
        acroForm.getFields().add(field);

        // set the annotation for the field
        final PDAnnotationWidget widget = field.getWidgets().get(0);
        final PDRectangle rect = new PDRectangle(x, y, width, height);
        widget.setRectangle(rect);

        // add the annotation and field to the page
        page.getAnnotations().add(widget);
    }


    /**
     * Sets the value of the specified field.
     * 
     * @param document the document
     * @param fieldName the name of the field
     * @param value the value for the field
     * @throws IOException
     */
    public static void setFieldValue(final PDDocument document, final String fieldName, final String value)
            throws IOException {
        final PDDocumentCatalog docCatalog = document.getDocumentCatalog();
        final PDAcroForm acroForm = docCatalog.getAcroForm();
        final PDField field = acroForm.getField(fieldName);

        if (field != null) {
            if (field instanceof PDCheckBox) {
                field.setValue(value); // e.g. "yes"
            } else if (field instanceof PDComboBox) {
                field.setValue(value);
            } else if (field instanceof PDListBox) {
                field.setValue(value);
            } else if (field instanceof PDRadioButton) {
                field.setValue(value);
            } else if (field instanceof PDTextField) {
                field.setValue(value);
            }
        } else {
            System.err.println("No field found with name:" + fieldName);
        }
    }


    protected static void testSimpleText(final PDDocument document, final PDPageContentStream contentStream)
            throws IOException {
        final PDFont font = PDFontFactory.createDefaultFont();
        final int fontSize = 12;

        addText(contentStream, font, fontSize, "Hello world!", PDRectangle.A4.getLowerLeftX(),
                PDRectangle.A4.getUpperRightY() - fontSize);
    }


    protected static void testSimpleForm(final PDDocument document, final PDPage page) throws IOException {
        final PDFont font = PDType1Font.HELVETICA;
        final int fontSize = 8;
        final PDAcroForm form = addForm(document, font);

        // Add a form field to the form.
        final String fieldName = "Test";
        final PDTextField textBox = new PDTextField(form);
        textBox.setPartialName(fieldName);

        addField(page, form, textBox, font, fontSize, PDRectangle.A4.getLowerLeftX(), PDRectangle.A4.getUpperRightY() - 30,
                100, fontSize * 2);

        setFieldValue(document, fieldName, "Test-value");
    }


    protected static void testLoadAndSetValue(final String filename) throws IOException {
        final PDDocument document = PDDocument.load(new File(filename));
        try {
            setFieldValue(document, "Test", "blabla");
            document.save(filename);
        } finally {
            document.close();
        }
    }


    public static void main(final String[] args) throws IOException {
        final PDDocument document = createDocument();
        try {
            final PDPage page = addPage(document);
            final PDPageContentStream contentStream = getPageContentStream(document, page);

            testSimpleText(document, contentStream);
            testSimpleForm(document, page);
            closeContent(contentStream);

            saveDocument(document, "test.pdf");
        } catch (final IOException e) {
            e.printStackTrace();

            try {
                document.close();
            } catch (final IOException e1) {
                e1.printStackTrace();
            }
        }

        testLoadAndSetValue("test.pdf");
    }
}
