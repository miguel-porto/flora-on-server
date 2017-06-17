package pt.floraon.geocoding.entities;

/**
 * Created by miguel on 11-06-2017.
 */
public class MatchedToponym extends Toponym implements Comparable<MatchedToponym> {
    private Integer levenDist;

    public Integer getLevenDist() {
        return levenDist;
    }

    public MatchedToponym(Toponym other, Integer levenDistance) {
            super();
            this.setLatitude(other.getLatitude());
            this.setLongitude(other.getLongitude());
            this.setElevation(other.getElevation());
            this.setToponymType(other.getToponymType());
            this.setLocality(other.getLocality());
            this.setMunicipality(other.getMunicipality());
            this.setProvince(other.getProvince());
            this.setCounty(other.getCounty());
            this.levenDist = levenDistance;
    }

    @Override
    public int compareTo(MatchedToponym toponym) {
        return (levenDist == null || toponym.levenDist == null) ? 0 : levenDist.compareTo(toponym.levenDist);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
