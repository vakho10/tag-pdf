package ge.vakho.tag_pdf.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lowagie.text.pdf.PdfName;

public class TaggerModel {

	private String rootTagName;
	private List<Parameter> parameters;

	public TaggerModel() {
		parameters = new ArrayList<>();
	}

	public TaggerModel(String rootTagName) {
		this();
		this.rootTagName = rootTagName;
	}

	public TaggerModel(String rootTagName, List<Parameter> parameters) {
		this(rootTagName);
		this.parameters = parameters;
	}

	public String getRootTagName() {
		return rootTagName;
	}

	public void setRootTagName(String rootTagName) {
		this.rootTagName = rootTagName;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public static class Parameter {

		private String key;
		private Object value;

		private boolean needsTagging;
		private String tagName;
		private PdfName mappedToType;

		public Parameter() {
		}

		public Parameter(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		public Parameter(String key, Object value, boolean needsTagging, String tagName, PdfName mappedToType) {
			this(key, value);
			this.needsTagging = needsTagging;
			this.tagName = tagName;
			this.mappedToType = mappedToType;
		}

		public PdfName getMappedToType() {
			return mappedToType;
		}

		public void setMappedToType(PdfName mappedToType) {
			this.mappedToType = mappedToType;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public boolean isNeedsTagging() {
			return needsTagging;
		}

		public void setNeedsTagging(boolean needsTagging) {
			this.needsTagging = needsTagging;
		}

		public String getTagName() {
			return tagName;
		}

		public void setTagName(String tagName) {
			this.tagName = tagName;
		}

	}

	public Parameter getParameterFor(Object value) {
		return parameters.parallelStream() //
				.filter(p -> p.getValue().equals(value) && p.isNeedsTagging()) //
				.findFirst().orElse(null);
	}

	public Map<String, Object> toParameters() {
		Map<String, Object> params = new HashMap<>();
		for (TaggerModel.Parameter parameter : parameters) {
			params.put(parameter.getKey(), parameter.getValue());
		}
		return params;
	}
}