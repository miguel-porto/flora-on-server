package pt.floraon.occurrences;

import pt.floraon.driver.datatypes.IntegerInterval;

/**
 * Represents the abundance of a taxon in an observation.
 * If the texti is ND or 0, wasDetected() returns false.
 */
public class Abundance extends IntegerInterval {
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
