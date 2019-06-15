package pt.floraon.geometry.gridmaps;

import java.io.PrintWriter;
import java.util.*;

/**
 * A map composed of squares {@link ISquare}, each holding a data object.
 * @param <T> The type of squares composing this map.
 */
public class GridMap<T extends ISquare> implements Iterable<Map.Entry<T, SquareData>>, Map<T, SquareData>, WKTExportable {
    private final Map<T, SquareData> squares = new HashMap<>();
    private final String gradient[] = {"#FFCDD2","#EF9A9A","#E57373","#EF5350","#F44336","#E53935","#D32F2F","#C62828","#B71C1C"};  //"#FFEBEE",
    private int maxNumSp = 0;

    @Override
    public int size() {
        return this.squares.size();
    }

    @Override
    public boolean isEmpty() {
        return this.squares.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return this.squares.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return this.squares.containsValue(o);
    }

    @Override
    public SquareData get(Object o) {
        return this.squares.get(o);
    }

    @Override
    public SquareData put(T square, SquareData squareData) {
        return this.squares.put(square, squareData);
    }

    public SquareData put(T square, Set<String> strings) {
        return put(square, new ListOfTaxa(strings));
    }

    @Override
    public SquareData remove(Object o) {
        return this.squares.remove(o);
    }

    @Override
    public void putAll(Map<? extends T, ? extends SquareData> map) {
        this.squares.putAll(map);
    }

    @Override
    public void clear() {
        this.squares.clear();
    }

    @Override
    public Set<T> keySet() {
        return this.squares.keySet();
    }

    @Override
    public Collection<SquareData> values() {
        return this.squares.values();
    }

    @Override
    public Set<Entry<T, SquareData>> entrySet() {
        return this.squares.entrySet();
    }

    public Boolean isColored() {
        if(this.size() == 0) return null;
        T first = this.keySet().iterator().next();
        return first.hasColor();
    }

    @Override
    public Iterator<Entry<T, SquareData>> iterator() {
        if(!this.keySet().iterator().hasNext())
            return Collections.emptyIterator();
        T first = this.keySet().iterator().next();
        if(!first.hasColor()) {
            return this.entrySet().iterator();
        }
        // first compute colors
        for(Map.Entry<T, SquareData> sqs : this.entrySet()) {
            if(sqs.getValue().getNumber() > maxNumSp)
                maxNumSp = sqs.getValue().getNumber();
        }

        return new SquareIterator(this.entrySet().iterator());
    }

    @Override
    public String toWKT() {
        StringBuilder sb = new StringBuilder();
        for (Entry<? extends WKTExportable, SquareData> entry : this) {
            sb.append(entry.getKey().toWKT()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void toWKT(PrintWriter writer) {
        Iterator<? extends Map.Entry<? extends WKTExportable, SquareData>> it = this.iterator();
        writer.println("wkt");
        while(it.hasNext()) {
            Map.Entry<? extends WKTExportable, SquareData> entry = it.next();
            entry.getKey().toWKT(writer);
        }
    }

    public class SquareIterator implements Iterator<Entry<T, SquareData>> {
        private Iterator<Entry<T, SquareData>> it;

        public SquareIterator(Iterator<Entry<T, SquareData>> iterator) {
            this.it = iterator;
        }
        @Override
        public boolean hasNext() {
            return this.it.hasNext();
        }

        @Override
        public Entry<T, SquareData> next() {
            Entry<T, SquareData> item = this.it.next();
            T key = item.getKey();
            float prop = (float) item.getValue().getNumber() / maxNumSp;
//                float prop = (float) Math.log(this.squares.get(s).size()) / maxNSp;
//                String color = String.format("#%02x%02x%02x", (int) (prop * 255f), 60, 0);
            String color = gradient[(int)(prop * 8f)];
            key.setColor(color);
            item = new AbstractMap.SimpleEntry<>(key, item.getValue());
//            System.out.println(item.getKey().getColor());
            return item;
        }

        @Override
        public void remove() {

        }
    }
}
