package pt.floraon.occurrences.fields.flavours;

import java.io.Serializable;
import java.util.Objects;

public class CustomOccurrenceFlavour extends GeneralOccurrenceFlavour implements IOccurrenceFlavour, Serializable {
    private String[] fields;
    private boolean showInOccurrenceView, showInInventoryView;
    private String name;

    public CustomOccurrenceFlavour() {
    }

    public CustomOccurrenceFlavour(String[] fields, String name, boolean showInOccurrenceView, boolean showInInventoryView) {
        this.fields = fields;
        this.name = name;
        this.showInInventoryView = showInInventoryView;
        this.showInOccurrenceView = showInOccurrenceView;
    }

    @Override
    public String[] getFields() {
        return fields;
    }

    @Override
    public boolean showInOccurrenceView() {
        return isShowInOccurrenceView();
    }

    @Override
    public boolean showInInventoryView() {
        return isShowInInventoryView();
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public boolean isShowInOccurrenceView() {
        return showInOccurrenceView;
    }

    public void setShowInOccurrenceView(boolean showInOccurrenceView) {
        this.showInOccurrenceView = showInOccurrenceView;
    }

    public boolean isShowInInventoryView() {
        return showInInventoryView;
    }

    public void setShowInInventoryView(boolean showInInventoryView) {
        this.showInInventoryView = showInInventoryView;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean containsCoordinates() {
        for(String s : fields) {
            if (s.equals("coordinates")) return true;
        }
        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomOccurrenceFlavour that = (CustomOccurrenceFlavour) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }
}