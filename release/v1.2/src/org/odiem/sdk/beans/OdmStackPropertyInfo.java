package org.odiem.sdk.beans;
public class OdmStackPropertyInfo {

		private String choices[];
		private String description;
		private String name;
		private boolean required;
		private String defaultValue;

		public OdmStackPropertyInfo(String name, boolean required, String description,
				String choices[], String defaultValue) {
			this.name = name;
			this.required = required;
			this.description = description;
			this.choices = choices;
			this.defaultValue = defaultValue;
		}

		public String[] getChoices() {
			return choices;
		}

		public void setChoices(String[] choices) {
			this.choices = choices;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}

	}