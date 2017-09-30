package pt.floraon.occurrences;

import pt.floraon.driver.datatypes.NumericInterval;

/**
 * Represents the abundance of a taxon in an obsrevation
 */
public class Abundance extends NumericInterval {
    public Abundance(String text) {
        super(text);
    }
    private transient boolean detected, parsed = false;


    protected void parseText() {  // lazy parsing
        if(parsed) return;
        super.parseText();

        if(this.error != null) {
            if(this.text.trim().toUpperCase().equals("ND")) {
                this.exactValue = null;
                this.detected = false;
                this.error = null;
            }
        } else
            this.detected = (this.exactValue != null && this.exactValue > 0)
                    || (this.maxValue != null && this.maxValue > 0)
                    || (this.minValue != null && this.minValue > 0);
        parsed = true;
    }

    public boolean wasDetected() {
        parseText();
        return this.detected;
    }
}
