package pt.floraon.occurrences;

import pt.floraon.driver.datatypes.NumericInterval;

/**
 * Represents the abundance of a taxon in an obsrevation
 */
public class Abundance extends NumericInterval {
    public Abundance(String text) {
        super(text);
    }
    private transient boolean detected;


    protected void parseText() {  // lazy parsing
        if(parsed) return;
        super.parseText();

        if(this.error != null) {
            if(this.text.trim().toUpperCase().equals("ND")) {
                this.exactValue = null;
                this.detected = false;
                this.error = null;
            } else {
                this.detected = true;   // if field has unparseable text, assume it was detected
            }
        } else
            this.detected =
                    (this.exactValue != null && this.exactValue > 0)
                    || (this.maxValue != null && this.maxValue > 0)
                    || (this.minValue != null && this.minValue > 0)
                    || this.isEmpty();  // if field is empty, assume it was detected
        parsed = true;
    }

    public boolean wasDetected() {
        parseText();
        return this.detected;
    }
}
