package pt.floraon.occurrences.entities;

import com.arangodb.velocypack.annotations.Expose;
import com.sun.tools.jxc.apt.Const;
import jline.internal.Log;
import pt.floraon.driver.*;
import pt.floraon.driver.annotations.*;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.*;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.fields.parsers.*;

import java.io.Serializable;
import java.util.*;

/**
 * Represents the data associated with an inventory, including unmatched taxa. Those that are matched will be converted
 * to graph links and removed from this entity.
 * Created by miguel on 05-02-2017.
 */
public class Inventory extends GeneralDBNode implements Serializable, DiffableBean, GeoBean {
    /**
     * NOTE: coordinates of the observation have priority over these. Only if observationLatitude and observationLongitude
     * are not set, then we can use these inventory coordinates.
     */
    @SmallField @InventoryField
    @FieldParser(LatitudeLongitudeParser.class)
    @PrettyName(value = "Latitude do inventário", shortName = "Inv lat")
    private Float latitude;
    @SmallField @InventoryField
    @FieldParser(LatitudeLongitudeParser.class)
    @PrettyName(value = "Longitude do inventário", shortName = "Inv long")
    private Float longitude;
    private String spatialRS;
    @SmallField @InventoryField
    @FieldParser(IntegerParser.class)
    @PrettyName(value = "Altitude", shortName = "Alt", alias={"z", "altitude"})
    private Float elevation;
    private String geometry;
    @SmallField @InventoryField
    @FieldParser(IntegerParser.class)
    @PrettyName(value = "Ano", shortName = "Ano", alias="ano")
    private Integer year;
    @SmallField @InventoryField
    @FieldParser(IntegerParser.class)
    @PrettyName(value = "Mês", shortName = "Mês", alias="mês")
    private Integer month;
    @SmallField @InventoryField
    @FieldParser(IntegerParser.class)
    @PrettyName(value = "Dia", shortName = "Dia")
    private Integer day;   // TODO: these cannot be erased...
    @SmallField @InventoryField
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Precisão", shortName = "Prec", important = true)
    private Precision precision;
    private Boolean complete;
    @InventoryField
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Habitat", shortName = "Hab")
    private String habitat;
    @HideInCompactView @InventoryField
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Notas públicas do inventário", shortName = "Notas pub", important = true)
    private String pubNotes;
    @InventoryField @HideInCompactView
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Projecto ou instituição financiadora", shortName = "Proj", alias = {"project"})
    private String credits;
    @HideInCompactView @InventoryField
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Notas privadas do inventário", shortName = "Notas priv", important = true)
    private String privNotes;
    @HideInCompactView @InventoryField
    @FieldParser(StringArrayParser.class)
    @PrettyName(value = "Etiquetas", shortName = "Tags")
    private String[] tags;
    @InventoryField @FieldType(FieldType.Type.AUTHORS)
    @PrettyName(value = "Observadores", shortName = "Observadores", important = true)
    private String[] observers;
    @HideInCompactView @InventoryField @FieldType(FieldType.Type.AUTHORS)
    @PrettyName(value = "Colectores", shortName = "Colectores")
    private String[] collectors;
    @HideInCompactView @InventoryField @FieldType(FieldType.Type.AUTHORS)
    @PrettyName(value = "Determinadores", shortName = "Dets")
    private String[] dets;
    @Deprecated
    private String verbLocality;
    @InventoryField
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Local", shortName = "Local")
    private String locality;
    @InventoryField
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Município", shortName = "Município", alias="concelho")
    private String municipality;
    @InventoryField
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Distrito", shortName = "Distrito", alias="distrito")
    private String province;
    @InventoryField
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Região", shortName = "Região", alias="região")
    private String county;
    @InventoryField @SmallField
    @FieldParser(GeneralFieldParser.class) @MonospaceFont
    @PrettyName(value = "Código do inventário", shortName = "Cod", alias={"código", "inventário"}, important = true)
    private String code;
    @InventoryField
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Ameaças do local", shortName = "Ameaças", alias="ameaças")
    private String threats;
    @InventoryField @HideInCompactView @ReadOnly
    @PrettyName(value = "Responsável", shortName = "Resp")
    private String maintainer;
    @InventoryField
    @FieldParser(FloatParser.class)
    @PrettyName(value = "Área do inventário", shortName = "Área")
    private Float area;
    @InventoryField
    @SmallField @HideInCompactView @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Escala de cobertura", shortName = "Escala", description = "Custom cover/abundance scale (e.g. Braun-Blanquet, etc.)")
    private String coverIndex;
    private String geology;
    private Float meanHeight;
    private Float totalCover;
    private String aspect;
    private Integer slope;

    /**
     * This list holds the occurrencces ({@link OBSERVED_IN}) that are not yet matched to the taxonomic graph.
     * Occurrences that are matched are removed from this list and converted into graph links.
     * All new occurrences shall go in here.
     */
    private List<OBSERVED_IN> unmatchedOccurrences;

    /**
     * This list shall be populated, when needed, with all matched occurrences in this inventory
     * TODO: this is a workaround for now...
     */
    @Expose(serialize = false)
    protected OBSERVED_IN[] taxa;

    /**
     * This list shall be populated, when needed, with the observer names.
     * Remember that only the observer IDs are stored in the field "observers"
     */
    @Expose(serialize = false)
    private String[] observerNames;

    @Expose(serialize = false)
    private String maintainerName;

    @Expose(serialize = false)
    private Float utmX, utmY;

    public Inventory(Inventory other) {
        super(other);
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.spatialRS = other.spatialRS;
        this.elevation = other.elevation;
        this.geometry = other.geometry;
        this.precision = other.precision;
        this.year = other.year;
        this.month = other.month;
        this.day = other.day;
        this.complete = other.complete;
        this.habitat = other.habitat;
        this.pubNotes = other.pubNotes;
        this.privNotes = other.privNotes;
        this.geology = other.geology;
        this.tags = other.tags;
        this.observers = other.observers;
        this.collectors = other.collectors;
        this.dets = other.dets;
        this.verbLocality = other.verbLocality;
        this.locality = other.locality;
        this.municipality = other.municipality;
        this.province = other.province;
        this.county = other.county;
        this.code = other.code;
        this.threats = other.threats;
        this.maintainer = other.maintainer;
        this.area = other.area;
        this.totalCover = other.totalCover;
        this.meanHeight = other.meanHeight;
        this.aspect = other.aspect;
        this.slope = other.slope;
        this.observerNames = other.observerNames;
    }

    public Inventory() { }

    /**
     * Check if latitude-longitude coordinates are set. If not, try to convert from UTM, if set.
     */
    private void checkGeographicCoordinates() {
        if((latitude == null || longitude == null) && utmX != null && utmY != null) {
            // TODO support for UTM zones!!
            LatLongCoordinate llc = CoordinateConversion.UtmToLatLonWGS84(29, 'S', utmX.longValue(), utmY.longValue());
            this.latitude = llc.getLatitude();
            this.longitude = llc.getLongitude();
        }

        if(Constants.isNoData(latitude)) latitude = null;
        if(Constants.isNoData(longitude)) longitude = null;
    }

    /**
     * @return True if there's only one taxon AND it has valid coordinates.
     */
    public boolean shouldGetCoordinatesFromObservation() {
        return _getTaxa() != null && this._getUniqueCoordinate(false) != null;

/*
        if(_getTaxa() != null && _getTaxa().length == 1) {
            return !Constants.isNullOrNoData(_getTaxa()[0].getObservationLatitude())
                    && !Constants.isNullOrNoData(_getTaxa()[0].getObservationLongitude());
        } else return false;
*/
    }

    /**
     * @return
     * - If there is only one observation with coordinates, the latitude of that observation, OR if not
     * - the latitude of the inventory, OR if null,
     * - the average latitude of all the observations.
     */
    public Float _getLatitude() {
        checkGeographicCoordinates();
        if(shouldGetCoordinatesFromObservation()) {
            return _getUniqueCoordinate(false).getLatitude();
        } else {
            if(Constants.isNullOrNoData(latitude)) {
                Float olat = 0f;
                int count = 0;
                for(OBSERVED_IN oi : _getTaxa()) {
                    if(Constants.isNullOrNoData(oi.getObservationLatitude())) continue;
                    olat += oi.getObservationLatitude();
                    count ++;
                }
                return olat == 0 ? null : (olat / count);

            } else return latitude;
        }
    }

    public Float _getLongitude() {
        checkGeographicCoordinates();
        if(shouldGetCoordinatesFromObservation()) {
            return _getUniqueCoordinate(false).getLongitude();
        } else {
            if(Constants.isNullOrNoData(longitude)) {
                Float olng = 0f;
                int count = 0;
                for(OBSERVED_IN oi : _getTaxa()) {
                    if(Constants.isNullOrNoData(oi.getObservationLongitude())) continue;
                    olng += oi.getObservationLongitude();
                    count ++;
                }
                return olng == 0 ? null : (olng / count);
            } else return longitude;
        }
    }

    public String _getCoordinates() {
        Float lat, lng;
        if(Constants.isNullOrNoData(lat = this._getLatitude()) || Constants.isNullOrNoData(lng = this._getLongitude()))
            return "*";
        else
            return String.format(Locale.ROOT, "%.5f, %.5f", lat, lng);
    }

    /**
     * Similar to _getLatitude but gives priority to inventory coordinates.
     * @return
     * - The latitude of the inventory, OR if null,
     * - If there is only one observation with coordinates, the latitude of that observation, OR if not
     * - the average latitude of all the observations.
     */
    public Float _getInventoryLatitude() {
        checkGeographicCoordinates();
        return latitude == null ? _getLatitude() : latitude;
    }

    public Float _getInventoryLongitude() {
        checkGeographicCoordinates();
        return longitude == null ? _getLongitude() : longitude;
    }

    /**
     * This gets the coordinates of the Inventory. If they are null, and the inventory <b>has only one observation</b>, returns
     * the coordinates of that observation in parenthesis.
     * @return
     */
    public String _getInventoryCoordinates() {
        if(this.latitude == null || this.longitude == null) {
            if(Constants.isNullOrNoData(this._getLatitude()) || Constants.isNullOrNoData(this._getLongitude()))
                return "*";
            else
                return "* (" + this._getCoordinates() + ")";
        } else
            return String.format(Locale.ROOT, "%.5f, %.5f", this._getInventoryLatitude(), this._getInventoryLongitude());
    }

    @Override
    public void _setUTMX(Float x) {
        this.utmX = x;
    }

    @Override
    public Float _getsetUTMX() {
        return this.utmX;
    }

    @Override
    public void _setUTMY(Float y) {
        this.utmY = y;
    }

    @Override
    public Float _getsetUTMY() {
        return this.utmY;
    }

    @Override
    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    @Override
    public Float getLatitude() {
        checkGeographicCoordinates();
        return this.latitude;
    }

    @Override
    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    @Override
    public Float getLongitude() {
        checkGeographicCoordinates();
        return this.longitude;
    }

    public String getSpatialRS() {
        return spatialRS;
    }

    public void setSpatialRS(String spatialRS) {
        this.spatialRS = spatialRS;
    }

    public Float getElevation() {
        return elevation;
    }

    public void setElevation(Float elevation) {
        this.elevation = elevation;
    }

    public void setElevation(Integer elevation) {
        this.elevation = elevation.floatValue();
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public Precision getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) throws FloraOnException {
        this.precision = new Precision(precision);
    }

    public void setPrecision(Precision precision) {
        this.precision = precision;
    }

    public Integer getYear() {
        return Constants.isNoData(year) ? null : year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return Constants.isNoData(month) ? null : month;
    }

    public void setMonth(Integer month) {
        if(!Constants.isNoData(month) && (month < 1 || month > 12)) {
//            Log.warn("Invalid month " + month);
            return;
        }
        this.month = month;
    }

    public Integer getDay() {
        return Constants.isNoData(day) ? null : day;
    }

    public void setDay(Integer day) {
        if(!Constants.isNoData(day) && (day < 1 || day > 31)) {
//            Log.warn("Invalid day " + day);
            return;
//            throw new IllegalArgumentException("Invalid day " + day);
        }
        this.day = day;
    }

    public String _getDate() {
/*
        Calendar c = new GregorianCalendar();
        if(year != null) c.set(Calendar.YEAR, year);
        if(month != null) c.set(Calendar.MONTH, month);
        if(day != null) c.set(Calendar.DAY_OF_MONTH, day);
        return Constants.dateFormat.format(c.getTime());
*/
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.isNullOrNoData(day) ? "--" : day).append("/")
                .append(Constants.isNullOrNoData(month) ? "--" : month).append("/")
                .append(Constants.isNullOrNoData(year) ? "----" : year);
        return sb.toString();
    }

    public boolean _isDateEmpty() {
        return (Constants.isNullOrNoData(day) || day == 0) && (Constants.isNullOrNoData(month) || month == 0)
                && (Constants.isNullOrNoData(year) || year == 0);
    }

    public String _getDateYMD() {
        return formatDateYMD(this.day, this.month, this.year);
    }

    static public String formatDateYMD(Integer day, Integer month, Integer year) {
        return formatDateYMD(day, month, year, "-");
    }

    static public String formatDateYMD(Integer day, Integer month, Integer year, String nullPlaceholder) {
        String yp = new String(new char[4]).replace("\0", nullPlaceholder);
        String dp = new String(new char[2]).replace("\0", nullPlaceholder);
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.isNullOrNoData(year) ? yp : year).append("/")
                .append(Constants.isNullOrNoData(month) ? dp : String.format("%02d", month)).append("/")
                .append(Constants.isNullOrNoData(day) ? dp : String.format("%02d", day));
        return sb.toString();
    }

    public UTMCoordinate _getUTMCoordinates() {
        if(this._getLatitude() == null || this._getLongitude() == null) return null;
        return CoordinateConversion.LatLonToUtmWGS84(this._getLatitude(), this._getLongitude(), 0);
    }

    public String _getMGRSString(long sizeOfSquare) {
        return CoordinateConversion.LatLongToMGRS(this._getLatitude(), this._getLongitude(), sizeOfSquare);
    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    public String getPubNotes() {
        return pubNotes;
    }

    public void setPubNotes(String pubNotes) {
        this.pubNotes = pubNotes;
    }

    public String getPrivNotes() {
        return privNotes;
    }

    public void setPrivNotes(String privNotes) {
        this.privNotes = privNotes;
    }

    public String getGeology() {
        return geology;
    }

    public void setGeology(String geology) {
        this.geology = geology;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String[] getObservers() {
        return StringUtils.isArrayEmpty(observers) ? new String[0] : observers;
    }

    public void setObservers(String[] observers) {
        this.observers = observers;
    }

    public String[] getCollectors() {
        return StringUtils.isArrayEmpty(collectors) ? new String[0] : collectors;
    }

    public void setCollectors(String[] collectors) {
        this.collectors = collectors;
    }

    public String[] getDets() {
        return StringUtils.isArrayEmpty(dets) ? new String[0] : dets;
    }

    public void setDets(String[] dets) {
        this.dets = dets;
    }

    @Override
    public String getVerbLocality() {
        return verbLocality;
    }

    @Override
    public void setVerbLocality(String verbLocality) {
        this.verbLocality = verbLocality;
    }

    @Override
    public String getLocality() {
        return locality;
    }

    @Override
    public void setLocality(String locality) {
        this.locality = locality;
    }

    @Override
    public String getMunicipality() {
        return municipality;
    }

    @Override
    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    @Override
    public String getProvince() {
        return province;
    }

    @Override
    public void setProvince(String province) {
        this.province = province;
    }

    @Override
    public String getCounty() {
        return county;
    }

    @Override
    public void setCounty(String county) {
        this.county = county;
    }

    public String getCode() {
        return code == null ? ((_getTaxa() != null && _getTaxa().length == 1) ? _getTaxa()[0].getGpsCode() : null) : code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getThreats() {
        return threats;
    }

    public void setThreats(String threats) {
        this.threats = threats;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public Float getArea() {
        return area;
    }

    public void setArea(Float area) {
        this.area = area;
    }

    public Float getTotalCover() {
        return totalCover;
    }

    public void setTotalCover(Float totalCover) {
        this.totalCover = totalCover;
    }

    public Float getMeanHeight() {
        return meanHeight;
    }

    public void setMeanHeight(Float meanHeight) {
        this.meanHeight = meanHeight;
    }

    public String getAspect() {
        return aspect;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
    }

    public Integer getSlope() {
        return slope;
    }

    public void setSlope(Integer slope) {
        this.slope = slope;
    }

    public String getCoverIndex() {
        return coverIndex;
    }

    public void setCoverIndex(String coverIndex) {
        this.coverIndex = coverIndex;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public List<OBSERVED_IN> getUnmatchedOccurrences() {
        return unmatchedOccurrences == null ? (this.unmatchedOccurrences = new ArrayList<>()) : unmatchedOccurrences;
    }

    public void setUnmatchedOccurrences(List<OBSERVED_IN> unmatchedOccurrences) {
        this.unmatchedOccurrences = unmatchedOccurrences;
    }

    /* ****************************************/
    /* Getters for transient fields           */
    /* ****************************************/

    public String[] _getObserverNames() {
        return StringUtils.isArrayEmpty(this.observerNames) ? new String[0] : this.observerNames;
    }

    public void _setObserverNames(String[] observerNames) {
        this.observerNames = observerNames;
    }

    public String _getMaintainerName() {
        return StringUtils.isStringEmpty(maintainerName) ? "" : maintainerName;
    }

    public void _setMaintainerName(String maintainerName) {
        this.maintainerName = maintainerName;
    }

    public OBSERVED_IN[] _getTaxa() {
        return StringUtils.isArrayEmpty(this.taxa) ?
                (unmatchedOccurrences == null ?
                        new OBSERVED_IN[0] : unmatchedOccurrences.toArray(new OBSERVED_IN[0]))
                : this.taxa;
    }

    public List<OBSERVED_IN> _getOccurrences() {
        // TODO: this should return the occurrences that are graph links aswell!
        return getUnmatchedOccurrences();
    }

    /**
     * Checks whether there are duplicated taxa (it only checks the taxa name, not other fields!)
     * @return
     */
    public boolean _hasDuplicatedTaxa() {
        Set<String> taxa = new HashSet<>();
        for(OBSERVED_IN oi : this._getTaxa()) {
            if(taxa.contains(oi.getTaxEntMatch()) || taxa.contains(oi.getVerbTaxon()))
                return true;
            if(StringUtils.isStringEmpty(oi.getTaxEntMatch()))
                taxa.add(oi.getVerbTaxon());
            else
                taxa.add(oi.getTaxEntMatch());
        }
        return false;
    }

    /**
     * @return True if this inventory has more than one single coordinate (including the inventory's)
     */
    public boolean _hasMultipleCoordinates() {
        Set<String> coords = new HashSet<>();
        if(!Constants.isNullOrNoData(this.getLatitude()) && !Constants.isNullOrNoData(this.getLongitude()))
            coords.add(String.format("%.6f %.6f", this.getLatitude(), this.getLongitude()));

        for(OBSERVED_IN oi : this._getTaxa()) {
            if(Constants.isNullOrNoData(oi.getObservationLatitude()) || Constants.isNullOrNoData(oi.getObservationLongitude())) continue;
            coords.add(String.format("%.6f %.6f", oi.getObservationLatitude(), oi.getObservationLongitude()));
            if(coords.size() > 1) return true;
        }
        return false;
    }

    /**
     * @return The unique coordinate of the occurrences, if it is unique. In all other cases, returns NULL.
     * Occurrences without coordinates are ignored.
     */
    public LatLongCoordinate _getUniqueCoordinate(boolean accountForInventory) {
        Set<String> coords = new HashSet<>();
        float lat = Constants.NODATA, lng = Constants.NODATA;
        if(accountForInventory) {
            if (!Constants.isNullOrNoData(this.getLatitude()) && !Constants.isNullOrNoData(this.getLongitude())) {
                coords.add(String.format("%.6f %.6f", lat = this.getLatitude(), lng = this.getLongitude()));
//            Log.info(String.format("INV: %.6f %.6f", lat = this.getLatitude(), lng = this.getLongitude()));
            }
        }
        for(OBSERVED_IN oi : this._getTaxa()) {
            if(Constants.isNullOrNoData(oi.getObservationLatitude()) || Constants.isNullOrNoData(oi.getObservationLongitude())) continue;
            coords.add(String.format("%.6f %.6f", lat = oi.getObservationLatitude(), lng = oi.getObservationLongitude()));
//            Log.info(String.format("OCC: %.6f %.6f", lat = oi.getObservationLatitude(), lng = oi.getObservationLongitude()));
            if(coords.size() > 1) return null;
        }
        if(coords.size() == 0) return null;

        return new LatLongCoordinate(lat, lng);
    }

    /**
     * Gets a textual summary of the taxa.
     * @param nTaxa How many taxa to show
     * @return
     */
    public String _getSampleTaxa(int nTaxa) {
        OBSERVED_IN[] tmp = _getTaxa();
        if(tmp.length == 0) return "[sem taxa]";
        List<String> tmp1 = new ArrayList<>();
        int i;
        StringBuilder suffix = new StringBuilder();
        for (i = 0; i < nTaxa && i < tmp.length; i++) {
            if(tmp[i].getConfidence() == OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL)
                suffix.append("?");
            if(tmp[i].getNaturalization() != null && tmp[i].getNaturalization() != OccurrenceConstants.OccurrenceNaturalization.WILD)
                suffix.append("*");
            if(tmp[i].getTaxEnt() == null) {
                if(tmp[i].getVerbTaxon() == null || tmp[i].getVerbTaxon().equals(""))
                    tmp1.add("[sem nome]");
                else
                    tmp1.add(tmp[i].getVerbTaxon() + suffix);
            } else
                tmp1.add("<i>" + tmp[i].getTaxEnt().getName() + suffix + "</i>");
            suffix.setLength(0);
        }
        if(i < tmp.length) tmp1.add("... e mais " + (tmp.length - i));
        return StringUtils.implode(", ", tmp1.toArray(new String[tmp1.size()]));
    }

    @Override
    public Constants.NodeTypes getType() {
        return Constants.NodeTypes.inventory;
    }

    @Override
    public String getTypeAsString() {
        return this.getType().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Inventory that = (Inventory) o;

        if(code != null) return code.equals(that.code);
        if ((precision != null && precision._isImprecise()) || (that.precision != null && that.precision._isImprecise())
            || (precision != null ? !precision.equals(that.precision) : that.precision != null) || getLatitude() == null
                || getLongitude() == null || that.getLatitude() == null || that.getLongitude() == null) return false;
        if (getLatitude() != null ? !getLatitude().equals(that.getLatitude()) : that.getLatitude() != null) return false;
        if (getLongitude() != null ? !getLongitude().equals(that.getLongitude()) : that.getLongitude() != null) return false;
        if (getYear() != null ? !getYear().equals(that.getYear()) : that.getYear() != null) return false;
        if (getMonth() != null ? !getMonth().equals(that.getMonth()) : that.getMonth() != null) return false;
        if (getDay() != null ? !getDay().equals(that.getDay()) : that.getDay() != null) return false;
        if ((getYear() == null && getMonth() == null && getDay() == null)  // if any of the dates is null, it's never equal
                || (that.getYear() == null && that.getMonth() == null && that.getDay() == null)) return false;
        if (municipality != null ? !municipality.equals(that.municipality) : that.municipality != null) return false;
        if (county != null ? !county.equals(that.county) : that.county != null) return false;
        if (locality != null ? !locality.equals(that.locality) : that.locality != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(observers, that.observers)) return false;
        return code != null ? code.equals(that.code) : that.code == null;
    }

    /**
     * NOTE that this implementation of hashCode and equals assume that if an inventory is in the same place, same date
     * and same observers, then it is the same inventory, no matter the other fields. If the inventory is imprecise, it
     * will never be equal to another one.
     * @return
     */
    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;    // NOTE: we don't use the getter here cause the getter does some processing to avoid nulls. here we want the real inventory code as is.
        if(code != null && !code.equals("")) return result;     // code rules!
        if(getLatitude() == null || getLongitude() == null) return result;
        result = 31 * result + (precision != null ? precision.hashCode() : 0);
        result = 31 * result + (getLatitude() != null ? getLatitude().hashCode() : 0);
        result = 31 * result + (getLongitude() != null ? getLongitude().hashCode() : 0);
        result = 31 * result + (getYear() != null ? getYear().hashCode() : 0);
        result = 31 * result + (getMonth() != null ? getMonth().hashCode() : 0);
        result = 31 * result + (getDay() != null ? getDay().hashCode() : 0);
        result = 31 * result + (municipality != null ? municipality.hashCode() : 0);
        result = 31 * result + (county != null ? county.hashCode() : 0);
        result = 31 * result + (locality != null ? locality.hashCode() : 0);
        result = 31 * result + (observers != null ? Arrays.hashCode(observers) : 0);
        return result;
    }
}
