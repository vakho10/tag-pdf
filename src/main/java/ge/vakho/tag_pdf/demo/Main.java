package ge.vakho.tag_pdf.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.text.PDFMarkedContentExtractor;

import com.lowagie.text.pdf.PdfName;

import ge.vakho.tag_pdf.service.TaggerService;
import ge.vakho.tag_pdf.service.TaggerModel;
import ge.vakho.tag_pdf.service.TaggerModel.Parameter;

public class Main {

	public static void main(String[] args) throws Exception {

		// Generate document...
		TaggerModel taggerModel = new TaggerModel();
		taggerModel.setRootTagName("RootTagName");

		List<Parameter> parameters = new ArrayList<>();
		parameters.add(new Parameter("param1", "Some unique value1 :)", true, "Tag1", PdfName.TEXT));
		parameters.add(new Parameter("param2", "Some unique value2 :)", true, "Tag2", PdfName.TEXT));
		parameters.add(new Parameter("param3", "Some unique untagged value3"));
		taggerModel.setParameters(parameters);

		Path path = Paths.get("tagged.pdf");
		Files.write(path, new TaggerService().generateTaggedDocument(taggerModel));

		// Read meta data :)
		PDDocument pdDocument = null;
		pdDocument = PDDocument.load(path.toFile());

		PDPageTree pages = pdDocument.getPages();
		pages.forEach(page -> {
			PDFMarkedContentExtractor extractor = null;
			try {
				extractor = new PDFMarkedContentExtractor();
				extractor.processPage(page); // Process the page

				// Read marked contents...
				List<PDMarkedContent> markedContents = extractor.getMarkedContents();
				for (PDMarkedContent mt : markedContents) {
					System.out.println(mt.getTag());
					System.out.println(mt.getActualText()); // Charset breaks when called concatenated with UTF-8 :(
					System.out.println();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}