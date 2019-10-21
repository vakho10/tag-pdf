package ge.vakho.tag_pdf.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

/**
 * Demo service class which generates tagged PDF document using *.jrxml template
 * file.
 * 
 * @author v.laluashvili
 */
public class TaggerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaggerService.class);

	public byte[] generateTaggedDocument(TaggerModel taggerModel) throws IOException, JRException {

		// Load template
		JasperReport report = loadTemplate();

		// Generate parameters
		Map<String, Object> parameters = taggerModel.toParameters();

		// Empty data source
		JREmptyDataSource emptyDataSource = new JREmptyDataSource();

		JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, emptyDataSource);

		TaggerJRPdfExporter exporter = new TaggerJRPdfExporter(taggerModel);

		SimpleExporterInput simpleExporterInput = new SimpleExporterInput(jasperPrint);
		exporter.setExporterInput(simpleExporterInput);
		SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
		configuration.setTagged(true);
		configuration.setMetadataCreator("Your organization's name");

		exporter.setConfiguration(configuration);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			SimpleOutputStreamExporterOutput simpleOutputStreamExporterOutput = new SimpleOutputStreamExporterOutput(
					baos);
			exporter.setExporterOutput(simpleOutputStreamExporterOutput);
			exporter.exportReport();
			return baos.toByteArray();
		}
	}

	private JasperReport loadTemplate() throws IOException, JRException {
		try (InputStream reportInputStream = TaggerService.class.getResourceAsStream("/tagged.jrxml")) {
			JasperDesign jasperDesign = JRXmlLoader.load(reportInputStream);
			return JasperCompileManager.compileReport(jasperDesign);
		}
	}
}