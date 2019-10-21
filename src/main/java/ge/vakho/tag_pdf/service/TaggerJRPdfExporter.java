package ge.vakho.tag_pdf.service;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.lowagie.text.DocumentException;

import ge.vakho.tag_pdf.service.TaggerModel.Parameter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintEllipse;
import net.sf.jasperreports.engine.JRPrintFrame;
import net.sf.jasperreports.engine.JRPrintImage;
import net.sf.jasperreports.engine.JRPrintLine;
import net.sf.jasperreports.engine.JRPrintRectangle;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.export.AbstractPdfTextRenderer;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.PdfGlyphRenderer;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.export.PdfReportConfiguration;

/**
 * This is a duplicate class of {@link JRPdfExporter} class. It has some methods
 * and fields added to support custom tag marking.
 * 
 * @author v.laluashvili
 */
public class TaggerJRPdfExporter extends JRPdfExporter {

	private TaggerModel taggerModel;

	public TaggerJRPdfExporter(TaggerModel taggerModel) {
		this.taggerModel = taggerModel;

		// Reinitialize tagHelper with our one!
		this.tagHelper = new TaggerJRPdfExporterTagHelper(this, taggerModel);
	}

	@Override
	protected void exportElements(Collection<JRPrintElement> elements)
			throws DocumentException, IOException, JRException {

		if (elements != null && elements.size() > 0) {
			for (Iterator<JRPrintElement> it = elements.iterator(); it.hasNext();) {
				JRPrintElement element = it.next();

				if (filter == null || filter.isToExport(element)) {

					// Modified this!
					((TaggerJRPdfExporterTagHelper) tagHelper).startElement(element);

					if (element instanceof JRPrintLine) {
						exportLine((JRPrintLine) element);
					} else if (element instanceof JRPrintRectangle) {
						exportRectangle((JRPrintRectangle) element);
					} else if (element instanceof JRPrintEllipse) {
						exportEllipse((JRPrintEllipse) element);
					} else if (element instanceof JRPrintImage) {
						exportImage((JRPrintImage) element);
					} else if (element instanceof JRPrintText) {
						exportText((JRPrintText) element);
					} else if (element instanceof JRPrintFrame) {
						exportFrame((JRPrintFrame) element);
					} else if (element instanceof JRGenericPrintElement) {
						exportGenericElement((JRGenericPrintElement) element);
					}

					// Modified this!
					((TaggerJRPdfExporterTagHelper) tagHelper).endElement(element);
				}
			}
		}
	}

	public void exportText(JRPrintText text) throws DocumentException {
		JRStyledText styledText = styledTextUtil.getProcessedStyledText(text, noBackcolorSelector, null);

		if (styledText == null) {
			return;
		}

		AbstractPdfTextRenderer textRenderer = getTextRenderer(text, styledText);
		textRenderer.initialize(this, pdfContentByte, text, styledText, getOffsetX(), getOffsetY());

		double angle = 0;

		switch (text.getRotationValue()) {
		case LEFT: {
			angle = Math.PI / 2;
			break;
		}
		case RIGHT: {
			angle = -Math.PI / 2;
			break;
		}
		case UPSIDE_DOWN: {
			angle = Math.PI;
			break;
		}
		case NONE:
		default: {
		}
		}

		AffineTransform atrans = new AffineTransform();
		atrans.rotate(angle, textRenderer.getX(), pageFormat.getPageHeight() - textRenderer.getY());
		pdfContentByte.transform(atrans);

		if (text.getModeValue() == ModeEnum.OPAQUE) {
			Color backcolor = text.getBackcolor();
			pdfContentByte.setRGBColorFill(backcolor.getRed(), backcolor.getGreen(), backcolor.getBlue());
			pdfContentByte.rectangle(textRenderer.getX(), pageFormat.getPageHeight() - textRenderer.getY(),
					textRenderer.getWidth(), -textRenderer.getHeight());
			pdfContentByte.fill();
		}

		// Added this fragment!
		boolean glyphRendererAddActualText = propertiesUtil
				.getBooleanProperty(PdfReportConfiguration.PROPERTY_GLYPH_RENDERER_ADD_ACTUAL_TEXT, false);

		// Will always call the same tagging method...
		String strText = null;
		if (glyphRendererAddActualText && textRenderer instanceof PdfGlyphRenderer) {
			strText = styledText.getText();
		} else {
			strText = text.getOriginalText();
		}

		// Check if it needs marking its content...
		Parameter parameter = taggerModel.getParameterFor(strText);
		if (!strText.isEmpty() && parameter != null && parameter.isNeedsTagging()) {
			((TaggerJRPdfExporterTagHelper) this.tagHelper).startText(strText, parameter.getTagName(),
					text.getLinkType() != null);
		}

		/* rendering only non empty texts */
		if (styledText.length() > 0) {
			textRenderer.render();
		}

		// Check if it needs marking its content...
		if (!strText.isEmpty() && parameter != null && parameter.isNeedsTagging()) {
			((TaggerJRPdfExporterTagHelper) tagHelper).endText();
		}

		atrans = new AffineTransform();
		atrans.rotate(-angle, textRenderer.getX(), pageFormat.getPageHeight() - textRenderer.getY());
		pdfContentByte.transform(atrans);

		/*   */
		exportBox(text.getLineBox(), text);
	}

}