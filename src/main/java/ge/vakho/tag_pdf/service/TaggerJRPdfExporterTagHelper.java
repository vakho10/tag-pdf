package ge.vakho.tag_pdf.service;

import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfStructureElement;
import com.lowagie.text.pdf.PdfStructureTreeRoot;
import com.lowagie.text.pdf.PdfWriter;

import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintImage;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterTagHelper;

public class TaggerJRPdfExporterTagHelper extends JRPdfExporterTagHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaggerJRPdfExporterTagHelper.class);

	private final TaggerModel taggerModel;

	protected TaggerJRPdfExporterTagHelper(JRPdfExporter exporter, TaggerModel taggerModel) {
		super(exporter);
		this.taggerModel = taggerModel;
	}

	@Override
	protected void startElement(JRPrintElement element) {
		super.startElement(element);
	}

	@Override
	protected void endElement(JRPrintElement element) {
		super.endElement(element);
	}

	@Override
	protected void startImage(JRPrintImage printImage) {
		LOGGER.debug("Ignored startImage call");
	}

	@Override
	protected void endImage() {
		LOGGER.debug("Ignored endImage call");
	}

	@Override
	protected void startPageAnchor() {
		LOGGER.debug("Ignored startPageAnchor call");
	}

	@Override
	protected void endPageAnchor() {
		LOGGER.debug("Ignored endPageAnchor call");
	}

	@Override
	protected void init(PdfContentByte pdfContentByte) {
		this.pdfContentByte = pdfContentByte;

		if (isTagged) {
			PdfStructureTreeRoot root = pdfWriter.getStructureTreeRoot();

			// Map custom tag names to the existing ones...
			taggerModel.getParameters().parallelStream() //
					.filter(i -> i.isNeedsTagging()) //
					.forEach(i -> root.mapRole(new PdfName(i.getTagName()), i.getMappedToType()));

			// Root element (SECT)
			PdfName pdfNameRoot = new PdfName(taggerModel.getRootTagName());
//			root.mapRole(pdfNameRoot, PdfName.SECT); // Root element needs no mapping

			root.mapRole(PdfName.IMAGE, PdfName.FIGURE);
			root.mapRole(PdfName.TEXT, PdfName.TEXT);
			allTag = new PdfStructureElement(root, pdfNameRoot);
			if (pdfWriter.getPDFXConformance() == PdfWriter.PDFA1A) {
				root.mapRole(new PdfName("Anchor"), PdfName.NONSTRUCT);
				root.mapRole(PdfName.TEXT, PdfName.SPAN);
			} else {
				root.mapRole(new PdfName("Anchor"), PdfName.TEXT);
			}

			if (language != null) {
				allTag.put(PdfName.LANG, new PdfString(language));
			}
			tagStack = new Stack<PdfStructureElement>();
			tagStack.push(allTag);
		}
	}

	/**
	 * This is guaranteed to be called if the value is tagged!
	 */
	@Override
	protected void startText(String text, boolean isHyperlink) {
		startText(text, PdfName.TEXT, isHyperlink);
	}

	public void startText(String text, String tagName, boolean isHyperlink) {
		startText(text, new PdfName(tagName), isHyperlink);
	}

	public void startText(String text, PdfName tagPdfName, boolean isHyperlink) {
		if (isTagged) {
			PdfDictionary markedContentProps = new PdfDictionary();
			markedContentProps.put(PdfName.ACTUALTEXT, new PdfString(text, PdfObject.TEXT_UNICODE));
			PdfStructureElement textTag = new PdfStructureElement(tagStack.peek(),
					isHyperlink ? PdfName.LINK : tagPdfName);
			// the following method is part of the patched iText
			pdfContentByte.beginMarkedContentSequence(textTag, markedContentProps);
		}
	}

	/**
	 * This is guaranteed to be called if the value is tagged!
	 */
	@Override
	protected void endText() {
		if (isTagged) {
			pdfContentByte.endMarkedContentSequence();
			isTagEmpty = false;
		}
	}

}